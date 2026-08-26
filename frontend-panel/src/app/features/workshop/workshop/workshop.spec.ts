import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { NotificationService } from '../../../core/notifications/notification-service';
import { Workshop as WorkshopModel } from '../workshop.models';
import { WorkshopService } from '../workshop-service';
import { Workshop } from './workshop';

describe('Workshop', () => {
  let component: Workshop;
  let fixture: ComponentFixture<Workshop>;
  let workshopService: {
    findMine: ReturnType<typeof vi.fn>;
    update: ReturnType<typeof vi.fn>;
  };
  let notifications: {
    success: ReturnType<typeof vi.fn>;
  };

  const incompleteWorkshop: WorkshopModel = {
    id: 1,
    name: null,
    phone: null,
    email: null,
    address: null,
    notes: null,
    complete: false,
    createdAt: '2026-08-26T10:00:00',
    updatedAt: '2026-08-26T10:00:00',
  };

  const completeWorkshop: WorkshopModel = {
    ...incompleteWorkshop,
    name: 'Taller Norte',
    phone: '123456',
    email: 'taller@example.com',
    address: 'Av. Siempre Viva 123',
    notes: 'Abre de lunes a viernes',
    complete: true,
  };

  beforeEach(async () => {
    workshopService = {
      findMine: vi.fn().mockReturnValue(of(incompleteWorkshop)),
      update: vi.fn().mockReturnValue(of(completeWorkshop)),
    };
    notifications = {
      success: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [Workshop],
      providers: [
        { provide: WorkshopService, useValue: workshopService },
        { provide: NotificationService, useValue: notifications },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Workshop);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  it('should show the register button when the workshop is incomplete', () => {
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;

    expect(text).toContain('Todavía no registraste los datos del taller');
    expect(text).toContain('Registrar datos');
    expect(fixture.nativeElement.querySelector('form')).toBeNull();
  });

  it('should open the form when the user clicks register data', () => {
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('.workshop__primary-button') as HTMLButtonElement;
    button.click();
    fixture.detectChanges();

    expect(component.showForm()).toBe(true);
    expect(fixture.nativeElement.querySelector('form')).not.toBeNull();
  });

  it('should show workshop details when the workshop is complete', () => {
    workshopService.findMine.mockReturnValue(of(completeWorkshop));

    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;

    expect(text).toContain('Taller registrado');
    expect(text).toContain('Taller Norte');
    expect(text).toContain('Editar datos');
    expect(fixture.nativeElement.querySelector('form')).toBeNull();
  });

  it('should update the workshop and return to details mode', () => {
    fixture.detectChanges();

    component.onRegisterClick();
    component.form.patchValue({
      name: ' Taller Norte ',
      phone: '123456',
      email: 'taller@example.com',
      address: '',
      notes: '',
    });
    component.onSubmit();

    expect(workshopService.update).toHaveBeenCalledWith({
      name: 'Taller Norte',
      phone: '123456',
      email: 'taller@example.com',
      address: null,
      notes: null,
    });
    expect(component.workshop()).toEqual(completeWorkshop);
    expect(component.showForm()).toBe(false);
    expect(notifications.success).toHaveBeenCalledWith('Datos del taller actualizados correctamente');
  });
});
