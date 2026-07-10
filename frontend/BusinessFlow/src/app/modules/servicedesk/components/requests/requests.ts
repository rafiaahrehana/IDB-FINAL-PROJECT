import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ServiceRequest } from '../../models/servicedesk.model';
import { ServiceRequestService } from '../../services/service-request.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

type Tab = 'all' | 'my' | 'assigned';

@Component({
  selector: 'app-requests',
  imports: [CommonModule, RouterLink, Pagination, Loader, EmptyState],
  templateUrl: './requests.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './requests.scss',
})
export class Requests implements OnInit {
  tab: Tab = 'all';
  requests: ServiceRequest[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';

  constructor(private requestService: ServiceRequestService) {}

  ngOnInit(): void {
    this.load();
  }

  setTab(tab: Tab): void {
    this.tab = tab;
    this.page = 0;
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    const source =
      this.tab === 'my'
        ? this.requestService.my(this.page)
        : this.tab === 'assigned'
          ? this.requestService.assignedToMe(this.page)
          : this.requestService.list(this.page);
    source.subscribe({
      next: (res) => {
        this.requests = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load requests';
        this.loading = false;
      },
    });
  }

  isOverdue(request: ServiceRequest): boolean {
    return request.slaBreach;
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
