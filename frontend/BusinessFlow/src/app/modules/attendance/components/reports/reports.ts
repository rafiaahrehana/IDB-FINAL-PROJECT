import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportService } from '../../services/report.service';
import { Loader } from '../../../../shared/components/loader/loader';

@Component({
  selector: 'app-attendance-reports',
  imports: [CommonModule, FormsModule, Loader],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './reports.html',
})
export class Reports implements OnInit {
  dailyReport: any = null;
  monthlyReport: any = null;
  lateAbsentReport: any = null;
  loading = false;
  error = '';
  selectedDate = new Date().toISOString().split('T')[0];
  selectedMonth = new Date().toISOString().substring(0, 7);

  constructor(private reportService: ReportService) {}

  ngOnInit(): void {
    this.loadDaily();
  }

  loadDaily(): void {
    this.loading = true;
    this.reportService.daily(this.selectedDate).subscribe({
      next: (data) => {
        this.dailyReport = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Report not available';
        this.loading = false;
      },
    });
  }

  loadMonthly(): void {
    this.loading = true;
    const [year, month] = this.selectedMonth.split('-');
    this.reportService.monthly(month, +year).subscribe({
      next: (data) => {
        this.monthlyReport = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Report not available';
        this.loading = false;
      },
    });
  }

  objectKeys(obj: any): string[] {
    return obj ? Object.keys(obj) : [];
  }
}
