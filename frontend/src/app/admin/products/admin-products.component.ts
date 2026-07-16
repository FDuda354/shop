import {Component, computed, inject} from '@angular/core';
import {Router} from '@angular/router';
import {rxResource} from '@angular/core/rxjs-interop';
import {ConfirmationService} from 'primeng/api';
import {PaginatorState} from 'primeng/paginator';
import {TranslateService} from '@ngx-translate/core';
import {AdminProductsStore} from './admin-products-store';
import {AdminCategory, AdminProduct} from '../../models/admin';
import {AdminProductService} from '../../services/admin/admin-product.service';
import {AdminCategoryService} from '../../services/admin/admin-category.service';
import {NotificationService} from '../../services/notification.service';
import {imageUrl} from '../../utils/image-url';
import {localizedName} from '../../utils/localized';

@Component({
  selector: 'app-admin-products',
  standalone: false,
  templateUrl: './admin-products.component.html',
  styleUrl: './admin-products.component.scss',
  providers: [AdminProductsStore, ConfirmationService],
})
export class AdminProductsComponent {
  private readonly router = inject(Router);
  private readonly adminProductService = inject(AdminProductService);
  private readonly adminCategoryService = inject(AdminCategoryService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly notification = inject(NotificationService);
  private readonly translate = inject(TranslateService);

  readonly store = inject(AdminProductsStore);
  readonly imageUrl = imageUrl;
  readonly localizedName = localizedName;

  private readonly categories = rxResource<AdminCategory[], undefined>({
    stream: () => this.adminCategoryService.getCategories(),
  });

  readonly categoryNames = computed<Record<number, string>>(() => {
    if (!this.categories.hasValue()) return {};
    return Object.fromEntries(this.categories.value().map(category => [category.id, localizedName(category)]));
  });

  onPageChange(event: PaginatorState) {
    this.store.setPage({page: event.page ?? 0, size: event.rows ?? 10});
  }

  addProduct(): void {
    void this.router.navigate(['/admin/product/new']);
  }

  editProduct(product: AdminProduct): void {
    void this.router.navigate(['/admin/product', product.id]);
  }

  deleteProduct(product: AdminProduct): void {
    this.confirmationService.confirm({
      message: this.translate.instant('admin.products.deleteMessage', {name: localizedName(product)}),
      header: this.translate.instant('admin.products.deleteHeader'),
      icon: 'pi pi-trash',
      acceptLabel: this.translate.instant('common.yes'),
      rejectLabel: this.translate.instant('common.no'),
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.adminProductService.deleteProduct(product.id).subscribe({
          next: () => {
            this.store.data.reload();
            this.notification.success('toast.productDeletedTitle', 'toast.productDeleted', {name: localizedName(product)});
          },
          error: () => this.notification.error('common.error', 'toast.productDeleteError'),
        });
      },
      key: 'deleteConfirmDialog',
    });
  }
}
