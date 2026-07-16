import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {Page} from '../../models/page';
import {AdminInitData, AdminOrder, AdminOrderListRow, AdminOrderStats} from '../../models/admin';

@Injectable({
  providedIn: 'root'
})
export class AdminOrderService {

  private readonly http = inject(HttpClient);

  private readonly baseUrl = `${environment.api.baseUrl}`;

  getOrders(page: number, size: number): Observable<Page<AdminOrderListRow>> {
    return this.http.get<Page<AdminOrderListRow>>(this.baseUrl + `/admin/orders?page=${page}&size=${size}`);
  }

  getOrder(id: number): Observable<AdminOrder> {
    return this.http.get<AdminOrder>(this.baseUrl + `/admin/order/${id}`);
  }

  changeStatus(id: number, orderStatus: string): Observable<void> {
    return this.http.patch<void>(this.baseUrl + `/admin/order/${id}`, {orderStatus});
  }

  getInitData(): Observable<AdminInitData> {
    return this.http.get<AdminInitData>(this.baseUrl + '/admin/orders/initData');
  }

  getStats(): Observable<AdminOrderStats> {
    return this.http.get<AdminOrderStats>(this.baseUrl + '/admin/orders/stats');
  }

  exportUrl(from: string, to: string, orderStatus: string): string {
    return this.baseUrl + `/admin/orders/export?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&orderStatus=${orderStatus}`;
  }
}
