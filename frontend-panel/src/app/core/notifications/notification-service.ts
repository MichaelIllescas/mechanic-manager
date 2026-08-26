import { Injectable, signal } from '@angular/core';

export interface Notification {
  text: string;
  type: 'success' | 'error';
}

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  readonly current = signal<Notification | null>(null);

  success(text: string): void {
    this.show({ text, type: 'success' });
  }

  error(text: string): void {
    this.show({ text, type: 'error' });
  }

  clear(): void {
    this.current.set(null);
  }

  private show(notification: Notification): void {
    this.current.set(notification);
  }
}
