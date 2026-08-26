import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { UserService } from '../user-service';
import { User } from '../user.models';

@Component({
  selector: 'app-user-detail',
  imports: [DatePipe, RouterLink],
  templateUrl: './user-detail.html',
  styleUrl: './user-detail.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserDetail implements OnInit {
  private readonly userService = inject(UserService);
  private readonly route = inject(ActivatedRoute);

  readonly user = signal<User | null>(null);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.userService.findById(id).subscribe({
      next: (user) => this.user.set(user),
      error: (err: HttpErrorResponse) =>
        this.errorMessage.set(err.error?.message ?? 'No se pudo cargar el usuario'),
    });
  }
}
