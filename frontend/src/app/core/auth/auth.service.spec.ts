import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { LoginResponse } from '../models';
import { AuthService, SESSION_KEY } from './auth.service';

function loginResponse(overrides: Partial<LoginResponse> = {}): LoginResponse {
  return {
    accessToken: 'token-123',
    tokenType: 'Bearer',
    expiresAt: new Date(Date.now() + 60_000).toISOString(),
    user: { id: 1, email: 'admin@orderhub.dev', fullName: 'Ada Admin', role: 'ADMIN' },
    ...overrides,
  };
}

describe('AuthService', () => {
  let http: HttpTestingController;

  function setup(): AuthService {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    http = TestBed.inject(HttpTestingController);
    return TestBed.inject(AuthService);
  }

  beforeEach(() => localStorage.clear());
  afterEach(() => http.verify());

  it('stores the session after a successful login', () => {
    const auth = setup();

    auth.login('admin@orderhub.dev', 'secret').subscribe();
    const req = http.expectOne('/api/auth/login');
    expect(req.request.body).toEqual({ email: 'admin@orderhub.dev', password: 'secret' });
    req.flush(loginResponse());

    expect(auth.isAuthenticated()).toBe(true);
    expect(auth.isAdmin()).toBe(true);
    expect(auth.token).toBe('token-123');
    expect(localStorage.getItem(SESSION_KEY)).toContain('token-123');
  });

  it('restores a valid session from storage', () => {
    const res = loginResponse({ accessToken: 'persisted' });
    localStorage.setItem(
      SESSION_KEY,
      JSON.stringify({ token: res.accessToken, expiresAt: res.expiresAt, user: res.user }),
    );

    const auth = setup();

    expect(auth.token).toBe('persisted');
    expect(auth.user()?.fullName).toBe('Ada Admin');
  });

  it('discards an expired session', () => {
    const res = loginResponse({ expiresAt: new Date(Date.now() - 1000).toISOString() });
    localStorage.setItem(
      SESSION_KEY,
      JSON.stringify({ token: res.accessToken, expiresAt: res.expiresAt, user: res.user }),
    );

    const auth = setup();

    expect(auth.isAuthenticated()).toBe(false);
    expect(localStorage.getItem(SESSION_KEY)).toBeNull();
  });

  it('clears the session and returns to the login page on logout', () => {
    const auth = setup();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    auth.login('admin@orderhub.dev', 'secret').subscribe();
    http.expectOne('/api/auth/login').flush(loginResponse());

    auth.logout();

    expect(auth.isAuthenticated()).toBe(false);
    expect(localStorage.getItem(SESSION_KEY)).toBeNull();
    expect(navigate).toHaveBeenCalledWith(['/login']);
  });
});
