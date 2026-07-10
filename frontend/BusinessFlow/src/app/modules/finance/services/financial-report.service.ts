import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { ProfitLossReport, BalanceSheetReport, TrialBalanceReport } from '../models/finance.model';

@Injectable({ providedIn: 'root' })
export class FinancialReportService {
  private readonly endpoint = '/company/finance/reports';
  constructor(private api: ApiService) {}

  profitLoss(startDate: string, endDate: string): Observable<ProfitLossReport> {
    return this.api.get<ProfitLossReport>(`${this.endpoint}/profit-loss`, { startDate, endDate });
  }

  balanceSheet(asOfDate: string): Observable<BalanceSheetReport> {
    return this.api.get<BalanceSheetReport>(`${this.endpoint}/balance-sheet`, { asOfDate });
  }

  trialBalance(asOfDate: string): Observable<TrialBalanceReport> {
    return this.api.get<TrialBalanceReport>(`${this.endpoint}/trial-balance`, { asOfDate });
  }
}
