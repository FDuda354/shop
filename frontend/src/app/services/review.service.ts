import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {Review, ReviewRequest} from '../models/product';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {

  private readonly http = inject(HttpClient);

  private readonly baseUrl = `${environment.api.baseUrl}`;

  addReview(review: ReviewRequest): Observable<Review> {
    return this.http.post<Review>(this.baseUrl + '/review', review);
  }
}
