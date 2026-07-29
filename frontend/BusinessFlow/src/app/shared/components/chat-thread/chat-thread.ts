import {
  Component,
  Input,
  Output,
  EventEmitter,
  ElementRef,
  ViewChild,
  OnChanges,
  SimpleChanges,
  ChangeDetectionStrategy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface ChatMessage {
  id: number;
  authorId: number;
  authorName: string;
  content: string;
  createdAt: string;
  internal?: boolean;
}

/**
 * Reusable live chat bubble thread. Expects `messages` in chronological
 * (oldest-first) order - the backend's *OrderByCreatedAtDesc endpoints return
 * newest-first, so callers should reverse before binding.
 */
@Component({
  selector: 'app-chat-thread',
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-thread.html',
  styleUrl: './chat-thread.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChatThread implements OnChanges {
  @Input() messages: ChatMessage[] = [];
  @Input() currentUserId: number | null = null;
  @Input() connected = false;
  @Input() sending = false;
  @Input() disabled = false;
  @Input() placeholder = 'Write a message...';
  @Input() emptyMessage = 'No messages yet — start the conversation.';
  @Output() send = new EventEmitter<string>();

  @ViewChild('scrollAnchor') private scrollAnchor?: ElementRef<HTMLElement>;

  draft = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['messages']) {
      // Wait a tick so the new bubble is actually in the DOM before scrolling to it.
      setTimeout(() => this.scrollToBottom(), 0);
    }
  }

  submit(): void {
    const text = this.draft.trim();
    if (!text || this.disabled) return;
    this.send.emit(text);
    this.draft = '';
  }

  isOwn(m: ChatMessage): boolean {
    return this.currentUserId != null && m.authorId === this.currentUserId;
  }

  initials(name: string): string {
    if (!name) return '?';
    const parts = name.trim().split(/\s+/);
    return ((parts[0]?.[0] || '') + (parts[1]?.[0] || '')).toUpperCase() || name[0]!.toUpperCase();
  }

  private scrollToBottom(): void {
    this.scrollAnchor?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'end' });
  }
}
