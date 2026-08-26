import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceTypeForm } from './service-type-form';

describe('ServiceTypeForm', () => {
  let component: ServiceTypeForm;
  let fixture: ComponentFixture<ServiceTypeForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ServiceTypeForm],
    }).compileComponents();

    fixture = TestBed.createComponent(ServiceTypeForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
