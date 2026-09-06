import { DOCUMENT } from '@angular/common';
import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
  inject,
  input,
  output,
  viewChild,
} from '@angular/core';

let openModalCount = 0;

@Component({
  selector: 'app-modal',
  imports: [],
  templateUrl: './modal.html',
  styleUrl: './modal.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '(document:keydown.escape)': 'onEscape()',
  },
})
export class Modal implements AfterViewInit, OnDestroy {
  private readonly document = inject(DOCUMENT);

  readonly title = input.required<string>();
  readonly subtitle = input<string | null>(null);
  readonly closeLabel = input('Cerrar');
  readonly dismissible = input(true);

  readonly closed = output<void>();

  private readonly dialog = viewChild.required<ElementRef<HTMLElement>>('dialog');
  private previouslyFocused: HTMLElement | null = null;

  ngAfterViewInit(): void {
    this.previouslyFocused = this.document.activeElement as HTMLElement | null;

    openModalCount += 1;
    this.document.body.classList.add('modal-open');

    this.focusableElements()[0]?.focus();
  }

  ngOnDestroy(): void {
    openModalCount = Math.max(0, openModalCount - 1);

    if (openModalCount === 0) {
      this.document.body.classList.remove('modal-open');
    }

    this.previouslyFocused?.focus();
  }

  onEscape(): void {
    this.requestClose();
  }

  onBackdropClick(): void {
    this.requestClose();
  }

  onTab(event: KeyboardEvent): void {
    const focusable = this.focusableElements();

    if (focusable.length === 0) {
      return;
    }

    const first = focusable[0];
    const last = focusable[focusable.length - 1];
    const active = this.document.activeElement;

    if (event.shiftKey && active === first) {
      event.preventDefault();
      last.focus();
      return;
    }

    if (!event.shiftKey && active === last) {
      event.preventDefault();
      first.focus();
    }
  }

  requestClose(): void {
    if (!this.dismissible()) {
      return;
    }

    this.closed.emit();
  }

  private focusableElements(): HTMLElement[] {
    const selector =
      'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

    return Array.from(this.dialog().nativeElement.querySelectorAll<HTMLElement>(selector)).filter(
      (element) => element.offsetParent !== null || element === this.document.activeElement,
    );
  }
}
