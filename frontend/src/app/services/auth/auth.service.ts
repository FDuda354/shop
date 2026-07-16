import {HttpClient} from '@angular/common/http';
import {inject, Injectable, signal} from '@angular/core';
import {map, Observable, of, tap} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {Router} from '@angular/router';
import {environment} from '../../../environments/environment';
import {UserDto} from '../../models/user';

export interface AuthRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  confirmPassword: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly baseUrl = environment.api.baseUrl;

  private readonly _currentUser = signal<UserDto | null>(null);
  readonly currentUser = this._currentUser.asReadonly();

  login(authReq: AuthRequest): Observable<UserDto> {
    return this.http.post<UserDto>(`${this.baseUrl}/api/auth/login`, authReq).pipe(
      tap(user => this._currentUser.set(user))
    );
  }

  register(registerReq: RegisterRequest): Observable<UserDto> {
    return this.http.post<UserDto>(`${this.baseUrl}/api/auth/register`, registerReq).pipe(
      tap(user => this._currentUser.set(user))
    );
  }

  clear(): void {
    this._currentUser.set(null);
  }

  loadMe(): Observable<UserDto | null> {
    const cached = this._currentUser();
    if (cached) return of(cached);

    return this.http.get<UserDto>(`${this.baseUrl}/api/auth/me`).pipe(
      map(user => {
        this._currentUser.set(user);
        return user;
      }),
      catchError(() => {
        this._currentUser.set(null);
        return of<UserDto | null>(null);
      })
    );
  }

  logout() {
    this.http.post<void>(`${this.baseUrl}/api/auth/logout`, {}).subscribe({
      next: () => {
        this.clear();
        void this.router.navigate(['/']);
      },
    });
  }
}
