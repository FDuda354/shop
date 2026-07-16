import {computed, inject, Injectable, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {AdminOrderService} from '../../services/admin/admin-order.service';
import {deepEqual, emptyPage, Page, PageRequest} from '../../models/page';
import {AdminOrderListRow} from '../../models/admin';

@Injectable()
export class AdminOrdersStore {
  private readonly adminOrderService = inject(AdminOrderService);

  private readonly _page = signal<PageRequest>({page: 0, size: 10});

  private readonly request = computed(() => this._page(), {equal: deepEqual});

  readonly data = rxResource<Page<AdminOrderListRow>, PageRequest>({
    params: () => this.request(),
    stream: ({params}) => this.adminOrderService.getOrders(params.page, params.size),
    defaultValue: emptyPage<AdminOrderListRow>(10),
  });

  readonly pageResp = computed(() => this.data.hasValue() ? this.data.value() : emptyPage<AdminOrderListRow>(10));

  setPage(page: PageRequest) {
    this._page.set(page);
  }
}
