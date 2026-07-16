import {Component, inject, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {rxResource, toSignal} from '@angular/core/rxjs-interop';
import {map} from 'rxjs/operators';
import {ProductService} from '../../services/product.service';
import {BasketService} from '../../services/basket.service';
import {NotificationService} from '../../services/notification.service';
import {Product} from '../../models/product';
import {imageUrl} from '../../utils/image-url';
import {localizedDescription, localizedFullDescription, localizedName} from '../../utils/localized';

@Component({
  selector: 'app-product-details',
  standalone: false,
  templateUrl: './product-details.component.html',
  styleUrl: './product-details.component.scss',
})
export class ProductDetailsComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly productService = inject(ProductService);
  private readonly basketService = inject(BasketService);
  private readonly notification = inject(NotificationService);

  private readonly slug = toSignal(this.route.paramMap.pipe(map(params => params.get('slug'))), {initialValue: null});

  readonly quantity = signal(1);
  readonly imageUrl = imageUrl;
  readonly localizedName = localizedName;
  readonly localizedDescription = localizedDescription;
  readonly localizedFullDescription = localizedFullDescription;

  readonly product = rxResource<Product, string | undefined>({
    params: () => this.slug() ?? undefined,
    stream: ({params}) => this.productService.getProductBySlug(params!),
  });

  addToBasket(): void {
    if (!this.product.hasValue()) return;
    const product = this.product.value();
    this.basketService.addProduct({productId: product.id, quantity: this.quantity()}).subscribe({
      next: () => this.notification.success('toast.addedToBasket', 'toast.addedToBasketDetail',
        {name: localizedName(product), quantity: this.quantity()}),
      error: () => this.notification.error('common.error', 'toast.basketAddError'),
    });
  }

  reviewAdded(): void {
    this.product.reload();
  }
}
