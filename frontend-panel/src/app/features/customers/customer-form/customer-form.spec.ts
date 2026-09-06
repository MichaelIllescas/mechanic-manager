import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, provideRouter } from '@angular/router';
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
    address: 'Av. Siempre Viva 742',
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

  it('should create', () => {
    createComponent();

    expect(component).toBeTruthy();
  });

  it('does not load any customer in create mode', () => {
    createComponent();

    expect(customerService.findFromListById).not.toHaveBeenCalled();
    expect(component.customer()).toBeNull();
    expect(component.isEdit()).toBe(false);
  });

  it('loads the customer to edit from the route', () => {
    routeId = '1';
    createComponent();

    expect(customerService.findFromListById).toHaveBeenCalledWith(1);
    expect(component.isEdit()).toBe(true);
    expect(component.customer()).toEqual(customer);
  });

  it('reports an invalid route id', () => {
    routeId = 'abc';
    createComponent();

    expect(customerService.findFromListById).not.toHaveBeenCalled();
    expect(component.errorMessage()).toBe('El identificador del cliente no es válido');
  });

  it('navigates back to the list after saving', () => {
    createComponent();
    const router = TestBed.inject(Router);
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    component.onSaved();

    expect(navigate).toHaveBeenCalledWith(['/customers']);
  });
});
