import {computed, inject, Injectable, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {AdminProductService} from '../../services/admin/admin-product.service';
import {deepEqual, emptyPage, Page, PageRequest} from '../../models/page';
import {AdminProduct} from '../../models/admin';

@Injectable()
export class AdminProductsStore {
  private readonly adminProductService = inject(AdminProductService);

  private readonly _page = signal<PageRequest>({page: 0, size: 10});

  private readonly request = computed(() => this._page(), {equal: deepEqual});

  readonly data = rxResource<Page<AdminProduct>, PageRequest>({
    params: () => this.request(),
    stream: ({params}) => this.adminProductService.getProducts(params.page, params.size),
    defaultValue: emptyPage<AdminProduct>(10),
  });

  readonly pageResp = computed(() => this.data.hasValue() ? this.data.value() : emptyPage<AdminProduct>(10));

  setPage(page: PageRequest) {
    this._page.set(page);
  }
}
