import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { AppFooter } from '../app-footer/app-footer';
import { Navbar } from '../navbar/navbar';

@Component({
  selector: 'app-admin-layout',
  imports: [AppFooter, Navbar, RouterOutlet],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.css',
})
export class AdminLayout {}
