import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppFooter } from './app-footer';

describe('AppFooter', () => {
  let fixture: ComponentFixture<AppFooter>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppFooter],
    }).compileComponents();

    fixture = TestBed.createComponent(AppFooter);
    fixture.detectChanges();
  });

  function queryAll(selector: string): HTMLAnchorElement[] {
    return Array.from(fixture.nativeElement.querySelectorAll(selector));
  }

  it('renders Imperial Net attribution and contact links', () => {
    const links = queryAll('a');

    expect(fixture.nativeElement.textContent).toContain('Desarrollado por');
    expect(fixture.nativeElement.textContent).toContain('Imperial Net');
    expect(fixture.nativeElement.textContent).toContain('+54 9 11 0000-0000');
    expect(links.map((link) => link.href)).toEqual([
      'https://www.imperial-net.com/',
      'https://www.imperial-net.com/',
      'https://wa.me/5491100000000',
    ]);
  });
});
