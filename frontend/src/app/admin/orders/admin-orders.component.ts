import {Component, computed, inject, signal} from '@angular/core';
import {Router} from '@angular/router';
import {rxResource} from '@angular/core/rxjs-interop';
import {PaginatorState} from 'primeng/paginator';
import {TranslateService} from '@ngx-translate/core';
import {LanguageService} from '../../services/language.service';
import {AdminOrdersStore} from './admin-orders-store';
import {AdminOrderService} from '../../services/admin/admin-order.service';
import {AdminInitData, AdminOrderListRow} from '../../models/admin';
import {orderStatusSeverity, statusesInLifecycleOrder} from '../../utils/order-status';
import {toLocalDateTime} from '../../utils/date-format';

@Component({
  selector: 'app-admin-orders',
  standalone: false,
  templateUrl: './admin-orders.component.html',
  styleUrl: './admin-orders.component.scss',
  providers: [AdminOrdersStore],
})
export class AdminOrdersComponent {
  private readonly router = inject(Router);
  private readonly adminOrderService = inject(AdminOrderService);
  private readonly translate = inject(TranslateService);
  private readonly languageService = inject(LanguageService);

  readonly store = inject(AdminOrdersStore);
  readonly orderStatusSeverity = orderStatusSeverity;

  readonly initData = rxResource<AdminInitData, undefined>({
    stream: () => this.adminOrderService.getInitData(),
  });

  readonly statusOptions = computed(() => {
    // instant() nie reaguje na zmianę języka — sygnał lang() wymusza przeliczenie.
    this.languageService.lang();
    if (!this.initData.hasValue()) return [];
    return statusesInLifecycleOrder(this.initData.value().orderStatuses)
      .map(name => ({label: this.translate.instant('orderStatus.' + name), value: name}));
  });

  readonly exportRange = signal<Date[] | null>(null);
  readonly exportStatus = signal<string | null>(null);

  onPageChange(event: PaginatorState) {
    this.store.setPage({page: event.page ?? 0, size: event.rows ?? 10});
  }

  goToDetails(order: AdminOrderListRow): void {
    void this.router.navigate(['/admin/order', order.id]);
  }

  exportCsv(): void {
    const range = this.exportRange();
    const status = this.exportStatus();
    if (!range || !range[0] || !range[1] || !status) return;
    // Lokalne daty bez konwersji na UTC — toISOString() przesuwałoby zakres
    // o dzień dla stref na wschód od UTC (backend parsuje to jako LocalDate).
    window.open(this.adminOrderService.exportUrl(toLocalDateTime(range[0]), toLocalDateTime(range[1]), status), '_blank');
  }
}
