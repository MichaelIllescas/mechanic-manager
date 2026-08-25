import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { Navbar} from '../navbar/navbar';

@Component({
  selector: 'app-main-layout',
  imports: [Navbar, RouterOutlet ],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.css',
})
export class MainLayout {}
