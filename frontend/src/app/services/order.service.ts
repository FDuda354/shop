import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {InitOrder, OrderForUser, OrderRequest, OrderSummary} from '../models/order';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private readonly http = inject(HttpClient);

  private readonly baseUrl = `${environment.api.baseUrl}`;

  initOrder(): Observable<InitOrder> {
    return this.http.get<InitOrder>(this.baseUrl + '/order/initOrder');
  }

  createOrder(order: OrderRequest): Observable<OrderSummary> {
    return this.http.post<OrderSummary>(this.baseUrl + '/order', order);
  }

  getMyOrders(): Observable<OrderForUser[]> {
    return this.http.get<OrderForUser[]>(this.baseUrl + '/orders');
  }
}
