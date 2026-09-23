import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { ConfirmService } from '../../../core/confirm/confirm-service';

@Component({
  selector: 'app-confirm-dialog',
  imports: [],
  templateUrl: './confirm-dialog.html',
  styleUrl: './confirm-dialog.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '(document:keydown.escape)': 'cancel()',
  },
})
export class ConfirmDialog {
  private readonly confirmService = inject(ConfirmService);

  readonly current = this.confirmService.current;

  confirm(): void {
    this.confirmService.respond(true);
  }

  cancel(): void {
    this.confirmService.respond(false);
  }
}
