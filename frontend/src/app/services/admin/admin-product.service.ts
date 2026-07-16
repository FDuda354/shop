import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {Page} from '../../models/page';
import {AdminProduct, AdminProductRequest, UploadResponse} from '../../models/admin';

@Injectable({
  providedIn: 'root'
})
export class AdminProductService {

  private readonly http = inject(HttpClient);

  private readonly baseUrl = `${environment.api.baseUrl}`;

  getProducts(page: number, size: number): Observable<Page<AdminProduct>> {
    return this.http.get<Page<AdminProduct>>(this.baseUrl + `/admin/products?page=${page}&size=${size}`);
  }

  getProduct(id: number): Observable<AdminProduct> {
    return this.http.get<AdminProduct>(this.baseUrl + `/admin/product/${id}`);
  }

  addProduct(product: AdminProductRequest): Observable<AdminProduct> {
    return this.http.post<AdminProduct>(this.baseUrl + '/admin/product', product);
  }

  updateProduct(id: number, product: AdminProductRequest): Observable<AdminProduct> {
    return this.http.put<AdminProduct>(this.baseUrl + `/admin/product/${id}`, product);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(this.baseUrl + `/admin/product/${id}`);
  }

  uploadImage(file: File): Observable<UploadResponse> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<UploadResponse>(this.baseUrl + '/admin/product/upload-image', form);
  }
}
