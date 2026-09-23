import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { ConfirmDialog } from './shared/components/confirm-dialog/confirm-dialog';
import { Notifications } from './shared/components/notifications/notifications';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Notifications, ConfirmDialog],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {}
