import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { NotificationService } from '../../../core/notifications/notification-service';
import { CustomerService } from '../customer-service';
import { Customer } from '../customer.models';
import { CustomerForm } from './customer-form';

@Component({
  template: '',
})
class TestCustomersPage {}

describe('CustomerForm', () => {
  let component: CustomerForm;
  let fixture: ComponentFixture<CustomerForm>;
  let routeId: string | null;

  const customer: Customer = {
    id: 1,
    firstName: 'Ana',
    lastName: 'Gómez',
    documentNumber: '30111222',
    phone: '11 4444-5555',
    email: 'ana@email.com',
    notes: 'Cliente habitual',
    createdAt: null,
    updatedAt: null,
  };

  const customerService = {
    findFromListById: vi.fn().mockReturnValue(of(customer)),
    create: vi.fn().mockReturnValue(of(customer)),
    update: vi.fn().mockReturnValue(of(customer)),
  };

  const notifications = {
    success: vi.fn(),
  };

  beforeEach(async () => {
    routeId = null;
    customerService.findFromListById.mockReset();
    customerService.findFromListById.mockReturnValue(of(customer));
    customerService.create.mockReset();
    customerService.create.mockReturnValue(of(customer));
    customerService.update.mockReset();
    customerService.update.mockReturnValue(of(customer));
    notifications.success.mockReset();

    await TestBed.configureTestingModule({
      imports: [CustomerForm],
      providers: [
        provideRouter([{ path: 'customers', component: TestCustomersPage }]),
        { provide: CustomerService, useValue: customerService },
        { provide: NotificationService, useValue: notifications },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => routeId,
              },
            },
          },
        },
      ],
    }).compileComponents();
  });

  function createComponent(): void {
    fixture = TestBed.createComponent(CustomerForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  function fillValidForm(): void {
    component.form.setValue({
      firstName: 'Juan',
      lastName: 'Pérez',
      documentNumber: '32111222',
      phone: '11 5555-6666',
      email: 'juan@email.com',
      notes: '',
    });
  }

  it('should create', () => {
    createComponent();

    expect(component).toBeTruthy();
  });

  it('does not submit an invalid form', () => {
    createComponent();

    component.onSubmit();

    expect(customerService.create).not.toHaveBeenCalled();
  });

  it('creates a customer with normalized optional values', () => {
    createComponent();
    fillValidForm();

    component.onSubmit();

    expect(customerService.create).toHaveBeenCalledWith({
      firstName: 'Juan',
      lastName: 'Pérez',
      documentNumber: '32111222',
      phone: '11 5555-6666',
      email: 'juan@email.com',
      notes: null,
    });
    expect(notifications.success).toHaveBeenCalledWith('Cliente registrado correctamente');
  });

  it('loads and updates an existing customer', () => {
    routeId = '1';
    createComponent();

    expect(customerService.findFromListById).toHaveBeenCalledWith(1);
    expect(component.form.controls.firstName.value).toBe('Ana');

    component.form.controls.firstName.setValue('Andrea');
    component.onSubmit();

    expect(customerService.update).toHaveBeenCalledWith(
      1,
      expect.objectContaining({ firstName: 'Andrea' }),
    );
    expect(notifications.success).toHaveBeenCalledWith('Cliente actualizado correctamente');
  });
});
