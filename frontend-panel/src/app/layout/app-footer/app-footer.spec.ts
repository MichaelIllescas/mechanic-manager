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

  it('renders the author attribution with a link to GitHub', () => {
    const links: HTMLAnchorElement[] = Array.from(fixture.nativeElement.querySelectorAll('a'));

    expect(fixture.nativeElement.textContent).toContain('Desarrollado por');
    expect(fixture.nativeElement.textContent).toContain('Jonathan Illescas');
    expect(links.map((link) => link.href)).toEqual(['https://github.com/MichaelIllescas']);
  });
});
