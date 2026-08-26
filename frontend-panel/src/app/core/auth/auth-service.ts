import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse } from './auth.models';

const TOKEN_KEY = 'mm_token';
const ROLE_KEY = 'mm_role';
const USER_ID_KEY = 'mm_user_id';
const USER_NAME_KEY = 'mm_user_name';
const EXPIRES_AT_KEY = 'mm_expires_at';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/auth/login`, credentials)
      .pipe(
        tap((res) => {
          const expiresAt = Date.now() + res.expiresIn * 1000;
          localStorage.setItem(TOKEN_KEY, res.token);
          localStorage.setItem(ROLE_KEY, res.role);
          localStorage.setItem(USER_ID_KEY, res.userId.toString());
          localStorage.setItem(USER_NAME_KEY, res.name);
          localStorage.setItem(EXPIRES_AT_KEY, expiresAt.toString());
        }),
      );
  }

  isAuthenticated(): boolean {
    const token = localStorage.getItem(TOKEN_KEY);
    const expiresAt = localStorage.getItem(EXPIRES_AT_KEY);
    const expiration = Number(expiresAt);

    if (
      !token ||
      !expiresAt ||
      !Number.isFinite(expiration) ||
      Date.now() >= expiration
    ) {
      this.logout();
      return false;
    }
    return true;
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  getRole(): string | null {
    return localStorage.getItem(ROLE_KEY);
  }

  isAdmin(): boolean {
    return this.getRole() === 'SYSTEM_ADMIN';
  }

  getUserName(): string | null {
    return localStorage.getItem(USER_NAME_KEY);
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ROLE_KEY);
    localStorage.removeItem(USER_ID_KEY);
    localStorage.removeItem(USER_NAME_KEY);
    localStorage.removeItem(EXPIRES_AT_KEY);
  }
}
