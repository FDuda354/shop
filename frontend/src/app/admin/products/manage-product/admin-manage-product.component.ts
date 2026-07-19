import {Component, computed, effect, inject, Injector, signal, untracked} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {rxResource, toSignal} from '@angular/core/rxjs-interop';
import {map} from 'rxjs/operators';
import {firstValueFrom} from 'rxjs';
import {form, maxLength, min, minLength, required, submit} from '@angular/forms/signals';
import {AdminProductService} from '../../../services/admin/admin-product.service';
import {AdminCategoryService} from '../../../services/admin/admin-category.service';
import {NotificationService} from '../../../services/notification.service';
import {AdminCategory, AdminProduct} from '../../../models/admin';
import {imageUrl} from '../../../utils/image-url';
import {localizedName} from '../../../utils/localized';
import {validationMessages} from '../../../utils/validation-message';
import {nameAndSlugRules} from '../../../utils/form-rules';

@Component({
  selector: 'app-admin-manage-product',
  standalone: false,
  templateUrl: './admin-manage-product.component.html',
  styleUrl: './admin-manage-product.component.scss',
})
export class AdminManageProductComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly adminProductService = inject(AdminProductService);
  private readonly adminCategoryService = inject(AdminCategoryService);
  private readonly notification = inject(NotificationService);
  private readonly injector = inject(Injector);
  private readonly msg = validationMessages();

  private readonly productId = toSignal(
    this.route.paramMap.pipe(map(params => {
      const id = params.get('id');
      return id ? Number(id) : undefined;
    })),
    {initialValue: undefined},
  );

  readonly isEdit = computed(() => this.productId() != null);
  readonly uploadingImage = signal(false);
  readonly imageUrl = imageUrl;

  readonly currencies = ['PLN', 'USD', 'EUR'];

  readonly categories = rxResource<AdminCategory[], undefined>({
    stream: () => this.adminCategoryService.getCategories(),
  });

  // localizedName czyta sygnał appLang, więc computed przelicza się po zmianie języka.
  readonly categoryOptions = computed(() => {
    if (!this.categories.hasValue()) return [];
    return this.categories.value().map(category => ({...category, label: localizedName(category)}));
  });

  readonly product = rxResource<AdminProduct, number | undefined>({
    params: () => this.productId(),
    stream: ({params}) => this.adminProductService.getProduct(params!),
  });

  readonly productModel = signal({
    name: '',
    slug: '',
    description: '',
    fullDescription: '',
    nameEn: '',
    descriptionEn: '',
    fullDescriptionEn: '',
    price: null as number | null,
    image: null as string | null,
  });

  readonly categoryId = signal<number | null>(null);
  readonly currency = signal<string>('PLN');

  readonly productForm = form(this.productModel, (f) => {
    nameAndSlugRules(f.name, f.slug, this.msg);
    required(f.description, {message: this.msg('validation.descriptionRequired')});
    minLength(f.description, 3, {message: this.msg('validation.descriptionMinLength')});
    maxLength(f.description, 100, {message: this.msg('validation.descriptionMaxLength')});
    // Wersja angielska opcjonalna — tylko limity długości.
    maxLength(f.nameEn, 255, {message: this.msg('validation.nameMaxLength')});
    maxLength(f.descriptionEn, 100, {message: this.msg('validation.descriptionMaxLength')});
    required(f.price, {message: this.msg('validation.priceRequired')});
    min(f.price, 0, {message: this.msg('validation.priceNegative')});
  }, {injector: this.injector});

  constructor() {
    // Tryb edycji: po załadowaniu produktu wypełnij formularz.
    effect(() => {
      if (!this.product.hasValue()) return;
      const product = this.product.value();
      untracked(() => {
        this.productModel.set({
          name: product.name,
          slug: product.slug,
          description: product.description,
          fullDescription: product.fullDescription ?? '',
          nameEn: product.nameEn ?? '',
          descriptionEn: product.descriptionEn ?? '',
          fullDescriptionEn: product.fullDescriptionEn ?? '',
          price: product.price,
          image: product.image,
        });
        this.categoryId.set(product.categoryId);
        this.currency.set(product.currency);
      });
    });
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.uploadingImage.set(true);
    this.adminProductService.uploadImage(file).subscribe({
      next: response => {
        this.uploadingImage.set(false);
        this.productModel.update(model => ({...model, image: response.fileName}));
        this.notification.success('toast.imageUploadedTitle', 'toast.imageUploaded', {name: response.fileName});
      },
      error: () => {
        this.uploadingImage.set(false);
        this.notification.error('common.error', 'toast.imageUploadError');
      },
    });
    input.value = '';
  }

  async onSubmit() {
    if (this.categoryId() == null) {
      this.notification.error('toast.missingDeliveryTitle', 'toast.missingCategoryDetail');
      return;
    }
    await submit(this.productForm, async () => {
      const model = this.productModel();
      const request = {
        name: model.name,
        slug: model.slug,
        description: model.description,
        fullDescription: model.fullDescription || null,
        // trim(): wartość z samych spacji ma być brakiem tłumaczenia (null),
        // inaczej wygrywałaby z fallbackiem do polskiej wersji.
        nameEn: model.nameEn.trim() || null,
        descriptionEn: model.descriptionEn.trim() || null,
        fullDescriptionEn: model.fullDescriptionEn.trim() || null,
        categoryId: this.categoryId(),
        price: model.price,
        currency: this.currency(),
        image: model.image,
      };
      try {
        if (this.isEdit()) {
          await firstValueFrom(this.adminProductService.updateProduct(this.productId()!, request));
          this.notification.success('common.saved', 'toast.productSaved');
        } else {
          await firstValueFrom(this.adminProductService.addProduct(request));
          this.notification.success('toast.productAddedTitle', 'toast.productAdded');
        }
        void this.router.navigate(['/admin/products']);
      } catch (_err) {
        this.notification.error('common.error', 'toast.productSaveError');
      }
      return undefined;
    });
  }

  cancel(): void {
    void this.router.navigate(['/admin/products']);
  }
}
