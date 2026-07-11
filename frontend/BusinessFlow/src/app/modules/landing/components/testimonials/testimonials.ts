import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-testimonials',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './testimonials.html',
  styleUrls: ['./testimonials.scss']
})
export class TestimonialsComponent {
  testimonials = signal([
    {
      quote: "BusinessOS has fundamentally changed how we operate. We replaced five different tools with one unified platform.",
      author: "Sarah Jenkins",
      role: "COO, TechFlow Inc.",
      initials: "SJ",
      color: "bg-primary-bos"
    },
    {
      quote: "The AI suggestions saved our support team hundreds of hours in the first month alone. Highly recommended.",
      author: "David Chen",
      role: "Head of Support, CloudScale",
      initials: "DC",
      color: "bg-accent"
    },
    {
      quote: "Security and compliance were our top priorities. BusinessOS exceeded our expectations on both fronts.",
      author: "Emily Rodriguez",
      role: "CTO, FinServ Global",
      initials: "ER",
      color: "bg-warning"
    }
  ]);
}
