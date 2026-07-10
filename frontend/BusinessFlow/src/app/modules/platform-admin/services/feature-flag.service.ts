import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { FeatureFlag } from '../models/platform-admin.model';

@Injectable({ providedIn: 'root' })
export class FeatureFlagService {
  private readonly endpoint = '/feature-flags';

  constructor(private api: ApiService) {}

  list(): Observable<FeatureFlag[]> {
    return this.api.get<FeatureFlag[]>(this.endpoint);
  }

  getByKey(key: string): Observable<FeatureFlag> {
    return this.api.get<FeatureFlag>(`${this.endpoint}/${key}`);
  }

  toggle(key: string): Observable<FeatureFlag> {
    return this.api.patch<FeatureFlag>(`${this.endpoint}/${key}/toggle`, {});
  }

  seed(): Observable<string> {
    return this.api.post<string>(`${this.endpoint}/seed`, {});
  }
}
