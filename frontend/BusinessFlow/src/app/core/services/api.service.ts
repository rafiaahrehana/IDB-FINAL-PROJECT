import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
 
export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}
 
export interface ApiError {
  error: string;
  message: string;
  timestamp: string;
  status: number;
}
 
@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly baseUrl = environment.apiUrl;
 
  constructor(private http: HttpClient) {}
 
  /**
   * GET request
   */
  get<T>(endpoint: string, params?: any): Observable<T> {
    let httpParams = new HttpParams();
    if (params) {
      Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined) {
          httpParams = httpParams.set(key, params[key]);
        }
      });
    }
    return this.http.get<T>(`${this.baseUrl}${endpoint}`, { params: httpParams });
  }
 
  /**
   * POST request
   */
  post<T>(endpoint: string, data: any): Observable<T> {
    return this.http.post<T>(`${this.baseUrl}${endpoint}`, data);
  }
 
  /**
   * PUT request
   */
  put<T>(endpoint: string, data: any): Observable<T> {
    return this.http.put<T>(`${this.baseUrl}${endpoint}`, data);
  }
 
  /**
   * PATCH request
   */
  patch<T>(endpoint: string, data: any): Observable<T> {
    return this.http.patch<T>(`${this.baseUrl}${endpoint}`, data);
  }
 
  /**
   * DELETE request
   */
  delete<T>(endpoint: string): Observable<T> {
    return this.http.delete<T>(`${this.baseUrl}${endpoint}`);
  }

  /**
   * Uploads a file to the generic file storage endpoint (POST /api/upload,
   * multipart/form-data). Returns { fileName, fileUrl, message }.
   */
  uploadFile(file: File): Observable<{ fileName: string; fileUrl: string; message: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{ fileName: string; fileUrl: string; message: string }>(
      `${this.baseUrl}/upload`, formData);
  }

  /**
   * Text-response variants. Several backend endpoints return a plain string body
   * (ResponseEntity<String>) with content-type text/plain - the default 'json'
   * responseType would fail to parse it and turn a successful 200 into an error.
   */
  postText(endpoint: string, data: any): Observable<string> {
    return this.http.post(`${this.baseUrl}${endpoint}`, data, { responseType: 'text' });
  }

  patchText(endpoint: string, data: any): Observable<string> {
    return this.http.patch(`${this.baseUrl}${endpoint}`, data, { responseType: 'text' });
  }

  deleteText(endpoint: string): Observable<string> {
    return this.http.delete(`${this.baseUrl}${endpoint}`, { responseType: 'text' });
  }
 
  /**
   * Get paginated data
   */
  getPaged<T>(endpoint: string, page: number = 0, size: number = 20, params?: any): Observable<PagedResponse<T>> {
    const httpParams = {
      page,
      size,
      ...params
    };
    // Backend returns Spring Data's Page<T>, whose JSON uses `number`/`size`
    // instead of `currentPage`/`pageSize` - normalize here so every list
    // screen gets a consistent PagedResponse shape.
    return this.get<any>(endpoint, httpParams).pipe(
      map((res: any): PagedResponse<T> => ({
        content: res?.content ?? [],
        totalElements: res?.totalElements ?? res?.page?.totalElements ?? 0,
        totalPages: res?.totalPages ?? res?.page?.totalPages ?? 0,
        currentPage: res?.currentPage ?? res?.number ?? res?.page?.number ?? page,
        pageSize: res?.pageSize ?? res?.size ?? res?.page?.size ?? size
      }))
    );
  }
}