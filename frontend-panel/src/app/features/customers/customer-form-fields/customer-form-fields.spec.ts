import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { NotificationService } from '../../../core/notifications/notification-service';
import { CustomerService } from '../customer-service';
import { Customer } from '../customer.models';
import { CustomerFormFields } from './customer-form-fields';

describe('CustomerFormFields', () => {
  let component: CustomerFormFields;
  let fixture: ComponentFixture<CustomerFormFields>;

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
    create: vi.fn().mockReturnValue(of(customer)),
    update: vi.fn().mockReturnValue(of(customer)),
  };

  const notifications = {
    success: vi.fn(),
  };

  beforeEach(async () => {
    customerService.create.mockReset();
    customerService.create.mockReturnValue(of(customer));
    customerService.update.mockReset();
    customerService.update.mockReturnValue(of(customer));
    notifications.success.mockReset();

    await TestBed.configureTestingModule({
      imports: [CustomerFormFields],
      providers: [
        { provide: CustomerService, useValue: customerService },
        { provide: NotificationService, useValue: notifications },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CustomerFormFields);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  function fillValidForm(): void {
    component.form.setValue({
      firstName: 'Juan',
      lastName: 'Pérez',
      documentNumber: '32111222',
      phone: '11 5555-6666',
      email: 'juan@email.com',
      address: ' San Martín 123 ',
      notes: '',
    });
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('does not submit an invalid form', () => {
    component.onSubmit();

    expect(customerService.create).not.toHaveBeenCalled();
  });

  it('creates a customer with normalized optional values and emits it', () => {
    const saved = vi.fn();
    component.saved.subscribe(saved);
    fillValidForm();

    component.onSubmit();

    expect(customerService.create).toHaveBeenCalledWith({
      firstName: 'Juan',
      lastName: 'Pérez',
      documentNumber: '32111222',
      phone: '11 5555-6666',
      email: 'juan@email.com',
      address: 'San Martín 123',
      notes: null,
    });
    expect(notifications.success).toHaveBeenCalledWith('Cliente registrado correctamente');
    expect(saved).toHaveBeenCalledWith(customer);
  });

  it('patches and updates the customer received as input', () => {
    fixture.componentRef.setInput('customer', customer);
    fixture.detectChanges();

    expect(component.form.controls.firstName.value).toBe('Ana');
    expect(component.form.controls.address.value).toBe('Av. Siempre Viva 742');

    component.form.controls.firstName.setValue('Andrea');
    component.onSubmit();

    expect(customerService.update).toHaveBeenCalledWith(
      1,
      expect.objectContaining({ firstName: 'Andrea' }),
    );
    expect(notifications.success).toHaveBeenCalledWith('Cliente actualizado correctamente');
  });

  it('emits cancelled', () => {
    const cancelled = vi.fn();
    component.cancelled.subscribe(cancelled);

    component.onCancel();

    expect(cancelled).toHaveBeenCalled();
  });
});
