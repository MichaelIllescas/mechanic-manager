import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { NotificationService } from '../../../core/notifications/notification-service';
import { SupplyService } from '../supply-service';

@Component({
  selector: 'app-supply-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './supply-form.html',
  styleUrl: './supply-form.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SupplyForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly supplyService = inject(SupplyService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly notifications = inject(NotificationService);

  private supplyId: number | null = null;

  readonly isEdit = signal(false);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    price: [null as number | null, [Validators.required, Validators.min(0)]],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id === null) {
      return;
    }

    this.supplyId = Number(id);
    this.isEdit.set(true);

    this.supplyService.findById(this.supplyId).subscribe({
      next: (supply) =>
        this.form.patchValue({
          name: supply.name,
          price: supply.price,
        }),
      error: (err: HttpErrorResponse) =>
        this.errorMessage.set(err.error?.message ?? 'No se pudo cargar el insumo'),
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    const payload = this.payload();
    const request = this.isEdit()
      ? this.supplyService.update(this.supplyId!, payload)
      : this.supplyService.create(payload);

    request.subscribe({
      next: () => {
        this.notifications.success(
          this.isEdit() ? 'Insumo actualizado correctamente' : 'Insumo creado correctamente',
        );
        void this.router.navigate(['/insumos']);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(
          err.error?.message ??
            (this.isEdit() ? 'No se pudo actualizar el insumo' : 'No se pudo crear el insumo'),
        );
      },
    });
  }

  private payload() {
    const { name, price } = this.form.getRawValue();

    return {
      name: name!.trim(),
      price: price!,
    };
  }
}
