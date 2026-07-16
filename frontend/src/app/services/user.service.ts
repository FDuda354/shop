import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {UploadResponse} from '../models/admin';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private readonly http = inject(HttpClient);

  private readonly baseUrl = `${environment.api.baseUrl}`;

  getProfileImage(userId: number): Observable<{image: string | null}> {
    return this.http.get<{image: string | null}>(this.baseUrl + `/profile/${userId}/image`);
  }

  updateProfileImage(image: string): Observable<{image: string | null}> {
    return this.http.put<{image: string | null}>(this.baseUrl + '/profile/image', {image});
  }

  uploadProfileImage(file: File): Observable<UploadResponse> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<UploadResponse>(this.baseUrl + '/profile/upload-image', form);
  }

  changePassword(password: string, repeatPassword: string): Observable<void> {
    return this.http.post<void>(this.baseUrl + '/profile/changePassword', {password, repeatPassword, hash: null});
  }
}
