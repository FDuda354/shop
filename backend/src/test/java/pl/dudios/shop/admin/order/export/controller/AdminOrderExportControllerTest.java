package pl.dudios.shop.admin.order.export.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import pl.dudios.shop.admin.order.export.service.AdminOrderExportService;
import pl.dudios.shop.admin.order.model.AdminOrder;
import pl.dudios.shop.admin.order.model.AdminPayment;
import pl.dudios.shop.common.model.OrderStatus;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * Eksport zamówień do CSV dla panelu admina.
 * <p>
 * Mockowana jest wyłącznie granica systemu — {@link AdminOrderExportService}, który schodzi do bazy.
 * Encje {@link AdminOrder} i {@link AdminPayment} są prawdziwymi obiektami domenowymi, a enum
 * {@link OrderStatus} prawdziwym enumem, więc testy pilnują tego, co widzi wołający: okna czasowego
 * przekazanego do zapytania oraz zawartości pliku CSV, który admin otworzy w arkuszu.
 * <p>
 * {@code AdminPayment} nie ma żadnego publicznego settera ani konstruktora z argumentami (samo
 * {@code @Getter}), dlatego jego nazwa ustawiana jest refleksją w prywatnym fixture — to jedyny
 * sposób zbudowania prawdziwej encji bez sięgania po mocka obiektu domenowego.
 */
@ExtendWith(MockitoExtension.class)
class AdminOrderExportControllerTest {

    private static final String HEADER_LINE =
            "Id,PlaceDate,OrderStatus,GrossValue,FirstName,LastName,Street,ZipCode,City,Email,Phone,Payment";

    @Mock
    private AdminOrderExportService adminOrderExportService;
    @InjectMocks
    private AdminOrderExportController adminOrderExportController;

    @Nested
    @DisplayName("translating the admin's date range into a query window")
    class QueryWindow {

        @Test
        @DisplayName("covers both boundary days completely — from midnight up to the last second of the closing day")
        void coversBothBoundaryDaysCompletely() {
            //Given
            given(adminOrderExportService.exportOrders(any(), any(), any())).willReturn(List.of());

            //When
            adminOrderExportController.exportOrders(
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), OrderStatus.PAID);

            //Then
            then(adminOrderExportService).should().exportOrders(
                    LocalDateTime.of(2024, 1, 1, 0, 0, 0),
                    LocalDateTime.of(2024, 1, 31, 23, 59, 59),
                    OrderStatus.PAID);
            then(adminOrderExportService).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("asks for the whole day when the admin exports a single date, not for a zero-length instant")
        void singleDateStillSpansTheWholeDay() {
            //Given
            given(adminOrderExportService.exportOrders(any(), any(), any())).willReturn(List.of());

            //When
            adminOrderExportController.exportOrders(
                    LocalDate.of(2024, 5, 9), LocalDate.of(2024, 5, 9), OrderStatus.NEW);

            //Then
            then(adminOrderExportService).should().exportOrders(
                    LocalDateTime.of(2024, 5, 9, 0, 0, 0),
                    LocalDateTime.of(2024, 5, 9, 23, 59, 59),
                    OrderStatus.NEW);
        }
    }

    @Nested
    @DisplayName("the CSV the admin downloads")
    class CsvBody {

        @Test
        @DisplayName("contains the column header and nothing else when no order matches the filter")
        void headerOnlyWhenNothingMatches() throws IOException {
            //Given
            given(adminOrderExportService.exportOrders(any(), any(), any())).willReturn(List.of());

            //When
            var response = exportJanuary(OrderStatus.CANCELLED);

            //Then
            assertThat(lines(response)).containsExactly(HEADER_LINE);
        }

        @Test
        @DisplayName("writes every order field into its declared column")
        void writesEveryFieldIntoItsColumn() throws IOException {
            //Given
            given(adminOrderExportService.exportOrders(any(), any(), any())).willReturn(List.of(order(1L)));

            //When
            var response = exportJanuary(OrderStatus.PAID);

            //Then
            assertThat(lines(response)).containsExactly(
                    HEADER_LINE,
                    "1,2024-01-15T10:30,PAID,199.99,Jan,Kowalski,Kwiatowa 5,00-001,Warszawa,jan@example.com,555111222,BLIK");
        }

