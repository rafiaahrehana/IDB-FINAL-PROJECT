import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-contact-sales',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './contact-sales.html',
  styleUrls: ['./contact-sales.scss']
})
export class ContactSales {
  submitForm(event: Event) {
    event.preventDefault();
    alert('Thank you for contacting sales! Our team will reach out to you shortly.');
  }
}
