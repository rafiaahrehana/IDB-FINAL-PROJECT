import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { ApiService } from './api.service';

// Mirrors backend com.businessos.auth.role.enums.PermissionCode - kept as a plain
// string type (not a TS enum) since the source of truth is GET /api/users/permissions,
// not a value duplicated on both sides.
export type PermissionCode = string;

/**
 * Caches the current user's resolved permission set (GET /api/users/permissions) so
 * hasPermission()/hasAnyPermission() can be checked synchronously - route guards run
 * synchronously today (see RoleGuard), so this must already be populated by the time
 * a guard evaluates, not fetched lazily on first check.
 *
 * Mirrors AuthService's own BehaviorSubject + localStorage pattern for `User` so the
 * permission set survives a page refresh the same way the logged-in user does.
 */
@Injectable({ providedIn: 'root' })
export class PermissionService {
  private readonly STORAGE_KEY = 'permissions';
  private readonly CATALOG_STORAGE_KEY = 'permission_catalog';

  private permissionsSubject = new BehaviorSubject<string[]>(this.getFromStorage(this.STORAGE_KEY));
  public permissions$ = this.permissionsSubject.asObservable();

  // Full catalog of every permission code the app knows about (GET /api/permissions) -
  // used only to answer "does this user hold every permission that exists", e.g. to
  // decide whether a custom role the owner built with every checkbox ticked should be
  // treated like an owner for UI purposes (see hasAllPermissions()).
  private catalogSubject = new BehaviorSubject<string[]>(this.getFromStorage(this.CATALOG_STORAGE_KEY));
  public catalog$ = this.catalogSubject.asObservable();

  constructor(private api: ApiService) {}

  /** Fetches the current user's permission set from the backend and caches it. */
  load(): Observable<string[]> {
    return this.api.get<string[]>('/users/permissions').pipe(
      tap(permissions => {
        this.permissionsSubject.next(permissions);
        localStorage.setItem(this.STORAGE_KEY, JSON.stringify(permissions));
      }),
    );
  }

  /** Fetches the full permission catalog and caches it. See catalogSubject above. */
  loadCatalog(): Observable<string[]> {
    return this.api.get<{ code: string }[]>('/permissions').pipe(
      map(list => list.map(p => p.code)),
      tap(codes => {
        this.catalogSubject.next(codes);
        localStorage.setItem(this.CATALOG_STORAGE_KEY, JSON.stringify(codes));
      }),
    );
  }

  hasPermission(permission: PermissionCode | null | undefined): boolean {
    if (!permission) return true;
    return this.permissionsSubject.value.includes(permission);
  }

  hasAnyPermission(permissions: PermissionCode[] | null | undefined): boolean {
    if (!permissions || permissions.length === 0) return true;
    const mine = this.permissionsSubject.value;
    return permissions.some(p => mine.includes(p));
  }

  /** True only once the catalog has loaded and the user's set covers every known code. */
  hasAllPermissions(): boolean {
    const all = this.catalogSubject.value;
    if (!all.length) return false;
    const mine = new Set(this.permissionsSubject.value);
    return all.every(code => mine.has(code));
  }

  /** Clears the cached sets. Called on logout (see AuthService.clearSession()). */
  clear(): void {
    this.permissionsSubject.next([]);
    this.catalogSubject.next([]);
    localStorage.removeItem(this.STORAGE_KEY);
    localStorage.removeItem(this.CATALOG_STORAGE_KEY);
  }

  private getFromStorage(key: string): string[] {
    const raw = localStorage.getItem(key);
    if (!raw) return [];
    try {
      return JSON.parse(raw);
    } catch {
      return [];
    }
  }
}
