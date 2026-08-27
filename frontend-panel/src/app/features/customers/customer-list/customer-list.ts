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

import { CustomerService } from '../customer-service';
import { Customer } from '../customer.models';

@Component({
  selector: 'app-customer-list',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './customer-list.html',
  styleUrl: './customer-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerList implements OnInit {
  private readonly customerService = inject(CustomerService);
  private readonly destroyRef = inject(DestroyRef);

  readonly customers = signal<Customer[]>([]);
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
          this.customerService.findAll(search).pipe(
            catchError((error: HttpErrorResponse) => {
              this.errorMessage.set(error.error?.message ?? 'No se pudieron cargar los clientes');
              return of([]);
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((customers) => {
        this.customers.set(customers);
        this.loading.set(false);
      });
  }

  clearSearch(): void {
    this.searchControl.setValue('');
  }
}
