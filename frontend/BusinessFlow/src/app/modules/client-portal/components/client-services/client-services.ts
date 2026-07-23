import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { CompanyService, ServiceCategory } from '../../../servicedesk/models/servicedesk.model';
import { CompanyServiceService } from '../../../servicedesk/services/company-service.service';
import { ServiceCategoryService } from '../../../servicedesk/services/service-category.service';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

@Component({
  selector: 'app-client-services',
  imports: [CommonModule, RouterLink, Loader, EmptyState],
  templateUrl: './client-services.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClientServices implements OnInit {
  categoryId: number | null = null;
  category: ServiceCategory | null = null;
  services: CompanyService[] = [];
  loading = true;
  error = '';

  constructor(
    private companyServiceService: CompanyServiceService,
    private categoryService: ServiceCategoryService,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.categoryId = +idParam;
      this.loadCategory();
      this.loadServices();
    } else {
      this.error = 'Category not specified';
      this.loading = false;
      this.cdr.markForCheck();
    }
  }

  loadCategory(): void {
    if (!this.categoryId) return;
    this.categoryService.getById(this.categoryId).subscribe({
      next: (res) => {
        this.category = res;
        this.cdr.markForCheck();
      }
    });
  }

  loadServices(): void {
    this.loading = true;
    this.error = '';
    this.companyServiceService.listActive().subscribe({
      next: (res) => {
        // Filter by category
        this.services = res.filter(s => s.categoryId === this.categoryId);
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load services.';
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  requestService(service: CompanyService): void {
    this.router.navigate(['/client/requests'], { queryParams: { create: 'true', serviceId: service.id } });
  }
}
