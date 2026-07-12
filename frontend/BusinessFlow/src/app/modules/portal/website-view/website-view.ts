import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { PortalService } from '../portal.service';
import { Loader } from '../../../shared/components/loader/loader';

@Component({
  selector: 'app-website-view',
  imports: [CommonModule, RouterLink, Loader],
  templateUrl: './website-view.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './website-view.scss',
})
export class WebsiteView implements OnInit {
  subdomain = '';
  loading = false;
  error = '';

  get portalUrl(): string {
    return `/portal/${this.subdomain}`;
  }

  constructor(
    private portalService: PortalService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.portalService.getMyCompany().subscribe({
      next: (c) => {
        this.subdomain = c.subdomain;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load company';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }
}
