import {computed, inject, Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, tap} from 'rxjs';
import {environment} from '../../environments/environment';
import {BasketProductRequest, BasketSummary} from '../models/basket';

const BASKET_ID_KEY = 'shop.basketId';

/**
 * HTTP + globalny stan koszyka. Id koszyka nadaje backend przy pierwszym
 * dodaniu produktu (PUT /basket/0) i jest trzymane w localStorage, żeby koszyk
 * przeżył odświeżenie strony. Licznik w headerze aktualizujemy po każdej
 * mutacji na podstawie zwróconego podsumowania koszyka.
 */
@Injectable({
  providedIn: 'root'
})
export class BasketService {

  private readonly http = inject(HttpClient);

  private readonly baseUrl = `${environment.api.baseUrl}`;

  private readonly _basketId = signal<number>(Number(localStorage.getItem(BASKET_ID_KEY) ?? 0));
  readonly basketId = this._basketId.asReadonly();

  private readonly _itemCount = signal<number>(0);
  readonly itemCount = this._itemCount.asReadonly();
  readonly hasItems = computed(() => this._itemCount() > 0);

  getBasket(id: number): Observable<BasketSummary> {
    return this.http.get<BasketSummary>(this.baseUrl + `/basket/${id}`).pipe(
      tap(basket => this.syncState(basket))
    );
  }

  addProduct(request: BasketProductRequest): Observable<BasketSummary> {
    return this.http.put<BasketSummary>(this.baseUrl + `/basket/${this._basketId()}`, request).pipe(
      tap(basket => this.syncState(basket))
    );
  }

  updateBasket(requests: BasketProductRequest[]): Observable<BasketSummary> {
    return this.http.put<BasketSummary>(this.baseUrl + `/basket/${this._basketId()}/update`, requests).pipe(
      tap(basket => this.syncState(basket))
    );
  }

  removeItem(basketItemId: number): Observable<void> {
    return this.http.delete<void>(this.baseUrl + `/basketItems/${basketItemId}`);
  }

  refreshCounter(): void {
    const id = this._basketId();
    if (id <= 0) {
      this._itemCount.set(0);
      return;
    }
    this.http.get<number>(this.baseUrl + `/basketItems/counter/${id}`).subscribe({
      next: count => this._itemCount.set(count ?? 0),
      error: () => this._itemCount.set(0),
    });
  }

  clearBasket(): void {
    this._basketId.set(0);
    this._itemCount.set(0);
    localStorage.removeItem(BASKET_ID_KEY);
  }

  private syncState(basket: BasketSummary): void {
    if (basket.id && basket.id !== this._basketId()) {
      this._basketId.set(basket.id);
      localStorage.setItem(BASKET_ID_KEY, String(basket.id));
    }
    this._itemCount.set(basket.items?.length ?? 0);
  }
}
