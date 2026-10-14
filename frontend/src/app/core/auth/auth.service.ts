import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { LoginResponse, User } from '../models';

export const SESSION_KEY = 'orderhub.session';

interface Session {
  token: string;
  expiresAt: string;
  user: User;
}

/**
 * Holds the authenticated session as a signal so every component reacts to login/logout.
 * The session is persisted in localStorage to survive page reloads and is discarded
 * as soon as the token expires.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly session = signal<Session | null>(this.restore());

  readonly user = computed(() => this.session()?.user ?? null);
  readonly isAuthenticated = computed(() => this.session() !== null);
  readonly isAdmin = computed(() => this.session()?.user.role === 'ADMIN');

  get token(): string | null {
    const current = this.session();
    if (current && isExpired(current.expiresAt)) {
      this.clear();
      return null;
    }
    return current?.token ?? null;
  }

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', { email, password }).pipe(
      tap((res) => {
        const session: Session = {
          token: res.accessToken,
          expiresAt: res.expiresAt,
          user: res.user,
        };
        localStorage.setItem(SESSION_KEY, JSON.stringify(session));
        this.session.set(session);
      }),
    );
  }

  logout(): void {
    this.clear();
    void this.router.navigate(['/login']);
  }

  private clear(): void {
    localStorage.removeItem(SESSION_KEY);
    this.session.set(null);
  }

  private restore(): Session | null {
    try {
      const raw = localStorage.getItem(SESSION_KEY);
      if (!raw) {
        return null;
      }
      const session = JSON.parse(raw) as Session;
      if (!session.token || isExpired(session.expiresAt)) {
        localStorage.removeItem(SESSION_KEY);
        return null;
      }
      return session;
    } catch {
      return null;
    }
  }
}

function isExpired(expiresAt: string): boolean {
  return new Date(expiresAt).getTime() <= Date.now();
}
