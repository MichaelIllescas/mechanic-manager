import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { NotificationService } from '../../../core/notifications/notification-service';
import { WorkshopService } from '../workshop-service';
import { UpdateWorkshopRequest, Workshop as WorkshopModel } from '../workshop.models';

@Component({
  selector: 'app-workshop',
  imports: [ReactiveFormsModule],
  templateUrl: './workshop.html',
  styleUrl: './workshop.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Workshop implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly workshopService = inject(WorkshopService);
  private readonly notifications = inject(NotificationService);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly complete = signal(false);
  readonly showForm = signal(false);
  readonly workshop = signal<WorkshopModel | null>(null);

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.maxLength(120)]],
    phone: ['', [Validators.pattern(/^\s*$|^[0-9+()\-\s]{6,30}$/)]],
    email: ['', [Validators.email, Validators.maxLength(255)]],
    address: ['', [Validators.maxLength(255)]],
    notes: ['', [Validators.maxLength(1000)]],
  });

  ngOnInit(): void {
    this.loading.set(true);

    this.workshopService.findMine().subscribe({
      next: (workshop) => {
        this.setWorkshop(workshop);
        this.complete.set(workshop.complete);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage.set(err.error?.message ?? 'No se pudo cargar el taller');
        this.loading.set(false);
      },
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    this.workshopService.update(this.buildRequest()).subscribe({
      next: (workshop) => {
        this.setWorkshop(workshop);
        this.saving.set(false);
        this.showForm.set(false);
        this.notifications.success('Datos del taller actualizados correctamente');
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage.set(err.error?.message ?? 'No se pudo actualizar el taller');
        this.saving.set(false);
      },
    });
  }

  onRegisterClick(): void {
    this.showForm.set(true);
  }

  onEditClick(): void {
    this.showForm.set(true);
  }

  onCancelClick(): void {
    const workshop = this.workshop();

    if (workshop !== null) {
      this.patchForm(workshop);
    }

    this.errorMessage.set(null);
    this.showForm.set(false);
  }

  fieldHasError(field: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[field];
    return control.invalid && (control.touched || control.dirty);
  }

  private setWorkshop(workshop: WorkshopModel): void {
    this.workshop.set(workshop);
    this.complete.set(workshop.complete);
    this.patchForm(workshop);
  }

  private patchForm(workshop: WorkshopModel): void {
    this.form.patchValue({
      name: workshop.name ?? '',
      phone: workshop.phone ?? '',
      email: workshop.email ?? '',
      address: workshop.address ?? '',
      notes: workshop.notes ?? '',
    });
  }

  private buildRequest(): UpdateWorkshopRequest {
    const value = this.form.getRawValue();

    return {
      name: this.emptyToNull(value.name),
      phone: this.emptyToNull(value.phone),
      email: this.emptyToNull(value.email),
      address: this.emptyToNull(value.address),
      notes: this.emptyToNull(value.notes),
    };
  }

  private emptyToNull(value: string): string | null {
    const trimmed = value.trim();
    return trimmed.length > 0 ? trimmed : null;
  }
}
