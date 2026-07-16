import {Component, inject, Injector, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {firstValueFrom} from 'rxjs';
import {ConfirmationService} from 'primeng/api';
import {form, maxLength, minLength, required, submit} from '@angular/forms/signals';
import {TranslateService} from '@ngx-translate/core';
import {AdminCategoryService} from '../../services/admin/admin-category.service';
import {NotificationService} from '../../services/notification.service';
import {validationMessages} from '../../utils/validation-message';
import {AdminCategory} from '../../models/admin';
import {localizedDescription, localizedName} from '../../utils/localized';

@Component({
  selector: 'app-admin-categories',
  standalone: false,
  templateUrl: './admin-categories.component.html',
  styleUrl: './admin-categories.component.scss',
  // MessageService celowo z roota — patrz globalny <p-toast> w AppComponent.
  providers: [ConfirmationService],
})
export class AdminCategoriesComponent {
  private readonly adminCategoryService = inject(AdminCategoryService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly notification = inject(NotificationService);
  private readonly translate = inject(TranslateService);
  private readonly injector = inject(Injector);
  private readonly msg = validationMessages();

  readonly localizedName = localizedName;
  readonly localizedDescription = localizedDescription;

  readonly categories = rxResource<AdminCategory[], undefined>({
    stream: () => this.adminCategoryService.getCategories(),
  });

  readonly drawerVisible = signal(false);
  readonly editedId = signal<number | null>(null);

  readonly categoryModel = signal({name: '', slug: '', description: '', nameEn: '', descriptionEn: ''});

  // Signal forms w 22.0 nie mają publicznego resetu stanu touched — świeży
  // formularz przy każdym otwarciu drawera załatwia problem "starych" błędów.
  readonly categoryForm = signal(this.buildForm());

  private buildForm() {
    return form(this.categoryModel, (f) => {
      required(f.name, {message: this.msg('validation.nameRequired')});
      minLength(f.name, 3, {message: this.msg('validation.nameMinLength')});
      maxLength(f.name, 255, {message: this.msg('validation.nameMaxLength')});
      required(f.slug, {message: this.msg('validation.slugRequired')});
      minLength(f.slug, 3, {message: this.msg('validation.slugMinLength')});
      maxLength(f.slug, 255, {message: this.msg('validation.slugMaxLength')});
      // Wersja angielska opcjonalna — tylko limit długości.
      maxLength(f.nameEn, 255, {message: this.msg('validation.nameMaxLength')});
    }, {injector: this.injector});
  }

  openAdd(): void {
    this.editedId.set(null);
    this.categoryModel.set({name: '', slug: '', description: '', nameEn: '', descriptionEn: ''});
    this.categoryForm.set(this.buildForm());
    this.drawerVisible.set(true);
  }

  openEdit(category: AdminCategory): void {
    this.editedId.set(category.id);
    this.categoryModel.set({
      name: category.name,
      slug: category.slug,
      description: category.description ?? '',
      nameEn: category.nameEn ?? '',
      descriptionEn: category.descriptionEn ?? '',
    });
    this.categoryForm.set(this.buildForm());
    this.drawerVisible.set(true);
  }

  async onSubmit() {
    await submit(this.categoryForm(), async () => {
      const model = this.categoryModel();
      const request = {
        name: model.name,
        slug: model.slug,
        description: model.description || null,
        // trim(): wartość z samych spacji ma być brakiem tłumaczenia (null),
        // inaczej wygrywałaby z fallbackiem do polskiej wersji.
        nameEn: model.nameEn.trim() || null,
        descriptionEn: model.descriptionEn.trim() || null,
      };
      try {
        if (this.editedId() != null) {
          await firstValueFrom(this.adminCategoryService.updateCategory(this.editedId()!, request));
          this.notification.success('common.saved', 'toast.categorySaved');
        } else {
          await firstValueFrom(this.adminCategoryService.addCategory(request));
          this.notification.success('toast.productAddedTitle', 'toast.categoryAdded');
        }
        this.drawerVisible.set(false);
        this.categories.reload();
      } catch (_err) {
        this.notification.error('common.error', 'toast.categorySaveError');
      }
      return undefined;
    });
  }

  deleteCategory(category: AdminCategory): void {
    this.confirmationService.confirm({
      message: this.translate.instant('admin.categories.deleteMessage', {name: localizedName(category)}),
      header: this.translate.instant('admin.categories.deleteHeader'),
      icon: 'pi pi-trash',
      acceptLabel: this.translate.instant('common.yes'),
      rejectLabel: this.translate.instant('common.no'),
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.adminCategoryService.deleteCategory(category.id).subscribe({
          next: () => {
            this.categories.reload();
            this.notification.success('toast.productDeletedTitle', 'toast.categoryDeleted', {name: localizedName(category)});
          },
          error: () => this.notification.error('common.error', 'toast.categoryDeleteError'),
        });
      },
      key: 'deleteConfirmDialog',
    });
  }
}
