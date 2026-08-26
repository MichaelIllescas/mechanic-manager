import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { SupplyService } from '../supply-service';
import { SupplyList } from './supply-list';

describe('SupplyList', () => {
  let component: SupplyList;
  let fixture: ComponentFixture<SupplyList>;

  const supplyService = {
    findAll: vi.fn().mockReturnValue(of([])),
    updateStatus: vi.fn(),
  };

  beforeEach(async () => {
    supplyService.findAll.mockReset();
    supplyService.findAll.mockReturnValue(of([]));
    supplyService.updateStatus.mockReset();

    await TestBed.configureTestingModule({
      imports: [SupplyList],
      providers: [
        provideHttpClient(),
        provideRouter([]),
        { provide: SupplyService, useValue: supplyService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SupplyList);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads all supplies by default', () => {
    expect(supplyService.findAll).toHaveBeenCalledWith(undefined);
  });
});
