import { Injectable, signal } from '@angular/core';

export interface ConfirmOptions {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  /** Resalta el botón de confirmar en rojo, para acciones destructivas (eliminar, etc). */
  danger?: boolean;
}

interface ConfirmRequest extends ConfirmOptions {
  resolve: (confirmed: boolean) => void;
}

@Injectable({
  providedIn: 'root',
})
export class ConfirmService {
  readonly current = signal<ConfirmRequest | null>(null);

  /** Se resuelve con `true` si el usuario confirma, `false` si cancela o cierra el modal. */
  confirm(options: ConfirmOptions): Promise<boolean> {
    return new Promise((resolve) => {
      this.current.set({ ...options, resolve });
    });
  }

  respond(confirmed: boolean): void {
    this.current()?.resolve(confirmed);
    this.current.set(null);
  }
}
