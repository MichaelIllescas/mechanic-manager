import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { CustomerService } from '../customer-service';
import { Customer } from '../customer.models';
import { CustomerDetail } from './customer-detail';

describe('CustomerDetail', () => {
  let component: CustomerDetail;
  let fixture: ComponentFixture<CustomerDetail>;
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
    createdAt: '2026-08-31T10:00:00',
    updatedAt: '2026-08-31T12:30:00',
  };

  const customerService = {
    findFromListById: vi.fn().mockReturnValue(of(customer)),
  };

  beforeEach(async () => {
    routeId = '1';
    customerService.findFromListById.mockReset();
    customerService.findFromListById.mockReturnValue(of(customer));

    await TestBed.configureTestingModule({
      imports: [CustomerDetail],
      providers: [
        provideRouter([]),
        { provide: CustomerService, useValue: customerService },
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
    fixture = TestBed.createComponent(CustomerDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('should load and render the selected customer', () => {
    createComponent();

    const text = fixture.nativeElement.textContent as string;

    expect(customerService.findFromListById).toHaveBeenCalledWith(1);
    expect(text).toContain('Ana Gómez');
    expect(text).not.toContain('#1');
    expect(text).toContain('30111222');
    expect(text).toContain('11 4444-5555');
    expect(text).toContain('ana@email.com');
    expect(text).toContain('Av. Siempre Viva 742');
    expect(text).toContain('Cliente habitual');
  });

  it('should show an error for an invalid route id', () => {
    routeId = 'abc';

    createComponent();

    expect(customerService.findFromListById).not.toHaveBeenCalled();
    expect(component.errorMessage()).toBe('El identificador del cliente no es válido');
  });

  it('should show an error when the customer does not exist', () => {
    customerService.findFromListById.mockReturnValue(of(undefined));

    createComponent();

    expect(component.errorMessage()).toBe('No se encontró el cliente solicitado');
  });

  it('should show an error when loading fails', () => {
    customerService.findFromListById.mockReturnValue(
      throwError(() => ({ error: { message: 'Error de carga' } })),
    );

    createComponent();

    expect(component.errorMessage()).toBe('Error de carga');
  });
});
