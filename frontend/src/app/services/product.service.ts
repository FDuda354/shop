import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {Page} from '../models/page';
import {Product, ProductDto} from '../models/product';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private readonly http = inject(HttpClient);

  private readonly baseUrl = `${environment.api.baseUrl}`;

  getProducts(page: number, size: number): Observable<Page<ProductDto>> {
    return this.http.get<Page<ProductDto>>(this.baseUrl + `/products?page=${page}&size=${size}`);
  }

  getProductBySlug(slug: string): Observable<Product> {
    return this.http.get<Product>(this.baseUrl + `/product/${slug}`);
  }
}
