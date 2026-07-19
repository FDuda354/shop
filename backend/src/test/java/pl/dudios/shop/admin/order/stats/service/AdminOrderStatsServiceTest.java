package pl.dudios.shop.admin.order.stats.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dudios.shop.admin.order.model.AdminOrder;
import pl.dudios.shop.admin.order.repository.AdminOrderRepo;
import pl.dudios.shop.common.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * Wykres sprzedaży w panelu admina: dzienne podsumowanie zrealizowanych zamówień
 * od pierwszego dnia bieżącego miesiąca do teraz. Mockowane jest wyłącznie
 * repozytorium (granica DB) — encje AdminOrder i sama agregacja to prawdziwa
 * logika, więc testy pilnują tego, co widzi wołający: zakresu odpytania bazy
 * oraz trzech równoległych list (etykiety dni, wartość i liczba zamówień).
 * <p>
 * Serwis czyta zegar przez LocalDateTime.now(), więc fixture'y są budowane
 * względem bieżącej daty — nie da się w nim ustawić sztywnego dnia.
 */
@ExtendWith(MockitoExtension.class)
class AdminOrderStatsServiceTest {

    @Mock
    private AdminOrderRepo adminOrderRepo;
    @InjectMocks
    private AdminOrderStatsService adminOrderStatsService;

    @Nested
    @DisplayName("the window the statistics are taken from")
    class QueryWindow {

        @BeforeEach
        void repositoryReturnsNothing() {
            given(adminOrderRepo.findAllByPlaceDateIsBetweenAndOrderStatus(any(), any(), any()))
                    .willReturn(List.of());
        }

