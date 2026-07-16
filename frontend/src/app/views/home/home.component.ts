import {Component, inject} from '@angular/core';
import {PaginatorState} from 'primeng/paginator';
import {ProductsStore} from './products-store';

@Component({
  selector: 'app-home',
  standalone: false,
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  providers: [ProductsStore],
})
export class HomeComponent {

  readonly store = inject(ProductsStore);

  onPageChange(event: PaginatorState) {
    this.store.setPage({page: event.page ?? 0, size: event.rows ?? 12});
  }
}
