import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { map, tap, catchError } from 'rxjs/operators';
import { LoginRequest, AuthResponse, User, TokenPayload } from '../models/auth.model';
import { environment } from '../../../environments/environment';
 
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = `${environment.apiUrl}/auth`;
  private readonly TOKEN_KEY = 'access_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly USER_KEY = 'user';
 
  private currentUserSubject = new BehaviorSubject<User | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();
 
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasValidToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();
 
  constructor(private http: HttpClient, private router: Router) {
    this.initializeAuthState();
  }
 
  /**
   * Login user with credentials
   */
  login(credentials: LoginRequest): Observable<User> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials).pipe(
      tap(response => {
        this.setTokens(response.token, response.refreshToken);
        this.setUserInStorage(response.user);
        this.currentUserSubject.next(response.user);
        this.isAuthenticatedSubject.next(true);
      }),
      map(response => response.user),
      catchError(error => {
        console.error('Login failed', error);
        return throwError(() => new Error('Invalid credentials'));
      })
    );
  }
 
  /**
   * Logout user
   */
  logout(): void {
    this.clearTokens();
    this.clearUserStorage();
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
    this.router.navigate(['/auth/login']);
  }
 
  /**
   * Refresh access token
   */
  refreshToken(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();
    return this.http.post<AuthResponse>(`${this.API_URL}/refresh`, { 
      refreshToken 
    }).pipe(
      tap(response => {
        this.setTokens(response.token, response.refreshToken);
        this.setUserInStorage(response.user);
        this.currentUserSubject.next(response.user);
      }),
      catchError(error => {
        this.logout();
        return throwError(() => error);
      })
    );
  }
 
  /**
   * Get current access token
   */
  getAccessToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }
 
  /**
   * Get refresh token
   */
  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }
 
  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    return this.hasValidToken();
  }
 
  /**
   * Check if user has specific role
   */
  hasRole(role: string): boolean {
    const user = this.currentUserSubject.value;
    return user ? user.roles.includes(role) : false;
  }
 
  /**
   * Check if user has any of the roles
   */
  hasAnyRole(roles: string[]): boolean {
    const user = this.currentUserSubject.value;
    if (!user) return false;
    return roles.some(role => user.roles.includes(role));
  }
 
  /**
   * Get current user
   */
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }
 
  /**
   * Get company ID
   */
  getCompanyId(): number | null {
    const user = this.currentUserSubject.value;
    return user?.companyId || null;
  }
 
  /**
   * Decode JWT token
   */
  private decodeToken(): TokenPayload | null {
    const token = this.getAccessToken();
    if (!token) return null;
 
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('Error decoding token', error);
      return null;
    }
  }
 
  /**
   * Check if token is valid and not expired
   */
  private hasValidToken(): boolean {
    const token = this.getAccessToken();
    if (!token) return false;
 
    const payload = this.decodeToken();
    if (!payload) return false;
 
    const currentTime = Date.now() / 1000;
    return payload.exp > currentTime;
  }
 
  /**
   * Store tokens in localStorage
   */
  private setTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(this.TOKEN_KEY, accessToken);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
  }
 
  /**
   * Clear tokens from localStorage
   */
  private clearTokens(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
  }
 
  /**
   * Store user in localStorage
   */
  private setUserInStorage(user: User): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }
 
  /**
   * Get user from localStorage
   */
  private getUserFromStorage(): User | null {
    const user = localStorage.getItem(this.USER_KEY);
    return user ? JSON.parse(user) : null;
  }
 
  /**
   * Clear user from localStorage
   */
  private clearUserStorage(): void {
    localStorage.removeItem(this.USER_KEY);
  }
 
  /**
   * Initialize auth state on app startup
   */
  private initializeAuthState(): void {
    const isAuthenticated = this.hasValidToken();
    this.isAuthenticatedSubject.next(isAuthenticated);
 
    if (isAuthenticated) {
      const user = this.getUserFromStorage();
      this.currentUserSubject.next(user);
    }
  }
}