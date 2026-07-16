import {computed, inject, Injectable, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {CategoryService} from '../../services/category.service';
import {deepEqual, PageRequest} from '../../models/page';
import {CategoryProducts} from '../../models/category';

interface CategoryRequest {
  slug: string;
  page: PageRequest;
}

/**
 * Store widoku kategorii: filtr (slug z routingu) + stronicowanie.
 * Zmiana kategorii resetuje stronę na 0.
 */
@Injectable()
export class CategoryStore {
  private readonly categoryService = inject(CategoryService);

  private readonly _slug = signal<string | undefined>(undefined);
  private readonly _page = signal<PageRequest>({page: 0, size: 12});

  private readonly request = computed<CategoryRequest | undefined>(() => {
    const slug = this._slug();
    return slug ? {slug, page: this._page()} : undefined;
  }, {equal: deepEqual});

  readonly data = rxResource<CategoryProducts, CategoryRequest | undefined>({
    params: () => this.request(),
    stream: ({params}) => this.categoryService.getCategoryProducts(params!.slug, params!.page.page, params!.page.size),
  });

  readonly category = computed(() => this.data.hasValue() ? this.data.value().category : null);

  readonly pageResp = computed(() => this.data.hasValue()
    ? this.data.value().productsPage
    : {content: [], totalElements: 0, totalPages: 0, numberOfElements: 0, number: 0, size: 12});

  setSlug(slug: string) {
    this._slug.set(slug);
    const cur = this._page();
    if (cur.page !== 0) this._page.set({...cur, page: 0});
  }

  setPage(page: PageRequest) {
    this._page.set(page);
  }
}
