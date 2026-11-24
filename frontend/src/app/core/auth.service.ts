import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, map, tap } from 'rxjs';

import { LoginRequest, LoginResponse, Role, SignupRequest, UserInfo } from './models';

interface Session {
  token: string;
  expiresAt: string;
  user: UserInfo;
}

const STORAGE_KEY = 'orbiterp.session';

/** Signs in and out, and keeps the JWT and the current user for the rest of the app. */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly sessionSubject = new BehaviorSubject<Session | null>(this.restore());

  readonly user$: Observable<UserInfo | null> = this.sessionSubject.pipe(map((s) => s?.user ?? null));

  get token(): string | null {
    return this.validSession()?.token ?? null;
  }

  get user(): UserInfo | null {
    return this.validSession()?.user ?? null;
  }

  isLoggedIn(): boolean {
    return this.validSession() !== null;
  }

  /** True when the user has one of the roles, or when no role is required. */
  hasAnyRole(roles: readonly Role[]): boolean {
    if (roles.length === 0) {
      return true;
    }
    const user = this.user;
    return !!user && roles.some((role) => user.roles.includes(role));
  }

  login(request: LoginRequest): Observable<UserInfo> {
    return this.http.post<LoginResponse>('/api/auth/login', request).pipe(
      tap((response) => this.store(response)),
      map((response) => response.user),
    );
  }

  signup(request: SignupRequest): Observable<UserInfo> {
    return this.http.post<LoginResponse>('/api/auth/signup', request).pipe(
      tap((response) => this.store(response)),
      map((response) => response.user),
    );
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.sessionSubject.next(null);
    void this.router.navigate(['/login']);
  }

  private validSession(): Session | null {
    const session = this.sessionSubject.value;
    return session && new Date(session.expiresAt).getTime() > Date.now() ? session : null;
  }

  private store(response: LoginResponse): void {
    const session: Session = { token: response.token, expiresAt: response.expiresAt, user: response.user };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    this.sessionSubject.next(session);
  }

  private restore(): Session | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as Session) : null;
    } catch {
      return null;
    }
  }
}
