import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { CustomerService } from '../customer-service';
import { Customer } from '../customer.models';
import { CustomerList } from './customer-list';

describe('CustomerList', () => {
  let component: CustomerList;
  let fixture: ComponentFixture<CustomerList>;

  const customers: Customer[] = [
    {
      id: 1,
      firstName: 'Ana',
      lastName: 'Gómez',
      documentNumber: '30111222',
      phone: '11 4444-5555',
      email: 'ana@email.com',
      notes: null,
      createdAt: null,
      updatedAt: null,
    },
  ];

  const customerService = {
    findAll: vi.fn().mockReturnValue(of(customers)),
  };

  beforeEach(async () => {
    customerService.findAll.mockReset();
    customerService.findAll.mockReturnValue(of(customers));

    await TestBed.configureTestingModule({
      imports: [CustomerList],
      providers: [provideRouter([]), { provide: CustomerService, useValue: customerService }],
    }).compileComponents();

    fixture = TestBed.createComponent(CustomerList);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await waitForSearch();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads customers on init', () => {
    expect(customerService.findAll).toHaveBeenCalledWith('');
    expect(component.customers()).toEqual(customers);
  });

  it('searches customers after the debounce time', async () => {
    component.searchControl.setValue('Ana');

    await waitForSearch();

    expect(customerService.findAll).toHaveBeenLastCalledWith('Ana');
  });

  function waitForSearch(): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, 350));
  }
});
