import {Component, DestroyRef, inject} from '@angular/core';
import {Router} from '@angular/router';
import {rxResource} from '@angular/core/rxjs-interop';
import {catchError, debounceTime, EMPTY, groupBy, mergeMap, Subject, switchMap} from 'rxjs';
import {BasketService} from '../../services/basket.service';
import {NotificationService} from '../../services/notification.service';
import {BasketItem, BasketProductRequest, BasketSummary} from '../../models/basket';
import {imageUrl} from '../../utils/image-url';
import {localizedName} from '../../utils/localized';

const QUANTITY_INPUT_SETTLE_MS = 400;

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

  private readonly quantityInput = new Subject<BasketProductRequest>();

  constructor() {
    inject(DestroyRef).onDestroy(() => this.quantityInput.complete());
    this.quantityInput.pipe(
      groupBy(request => request.productId),
      mergeMap(productInput => productInput.pipe(
        debounceTime(QUANTITY_INPUT_SETTLE_MS),
        switchMap(request => this.basketService.updateBasket([request]).pipe(
          catchError(() => {
            this.notification.error('common.error', 'toast.basketUpdateError');
            return EMPTY;
          }),
        )),
      )),
    ).subscribe(basket => this.basket.set(basket));
  }

  quantityChanged(item: BasketItem, quantity: number | null): void {
    if (!quantity || quantity < 1) {
      return;
    }
    this.quantityInput.next({productId: item.product.id, quantity});
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
