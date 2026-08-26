import { Injectable, signal } from '@angular/core';

export interface Notification {
  text: string;
  type: 'success' | 'error';
}

const AUTO_DISMISS_MS = 4000;

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  readonly current = signal<Notification | null>(null);

  private timeoutId: ReturnType<typeof setTimeout> | null = null;

  success(text: string): void {
    this.show({ text, type: 'success' });
  }

  error(text: string): void {
    this.show({ text, type: 'error' });
  }

  clear(): void {
    this.cancelAutoDismiss();
    this.current.set(null);
  }

  private show(notification: Notification): void {
    // Cada notificacion reinicia el temporizador: solo se muestra una a la vez.
    this.cancelAutoDismiss();
    this.current.set(notification);
    this.timeoutId = setTimeout(() => this.clear(), AUTO_DISMISS_MS);
  }

  private cancelAutoDismiss(): void {
    if (this.timeoutId !== null) {
      clearTimeout(this.timeoutId);
      this.timeoutId = null;
    }
  }
}
