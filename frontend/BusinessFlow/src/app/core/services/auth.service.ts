import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { map, tap, catchError } from 'rxjs/operators';
import { LoginRequest, RegisterRequest, AuthResponse, User, TokenPayload } from '../models/auth.model';
import { environment } from '../../../environments/environment';
 
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = `${environment.apiUrl}/auth`;
  private readonly TOKEN_KEY = 'access_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly USER_KEY = 'user';
  private readonly PERMISSIONS_KEY = 'user_permissions';
 
  private currentUserSubject = new BehaviorSubject<User | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();
 
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasValidToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  private permissionsSubject = new BehaviorSubject<string[]>(this.getPermissionsFromStorage());
  public permissions$ = this.permissionsSubject.asObservable();
 
  constructor(private http: HttpClient, private router: Router) {
    this.initializeAuthState();
  }
 
  login(credentials: LoginRequest): Observable<User> {
    return this.http.post<any>(`${this.API_URL}/login`, credentials).pipe(
      tap(response => {
        const user: User = {
          id: response.userId,
          firstName: response.firstName,
          lastName: response.lastName || '',
          email: response.email,
          role: response.role,
          companyId: response.companyId,
          customRoleId: response.customRoleId,
          customRoleName: response.customRoleName
        };
        this.setTokens(response.accessToken, response.refreshToken);
        this.setUserInStorage(user);
        this.currentUserSubject.next(user);
        this.isAuthenticatedSubject.next(true);
        if (response.customRoleId) {
          this.fetchAndStorePermissions(response.customRoleId);
        }
      }),
      map(response => ({
        id: response.userId,
        firstName: response.firstName,
        lastName: response.lastName || '',
        email: response.email,
        role: response.role,
        companyId: response.companyId,
        customRoleId: response.customRoleId,
        customRoleName: response.customRoleName
      } as User)),
      catchError(error => {
        console.error('Login failed', error);
        return throwError(() => error);
      })
    );
  }
 
  register(payload: RegisterRequest): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/register`, payload).pipe(
      catchError(error => {
        console.error('Registration failed', error);
        return throwError(() => error);
      })
    );
  }

  logout(): void {
    this.clearTokens();
    this.clearUserStorage();
    localStorage.removeItem(this.PERMISSIONS_KEY);
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
    this.permissionsSubject.next([]);
    this.router.navigate(['/auth/login']);
  }
 
  refreshToken(): Observable<any> {
    const refreshToken = this.getRefreshToken();
    return this.http.post<any>(`${this.API_URL}/refresh`, { 
      refreshToken 
    }).pipe(
      tap(response => {
        this.setTokens(response.accessToken, response.refreshToken);
      }),
      catchError(error => {
        this.logout();
        return throwError(() => error);
      })
    );
  }
 
  getAccessToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }
 
  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }
 
  isAuthenticated(): boolean {
    return this.hasValidToken();
  }
 
  hasRole(role: string): boolean {
    const user = this.currentUserSubject.value;
    return user ? user.role === role : false;
  }

  hasAnyRole(roles: string[]): boolean {
    const user = this.currentUserSubject.value;
    if (!user) return false;
    return roles.some(role => user.role === role);
  }

  hasPermission(permission: string): boolean {
    const user = this.currentUserSubject.value;
    if (!user) return false;
    if (user.role === 'COMPANY_OWNER' || user.role === 'SUPER_ADMIN') return true;
    return this.permissionsSubject.value.includes(permission);
  }

  hasAnyPermission(permissions: string[]): boolean {
    return permissions.some(p => this.hasPermission(p));
  }

  getPermissions(): string[] {
    return this.permissionsSubject.value;
  }

  refreshPermissions(): void {
    const user = this.currentUserSubject.value;
    if (user?.customRoleId) {
      this.fetchAndStorePermissions(user.customRoleId);
    }
  }

  private fetchAndStorePermissions(customRoleId: number): void {
    this.http.get<string[]>(`${environment.apiUrl}/custom-roles/${customRoleId}/permissions`).subscribe({
      next: (perms) => {
        localStorage.setItem(this.PERMISSIONS_KEY, JSON.stringify(perms));
        this.permissionsSubject.next(perms);
      },
      error: () => {
        this.permissionsSubject.next([]);
      }
    });
  }
 
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }
 
  getCompanyId(): number | null {
    const user = this.currentUserSubject.value;
    return user?.companyId || null;
  }
 
  getProfile(): Observable<any> {
    return this.http.get<any>(`${environment.apiUrl}/users/profile`);
  }

  updateProfile(request: any): Observable<any> {
    return this.http.patch<any>(`${environment.apiUrl}/users/profile`, request).pipe(
      tap(profile => {
        const storedUser = this.getUserFromStorage();
        if (storedUser) {
          storedUser.firstName = profile.firstName;
          storedUser.lastName = profile.lastName;
          storedUser.image = profile.image;
          this.setUserInStorage(storedUser);
          this.currentUserSubject.next(storedUser);
        }
      })
    );
  }
 
  private setTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(this.TOKEN_KEY, accessToken);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
  }
 
  private clearTokens(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
  }
 
  private setUserInStorage(user: User): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }
 
  private getUserFromStorage(): User | null {
    const user = localStorage.getItem(this.USER_KEY);
    return (user && user !== 'undefined') ? JSON.parse(user) : null;
  }

  private getPermissionsFromStorage(): string[] {
    const perms = localStorage.getItem(this.PERMISSIONS_KEY);
    return perms ? JSON.parse(perms) : [];
  }
 
  private clearUserStorage(): void {
    localStorage.removeItem(this.USER_KEY);
  }
 
  private hasValidToken(): boolean {
    const token = this.getAccessToken();
    if (!token) return false;
    const payload = this.decodeToken();
    if (!payload) return false;
    const currentTime = Date.now() / 1000;
    return payload.exp > currentTime;
  }
 
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
 
  private initializeAuthState(): void {
    const isAuthenticated = this.hasValidToken();
    this.isAuthenticatedSubject.next(isAuthenticated);
    if (isAuthenticated) {
      const user = this.getUserFromStorage();
      this.currentUserSubject.next(user);
      if (user?.customRoleId) {
        this.fetchAndStorePermissions(user.customRoleId);
      }
    }
  }
}
