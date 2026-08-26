import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';

import { NotificationService } from '../../../../core/notifications/notification-service';
import { UserService } from '../user-service';
import { User } from '../user.models';

@Component({
  selector: 'app-user-list',
  imports: [RouterLink],
  templateUrl: './user-list.html',
  styleUrl: './user-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserList implements OnInit {
  private readonly userService = inject(UserService);
  private readonly notifications = inject(NotificationService);

  readonly users = signal<User[]>([]);
  readonly updatingId = signal<number | null>(null);

  ngOnInit(): void {
    this.userService.findAll().subscribe({
      next: (users) => this.users.set(users),
    });
  }

  toggleStatus(user: User): void {
    const status = user.status === 'ENABLED' ? 'DISABLED' : 'ENABLED';

    this.updatingId.set(user.id);

    this.userService.updateStatus(user.id, { status }).subscribe({
      next: (updated) => {
        this.updatingId.set(null);
        this.users.update((users) => users.map((u) => (u.id === updated.id ? updated : u)));
        this.notifications.success(
          updated.status === 'ENABLED'
            ? `${updated.name} fue habilitado`
            : `${updated.name} fue deshabilitado`,
        );
      },
      error: (err: HttpErrorResponse) => {
        this.updatingId.set(null);
        this.notifications.error(err.error?.message ?? 'No se pudo cambiar el estado');
      },
    });
  }
}
