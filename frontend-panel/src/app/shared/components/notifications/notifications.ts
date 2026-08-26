import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { NotificationService } from '../../../core/notifications/notification-service';

@Component({
  selector: 'app-notifications',
  imports: [],
  templateUrl: './notifications.html',
  styleUrl: './notifications.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Notifications {
  private readonly notifications = inject(NotificationService);

  readonly current = this.notifications.current;

  close(): void {
    this.notifications.clear();
  }
}
