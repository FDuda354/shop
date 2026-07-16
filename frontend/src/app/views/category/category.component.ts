import {Component, computed, DestroyRef, inject, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {PaginatorState} from 'primeng/paginator';
import {CategoryStore} from './category-store';
import {localizedDescription, localizedName} from '../../utils/localized';

@Component({
  selector: 'app-category',
  standalone: false,
  templateUrl: './category.component.html',
  styleUrl: './category.component.scss',
  providers: [CategoryStore],
})
export class CategoryComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  readonly store = inject(CategoryStore);

  readonly categoryName = computed(() => {
    const category = this.store.category();
    return category ? localizedName(category) : '';
  });

  readonly categoryDescription = computed(() => {
    const category = this.store.category();
    return category ? localizedDescription(category) : null;
  });

  ngOnInit(): void {
    // Router reuse'uje instancję komponentu przy zmianie :slug — stąd
    // subskrypcja na paramMap zamiast snapshotu.
    this.route.paramMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {
        const slug = params.get('slug');
        if (slug) this.store.setSlug(slug);
      });
  }

  onPageChange(event: PaginatorState) {
    this.store.setPage({page: event.page ?? 0, size: event.rows ?? 12});
  }
}
