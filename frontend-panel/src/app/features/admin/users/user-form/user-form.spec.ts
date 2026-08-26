import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { UserForm } from './user-form';
import { UserService } from '../user-service';

describe('UserForm', () => {
  let component: UserForm;
  let fixture: ComponentFixture<UserForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserForm],
      providers: [
        provideRouter([]),
        {
          provide: UserService,
          useValue: {
            create: () => of({}),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UserForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
