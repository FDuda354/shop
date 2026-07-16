import {Component, computed, effect, inject, signal, untracked} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {rxResource, toSignal} from '@angular/core/rxjs-interop';
import {map} from 'rxjs/operators';
import {TranslateService} from '@ngx-translate/core';
import {LanguageService} from '../../../services/language.service';
import {AdminOrderService} from '../../../services/admin/admin-order.service';
import {NotificationService} from '../../../services/notification.service';
import {AdminInitData, AdminOrder} from '../../../models/admin';
import {imageUrl} from '../../../utils/image-url';
import {localizedName} from '../../../utils/localized';
import {orderStatusSeverity, statusesInLifecycleOrder} from '../../../utils/order-status';

@Component({
  selector: 'app-admin-order-details',
  standalone: false,
  templateUrl: './admin-order-details.component.html',
  styleUrl: './admin-order-details.component.scss',
})
export class AdminOrderDetailsComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly adminOrderService = inject(AdminOrderService);
  private readonly notification = inject(NotificationService);
  private readonly translate = inject(TranslateService);
  private readonly languageService = inject(LanguageService);

  private readonly orderId = toSignal(
    this.route.paramMap.pipe(map(params => {
      const id = params.get('id');
      return id ? Number(id) : undefined;
    })),
    {initialValue: undefined},
  );

  readonly orderStatusSeverity = orderStatusSeverity;
  readonly imageUrl = imageUrl;
  readonly localizedName = localizedName;

  readonly order = rxResource<AdminOrder, number | undefined>({
    params: () => this.orderId(),
    stream: ({params}) => this.adminOrderService.getOrder(params!),
  });

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

  readonly selectedStatus = signal<string | null>(null);
  readonly changingStatus = signal(false);

  constructor() {
    effect(() => {
      if (!this.order.hasValue()) return;
      const status = this.order.value().orderStatus;
      untracked(() => this.selectedStatus.set(status));
    });
  }

  changeStatus(): void {
    const status = this.selectedStatus();
    if (!status || !this.order.hasValue() || status === this.order.value().orderStatus) return;
    this.changingStatus.set(true);
    this.adminOrderService.changeStatus(this.order.value().id, status).subscribe({
      next: () => {
        this.changingStatus.set(false);
        this.order.reload();
        this.notification.success('common.saved', 'toast.statusChanged');
      },
      error: () => {
        this.changingStatus.set(false);
        this.notification.error('common.error', 'toast.statusChangeError');
      },
    });
  }

  back(): void {
    void this.router.navigate(['/admin/orders']);
  }
}
