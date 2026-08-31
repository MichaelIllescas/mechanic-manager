import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  of,
  startWith,
  switchMap,
  tap,
} from 'rxjs';

import { VehicleService } from '../vehicle-service';
import { Vehicle } from '../vehicle.models';

@Component({
  selector: 'app-vehicle-list',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './vehicle-list.html',
  styleUrl: './vehicle-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VehicleList implements OnInit {
  private readonly vehicleService = inject(VehicleService);
  private readonly destroyRef = inject(DestroyRef);

  readonly vehicles = signal<Vehicle[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly searchControl = new FormControl('', { nonNullable: true });

  ngOnInit(): void {
    this.searchControl.valueChanges
      .pipe(
        startWith(this.searchControl.value),
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => {
          this.loading.set(true);
          this.errorMessage.set(null);
        }),
        switchMap((search) =>
          this.vehicleService.findAll(search).pipe(
            catchError((error: HttpErrorResponse) => {
              this.errorMessage.set(error.error?.message ?? 'No se pudieron cargar los vehículos');
              return of([]);
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((vehicles) => {
        this.vehicles.set(vehicles);
        this.loading.set(false);
      });
  }

  clearSearch(): void {
    this.searchControl.setValue('');
  }
}
