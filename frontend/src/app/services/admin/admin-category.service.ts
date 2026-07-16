import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {AdminCategory, AdminCategoryRequest} from '../../models/admin';

@Injectable({
  providedIn: 'root'
})
export class AdminCategoryService {

  private readonly http = inject(HttpClient);

  private readonly baseUrl = `${environment.api.baseUrl}`;

  getCategories(): Observable<AdminCategory[]> {
    return this.http.get<AdminCategory[]>(this.baseUrl + '/admin/categories');
  }

  addCategory(category: AdminCategoryRequest): Observable<AdminCategory> {
    return this.http.post<AdminCategory>(this.baseUrl + '/admin/category', category);
  }

  updateCategory(id: number, category: AdminCategoryRequest): Observable<AdminCategory> {
    return this.http.put<AdminCategory>(this.baseUrl + `/admin/category/${id}`, category);
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(this.baseUrl + `/admin/category/${id}`);
  }
}