        @Test
        @DisplayName("writes one row per order and keeps them in the order the service returned")
        void writesOneRowPerOrder() throws IOException {
            //Given
            given(adminOrderExportService.exportOrders(any(), any(), any()))
                    .willReturn(List.of(order(1L), order(2L)));

            //When
            var response = exportJanuary(OrderStatus.PAID);

            //Then
            var csvLines = lines(response);
            assertThat(csvLines).hasSize(3);
            assertThat(csvLines[1]).startsWith("1,");
            assertThat(csvLines[2]).startsWith("2,");
        }

        @Test
        @DisplayName("quotes an address containing a comma so the following columns do not shift")
        void quotesValueContainingComma() throws IOException {
            //Given
            var orderWithCommaInStreet = order(3L);
            orderWithCommaInStreet.setStreet("Kwiatowa 5, m. 3");
            given(adminOrderExportService.exportOrders(any(), any(), any()))
                    .willReturn(List.of(orderWithCommaInStreet));

            //When
            var response = exportJanuary(OrderStatus.PAID);

            //Then
            assertThat(lines(response)[1]).contains("\"Kwiatowa 5, m. 3\",00-001,Warszawa");
        }

        @Test
        @DisplayName("exports an order with no contact details as empty columns instead of failing the whole file")
        void missingContactDetailsBecomeEmptyColumns() throws IOException {
            //Given
            var orderWithoutContactDetails = order(7L);
            orderWithoutContactDetails.setEmail(null);
            orderWithoutContactDetails.setPhone(null);
            given(adminOrderExportService.exportOrders(any(), any(), any()))
                    .willReturn(List.of(orderWithoutContactDetails));

            //When
            var response = exportJanuary(OrderStatus.PAID);

            //Then
            assertThat(lines(response)[1])
                    .isEqualTo("7,2024-01-15T10:30,PAID,199.99,Jan,Kowalski,Kwiatowa 5,00-001,Warszawa,,,BLIK");
        }
    }

    @Nested
    @DisplayName("the download response itself")
    class DownloadResponse {

        @Test
        @DisplayName("answers 200 with a text/csv payload so the browser treats it as a spreadsheet")
        void answersWithCsvPayload() {
            //Given
            given(adminOrderExportService.exportOrders(any(), any(), any())).willReturn(List.of(order(1L)));

            //When
            var response = exportJanuary(OrderStatus.PAID);

            //Then
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("text/csv"));
        }

        @Test
        @DisplayName("names the downloaded file after the exported date range")
        void fileNameCarriesTheExportedRange() {
            //Given
            given(adminOrderExportService.exportOrders(any(), any(), any())).willReturn(List.of());

            //When
            var response = adminOrderExportController.exportOrders(
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), OrderStatus.PAID);

            //Then
            assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                    .contains("2024-01-01")
                    .contains("2024-01-31")
                    .endsWith(".csv");
        }
    }

    private ResponseEntity<Resource> exportJanuary(OrderStatus orderStatus) {
        return adminOrderExportController.exportOrders(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), orderStatus);
    }

    private static String[] lines(ResponseEntity<Resource> response) throws IOException {
        var body = new String(response.getBody().getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return body.split("\\R");
    }

    private static AdminOrder order(Long id) {
        var order = new AdminOrder();
        order.setId(id);
        order.setPlaceDate(LocalDateTime.of(2024, 1, 15, 10, 30));
        order.setOrderStatus(OrderStatus.PAID);
        order.setGrossValue(new BigDecimal("199.99"));
        order.setFirstName("Jan");
        order.setLastName("Kowalski");
        order.setStreet("Kwiatowa 5");
        order.setZipCode("00-001");
        order.setCity("Warszawa");
        order.setEmail("jan@example.com");
        order.setPhone("555111222");
        order.setPayment(payment("BLIK"));
        return order;
    }

    private static AdminPayment payment(String name) {
        var adminPayment = new AdminPayment();
        ReflectionTestUtils.setField(adminPayment, "name", name);
        return adminPayment;
    }
}
