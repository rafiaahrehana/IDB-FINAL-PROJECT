import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AssetImportResult } from '../../models/itam.model';
import { AssetImportService } from '../../services/asset-import.service';

@Component({
  selector: 'app-asset-import',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './asset-import.html',
})
export class AssetImport {
  uploading = false;
  error = '';
  result: AssetImportResult | null = null;

  constructor(private importService: AssetImportService) {}

  downloadTemplate(): void {
    this.importService.getTemplateCsv().subscribe({
      next: (csv) => {
        const blob = new Blob([csv], { type: 'text/csv' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'asset-import-template.csv';
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => (this.error = 'Failed to download template'),
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.uploading = true;
    this.error = '';
    this.result = null;
    this.importService.importCsv(file).subscribe({
      next: (res) => {
        this.result = res;
        this.uploading = false;
        input.value = '';
      },
      error: (err) => {
        this.error = err?.error?.message || 'Import failed';
        this.uploading = false;
        input.value = '';
      },
    });
  }
}
