import {Component, inject} from '@angular/core';
import {Router} from '@angular/router';
import {rxResource} from '@angular/core/rxjs-interop';
import {BasketService} from '../../services/basket.service';
import {NotificationService} from '../../services/notification.service';
import {BasketItem, BasketSummary} from '../../models/basket';
import {imageUrl} from '../../utils/image-url';
import {localizedName} from '../../utils/localized';

@Component({
  selector: 'app-basket',
  standalone: false,
  templateUrl: './basket.component.html',
  styleUrl: './basket.component.scss',
})
export class BasketComponent {
  private readonly router = inject(Router);
  private readonly notification = inject(NotificationService);

  readonly basketService = inject(BasketService);
  readonly imageUrl = imageUrl;
  readonly localizedName = localizedName;

  readonly basket = rxResource<BasketSummary, number | undefined>({
    // basketId 0 = koszyk jeszcze nie istnieje — resource zostaje w idle,
    // a szablon pokazuje stan pusty.
    params: () => this.basketService.basketId() > 0 ? this.basketService.basketId() : undefined,
    stream: ({params}) => this.basketService.getBasket(params!),
  });

  readonly hasBasket = () => this.basketService.basketId() > 0;

  quantityChanged(item: BasketItem, quantity: number | null): void {
    if (!quantity || quantity < 1) {
      // Wyczyszczone pole — przeładowanie przywraca faktyczną ilość z koszyka.
      this.basket.reload();
      return;
    }
    this.basketService.updateBasket([{productId: item.product.id, quantity}]).subscribe({
      next: () => this.basket.reload(),
      error: () => this.notification.error('common.error', 'toast.basketUpdateError'),
    });
  }

  removeItem(item: BasketItem): void {
    this.basketService.removeItem(item.id).subscribe({
      next: () => {
        this.basket.reload();
        this.basketService.refreshCounter();
      },
      error: () => this.notification.error('common.error', 'toast.basketRemoveError'),
    });
  }

  goToCheckout(): void {
    void this.router.navigate(['/checkout']);
  }

  goToShop(): void {
    void this.router.navigate(['/']);
  }
}
