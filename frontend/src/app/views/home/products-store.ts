import {computed, inject, Injectable, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {ProductService} from '../../services/product.service';
import {deepEqual, emptyPage, Page, PageRequest} from '../../models/page';
import {ProductDto} from '../../models/product';

/**
 * Store listy produktów (wzorzec z hercu-pulpit): sygnały stronicowania ->
 * computed request z deepEqual -> rxResource, który refetchuje sam, gdy
 * request faktycznie się zmieni. Dostarczany per-komponent (providers),
 * więc stan resetuje się przy nawigacji.
 */
@Injectable()
export class ProductsStore {
  private readonly productService = inject(ProductService);

  private readonly _page = signal<PageRequest>({page: 0, size: 12});

  private readonly request = computed(() => this._page(), {equal: deepEqual});

  readonly data = rxResource<Page<ProductDto>, PageRequest>({
    params: () => this.request(),
    stream: ({params}) => this.productService.getProducts(params.page, params.size),
    defaultValue: emptyPage<ProductDto>(12),
  });

  readonly pageResp = computed(() => this.data.hasValue() ? this.data.value() : emptyPage<ProductDto>(12));

  setPage(page: PageRequest) {
    this._page.set(page);
  }
}
