import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { AppFooter } from '../app-footer/app-footer';
import { Navbar } from '../navbar/navbar';

@Component({
  selector: 'app-main-layout',
  imports: [AppFooter, Navbar, RouterOutlet],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.css',
})
export class MainLayout {}
