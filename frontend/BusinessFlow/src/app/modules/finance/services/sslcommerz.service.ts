import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';

export interface SslCommerzInitRequest {
  amount: number;
  currency?: string;
  cusName: string;
  cusEmail: string;
  cusPhone?: string;
  cusAdd1?: string;
  cusCity?: string;
  cusCountry?: string;
}

export interface SslCommerzInitResponse {
  success: boolean;
  gatewayPageUrl: string;
  sessionKey: string;
  tranId: string;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class SslCommerzService {
  private readonly endpoint = '/sslcommerz';
  constructor(private api: ApiService) {}

  initPayment(request: SslCommerzInitRequest): Observable<SslCommerzInitResponse> {
    return this.api.post<SslCommerzInitResponse>(`${this.endpoint}/init`, request);
  }

  getStatus(tranId: string): Observable<{ tranId: string; status: string }> {
    return this.api.get<{ tranId: string; status: string }>(`${this.endpoint}/status/${tranId}`);
  }
}
