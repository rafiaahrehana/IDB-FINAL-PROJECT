import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FileUploadResult, FileUploadService } from '../../services/file-upload.service';

@Component({
  selector: 'app-file-upload',
  imports: [CommonModule],
  templateUrl: './file-upload.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FileUpload {
  @Input() label = 'Upload File';
  @Input() accept = '';
  @Output() uploaded = new EventEmitter<FileUploadResult>();

  uploading = false;
  error = '';
  lastResult: FileUploadResult | null = null;

  constructor(private uploadService: FileUploadService, private cdr: ChangeDetectorRef) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.uploading = true;
    this.error = '';
    this.cdr.markForCheck();
    this.uploadService.upload(file).subscribe({
      next: (result) => {
        this.uploading = false;
        this.lastResult = result;
        this.uploaded.emit(result);
        input.value = '';
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.uploading = false;
        this.error = err?.error?.message || 'Upload failed';
        input.value = '';
        this.cdr.markForCheck();
      },
    });
  }
}
