import {Component, computed, inject} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslateService} from '@ngx-translate/core';
import {LanguageService} from '../../services/language.service';
import {AdminOrderService} from '../../services/admin/admin-order.service';
import {AdminOrderStats} from '../../models/admin';

@Component({
  selector: 'app-admin-dashboard',
  standalone: false,
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss',
})
export class AdminDashboardComponent {
  private readonly adminOrderService = inject(AdminOrderService);
  private readonly translate = inject(TranslateService);
  private readonly languageService = inject(LanguageService);

  readonly stats = rxResource<AdminOrderStats, undefined>({
    stream: () => this.adminOrderService.getStats(),
  });

  readonly totalOrders = computed(() =>
    this.stats.hasValue() ? this.stats.value().ordersCount.reduce((sum, count) => sum + count, 0) : 0);

  readonly totalValue = computed(() =>
    this.stats.hasValue() ? this.stats.value().ordersValue.reduce((sum, value) => sum + Number(value), 0) : 0);

  readonly chartData = computed<any>(() => {
    // instant() nie reaguje na zmianę języka — sygnał lang() wymusza przeliczenie.
    this.languageService.lang();
    if (!this.stats.hasValue()) return undefined;
    const stats = this.stats.value();
    return {
      labels: stats.labels,
      datasets: [
        {
          type: 'bar',
          label: this.translate.instant('admin.dashboard.chartCount'),
          data: stats.ordersCount,
          backgroundColor: 'rgba(16, 185, 129, 0.5)',
          borderColor: 'rgb(5, 150, 105)',
          borderWidth: 1,
          yAxisID: 'yCount',
        },
        {
          type: 'line',
          label: this.translate.instant('admin.dashboard.chartValue'),
          data: stats.ordersValue,
          borderColor: 'rgb(5, 150, 105)',
          backgroundColor: 'rgb(5, 150, 105)',
          tension: 0.3,
          yAxisID: 'yValue',
        },
      ],
    };
  });

  readonly chartOptions = {
    maintainAspectRatio: false,
    scales: {
      yCount: {type: 'linear', position: 'left', beginAtZero: true, ticks: {precision: 0}},
      yValue: {type: 'linear', position: 'right', beginAtZero: true, grid: {drawOnChartArea: false}},
    },
  };
}
