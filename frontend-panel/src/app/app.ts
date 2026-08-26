import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { Notifications } from './shared/components/notifications/notifications';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Notifications],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {}
