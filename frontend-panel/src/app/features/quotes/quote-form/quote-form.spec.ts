import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { NotificationService } from '../../../core/notifications/notification-service';
import { CustomerService } from '../../customers/customer-service';
import { ServiceTypeService } from '../../service-types/service-type-service';
import { SupplyService } from '../../supplies/supply-service';
import { VehicleService } from '../../vehicles/vehicle-service';
import { QuoteService } from '../quote-service';
import { Quote } from '../quote.models';
import { QuoteForm } from './quote-form';

@Component({ template: '' })
class TestQuoteDetail {}

describe('QuoteForm', () => {
  let component: QuoteForm;
  let fixture: ComponentFixture<QuoteForm>;
  let routeId: string | null;

  const quote: Quote = {
    id: 15,
    number: 24,
    customerId: 1,
    customerName: 'Ana Gómez',
    vehicleId: 8,
    vehicleLicensePlate: 'AB123CD',
    vehicleDescription: 'Toyota Corolla',
    validUntil: null,
    status: 'APPROVED',
    total: 10000,
    notes: null,
    items: [
      {
        id: 31,
        serviceTypeId: 10,
        supplyId: null,
        description: 'Servicio',
        quantity: 1,
        unitPrice: 10000,
        subtotal: 10000,
      },
    ],
    createdAt: '2026-09-07T10:00:00',
    updatedAt: '2026-09-07T10:00:00',
  };
  const quoteService = {
    findById: vi.fn(() => of(quote)),
    create: vi.fn(() => of(quote)),
    update: vi.fn(() => of(quote)),
  };
  const customerService = { findAll: vi.fn(() => of([])), create: vi.fn() };
  const vehicleService = { findAll: vi.fn(() => of([])), create: vi.fn() };
  const serviceTypeService = { findAll: vi.fn(() => of([])), create: vi.fn() };
  const supplyService = { findAll: vi.fn(() => of([])), create: vi.fn() };
  const notifications = { success: vi.fn() };

  beforeEach(async () => {
    routeId = null;
    Object.values(quoteService).forEach((mock) => mock.mockClear());
    notifications.success.mockClear();
    await TestBed.configureTestingModule({
      imports: [QuoteForm],
      providers: [
        provideRouter([{ path: 'quotes/:id', component: TestQuoteDetail }]),
        { provide: QuoteService, useValue: quoteService },
        { provide: CustomerService, useValue: customerService },
        { provide: VehicleService, useValue: vehicleService },
        { provide: ServiceTypeService, useValue: serviceTypeService },
        { provide: SupplyService, useValue: supplyService },
        { provide: NotificationService, useValue: notifications },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => routeId } } } },
      ],
    }).compileComponents();
  });

  function createComponent(): void {
    fixture = TestBed.createComponent(QuoteForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  function addItem(): void {
    component.items.push(
      new FormGroup({
        serviceTypeId: new FormControl<number | null>(10),
        supplyId: new FormControl<number | null>(null),
        description: new FormControl('Servicio'),
        quantity: new FormControl(1),
        unitPrice: new FormControl(10000),
      }),
    );
  }

  it('creates the lightweight form container', () => {
    createComponent();
    expect(component).toBeTruthy();
  });

  it('creates a quote using data supplied by its child sections', () => {
    createComponent();
    component.form.patchValue({
      customerId: 1,
      vehicleId: 8,
      validUntil: '2026-09-30',
      notes: ' Revisar ',
    });
    addItem();
    component.onSubmit();
    expect(quoteService.create).toHaveBeenCalledWith({
      customerId: 1,
      vehicleId: 8,
      validUntil: '2026-09-30',
      notes: 'Revisar',
      items: [
        {
          serviceTypeId: 10,
          supplyId: null,
          description: 'Servicio',
          quantity: 1,
          unitPrice: 10000,
        },
      ],
    });
  });

  it('loads and updates a quote regardless of its approved status', () => {
    routeId = '15';
    createComponent();
    expect(component.isEdit()).toBe(true);
    expect(component.customerLabel()).toBe('Ana Gómez');
    expect(component.items.length).toBe(1);
    component.form.controls.notes.setValue('Corrección');
    component.onSubmit();
    expect(quoteService.update).toHaveBeenCalledWith(
      15,
      expect.objectContaining({ notes: 'Corrección' }),
    );
  });

  it('does not submit without customer, vehicle and items', () => {
    createComponent();
    component.onSubmit();
    expect(quoteService.create).not.toHaveBeenCalled();
    expect(component.errorMessage()).toContain('Agregá al menos');
  });
});
