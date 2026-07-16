import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {Category, CategoryProducts} from '../models/category';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {

  private readonly http = inject(HttpClient);

  private readonly baseUrl = `${environment.api.baseUrl}`;

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(this.baseUrl + '/categories');
  }

  getCategoryProducts(slug: string, page: number, size: number): Observable<CategoryProducts> {
    return this.http.get<CategoryProducts>(this.baseUrl + `/category/${slug}/products?page=${page}&size=${size}`);
  }
}