        @Test
        @DisplayName("counts completed orders only — nothing that is still in flight or cancelled")
        void asksForCompletedOrdersOnly() {
            //Given

            //When
            adminOrderStatsService.getOrdersStats();

            //Then
            assertThat(captureQuery().status()).isEqualTo(OrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("spans the current month: from midnight of the 1st up to the current moment")
        void spansCurrentMonthUpToNow() {
            //Given
            var beforeCall = LocalDateTime.now();

            //When
            adminOrderStatsService.getOrdersStats();

            //Then
            var query = captureQuery();
            assertThat(query.from().toLocalDate()).isEqualTo(LocalDate.now().withDayOfMonth(1));
            assertThat(query.from().toLocalTime().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(LocalTime.MIDNIGHT);
            assertThat(query.to()).isBetween(beforeCall, LocalDateTime.now());
        }
    }

    @Nested
    @DisplayName("a month without any completed order")
    class EmptyMonth {

        @Test
        @DisplayName("still reports every elapsed day, each with zero turnover and zero orders")
        void reportsEveryElapsedDayAsZero() {
            //Given
            given(adminOrderRepo.findAllByPlaceDateIsBetweenAndOrderStatus(any(), any(), any()))
                    .willReturn(List.of());

            //When
            var stats = adminOrderStatsService.getOrdersStats();

            //Then
            assertThat(stats.labels()).containsExactlyElementsOf(elapsedDaysOfMonth());
            assertThat(stats.ordersCount()).allSatisfy(count -> assertThat(count).isZero());
            assertThat(stats.ordersValue()).allSatisfy(value -> assertThat(value).isEqualByComparingTo(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("keeps the three chart series aligned so day N shares one index across all of them")
        void keepsChartSeriesAligned() {
            //Given
            given(adminOrderRepo.findAllByPlaceDateIsBetweenAndOrderStatus(any(), any(), any()))
                    .willReturn(List.of());

            //When
            var stats = adminOrderStatsService.getOrdersStats();

            //Then
            assertThat(stats.ordersValue()).hasSameSizeAs(stats.labels());
            assertThat(stats.ordersCount()).hasSameSizeAs(stats.labels());
        }
    }

    @Nested
    @DisplayName("aggregating completed orders day by day")
    class DailyAggregation {

        @Test
        @DisplayName("adds up the gross value of every order placed on the same day")
        void sumsGrossValueOfTheSameDay() {
            //Given
            given(adminOrderRepo.findAllByPlaceDateIsBetweenAndOrderStatus(any(), any(), any()))
                    .willReturn(List.of(
                            completedOrderToday("100.00"),
                            completedOrderToday("49.50")));

            //When
            var stats = adminOrderStatsService.getOrdersStats();

            //Then
            assertThat(stats.ordersValue().get(todayIndex())).isEqualByComparingTo("149.50");
        }

        @Test
        @DisplayName("counts how many orders were placed on that day")
        void countsOrdersOfTheSameDay() {
            //Given
            given(adminOrderRepo.findAllByPlaceDateIsBetweenAndOrderStatus(any(), any(), any()))
                    .willReturn(List.of(
                            completedOrderToday("100.00"),
                            completedOrderToday("49.50")));

            //When
            var stats = adminOrderStatsService.getOrdersStats();

            //Then
            assertThat(stats.ordersCount().get(todayIndex())).isEqualTo(2L);
        }

        @Test
        @DisplayName("books an order on its own day and leaves the remaining days untouched")
        void booksOrderOnItsOwnDayOnly() {
            //Given
            given(adminOrderRepo.findAllByPlaceDateIsBetweenAndOrderStatus(any(), any(), any()))
                    .willReturn(List.of(completedOrderToday("250.00")));

            //When
            var stats = adminOrderStatsService.getOrdersStats();

            //Then
            assertThat(stats.ordersValue().get(todayIndex())).isEqualByComparingTo("250.00");
            assertThat(withoutToday(stats.ordersValue()))
                    .allSatisfy(value -> assertThat(value).isEqualByComparingTo(BigDecimal.ZERO));
            assertThat(withoutToday(stats.ordersCount()))
                    .allSatisfy(count -> assertThat(count).isZero());
        }

        @Test
        @DisplayName("labels carry the calendar day the matching turnover belongs to")
        void labelsCarryTheDayOfTheirTurnover() {
            //Given
            given(adminOrderRepo.findAllByPlaceDateIsBetweenAndOrderStatus(any(), any(), any()))
                    .willReturn(List.of(completedOrderOn(LocalDate.now().atTime(0, 1), "70.00")));

            //When
            var stats = adminOrderStatsService.getOrdersStats();

            //Then
            assertThat(stats.labels().get(todayIndex())).isEqualTo(LocalDate.now().getDayOfMonth());
            assertThat(stats.ordersValue().get(todayIndex())).isEqualByComparingTo("70.00");
        }
    }

    private QueryArgs captureQuery() {
        var fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        var toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        var statusCaptor = ArgumentCaptor.forClass(OrderStatus.class);
        then(adminOrderRepo).should().findAllByPlaceDateIsBetweenAndOrderStatus(
                fromCaptor.capture(), toCaptor.capture(), statusCaptor.capture());
        return new QueryArgs(fromCaptor.getValue(), toCaptor.getValue(), statusCaptor.getValue());
    }

    private static AdminOrder completedOrderToday(String grossValue) {
        return completedOrderOn(LocalDate.now().atTime(12, 0), grossValue);
    }

    private static AdminOrder completedOrderOn(LocalDateTime placeDate, String grossValue) {
        var order = new AdminOrder();
        order.setPlaceDate(placeDate);
        order.setOrderStatus(OrderStatus.COMPLETED);
        order.setGrossValue(new BigDecimal(grossValue));
        return order;
    }

    private static List<Integer> elapsedDaysOfMonth() {
        return IntStream.rangeClosed(1, LocalDate.now().getDayOfMonth()).boxed().toList();
    }

    private static int todayIndex() {
        return LocalDate.now().getDayOfMonth() - 1;
    }

    private static <T> List<T> withoutToday(List<T> series) {
        var remaining = new ArrayList<>(series);
        remaining.remove(todayIndex());
        return remaining;
    }

    private record QueryArgs(LocalDateTime from, LocalDateTime to, OrderStatus status) {
    }
}
