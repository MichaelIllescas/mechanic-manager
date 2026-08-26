import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceTypeList } from './service-type-list';

describe('ServiceTypeList', () => {
  let component: ServiceTypeList;
  let fixture: ComponentFixture<ServiceTypeList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ServiceTypeList],
    }).compileComponents();

    fixture = TestBed.createComponent(ServiceTypeList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
