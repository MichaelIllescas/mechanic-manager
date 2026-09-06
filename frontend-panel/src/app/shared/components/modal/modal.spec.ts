import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Modal } from './modal';

@Component({
  imports: [Modal],
  template: `
    <app-modal [title]="'Nuevo cliente'" [dismissible]="dismissible" (closed)="closed = closed + 1">
      <button type="button" class="inner">Aceptar</button>
    </app-modal>
  `,
})
class TestHost {
  dismissible = true;
  closed = 0;
}

describe('Modal', () => {
  let fixture: ComponentFixture<TestHost>;
  let host: TestHost;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHost],
    }).compileComponents();

    fixture = TestBed.createComponent(TestHost);
    host = fixture.componentInstance;
  });

  function render(): void {
    fixture.detectChanges();
  }

  function query(selector: string): HTMLElement {
    return fixture.nativeElement.querySelector(selector) as HTMLElement;
  }

  it('renders the title and the projected content', () => {
    render();

    expect(query('.modal__title').textContent).toContain('Nuevo cliente');
    expect(query('.inner')).toBeTruthy();
  });

  it('locks the body scroll while open and releases it on destroy', () => {
    render();

    expect(document.body.classList.contains('modal-open')).toBe(true);

    fixture.destroy();

    expect(document.body.classList.contains('modal-open')).toBe(false);
  });

  it('emits closed from the backdrop, the close button and Escape', () => {
    render();

    query('.modal__backdrop').click();
    query('.modal__close').click();
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    fixture.detectChanges();

    expect(host.closed).toBe(3);
  });

  it('does not close when it is not dismissible', () => {
    host.dismissible = false;
    render();

    query('.modal__backdrop').click();
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    fixture.detectChanges();

    expect(query('.modal__close')).toBeNull();
    expect(host.closed).toBe(0);
  });
});
