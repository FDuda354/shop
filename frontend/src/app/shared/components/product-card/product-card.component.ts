import {Component, inject, input, output} from '@angular/core';
import {Router} from '@angular/router';
import {ProductDto} from '../../../models/product';
import {BasketService} from '../../../services/basket.service';
import {NotificationService} from '../../../services/notification.service';
import {imageUrl} from '../../../utils/image-url';
import {localizedDescription, localizedName} from '../../../utils/localized';

/**
 * Karta produktu współdzielona przez stronę główną i widok kategorii.
 */
@Component({
  selector: 'app-product-card',
  standalone: false,
  templateUrl: './product-card.component.html',
  styleUrl: './product-card.component.scss',
})
export class ProductCardComponent {
  private readonly router = inject(Router);
  private readonly basketService = inject(BasketService);
  private readonly notification = inject(NotificationService);

  product = input.required<ProductDto>();
  addedToBasket = output<void>();

  readonly imageUrl = imageUrl;
  readonly localizedName = localizedName;
  readonly localizedDescription = localizedDescription;

  goToDetails(): void {
    void this.router.navigate(['/product', this.product().slug]);
  }

  addToBasket(event: Event): void {
    event.stopPropagation();
    this.basketService.addProduct({productId: this.product().id, quantity: 1}).subscribe({
      next: () => {
        this.notification.success('toast.addedToBasket', 'toast.addedToBasketDetail',
          {name: localizedName(this.product()), quantity: 1});
        this.addedToBasket.emit();
      },
      error: () => this.notification.error('common.error', 'toast.basketAddError'),
    });
  }
}
