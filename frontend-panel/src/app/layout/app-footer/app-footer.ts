import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  templateUrl: './app-footer.html',
  styleUrl: './app-footer.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppFooter {
  readonly websiteUrl = 'https://www.imperial-net.com/';
  readonly whatsappUrl = 'https://wa.me/5491100000000';
  readonly whatsappLabel = '+54 9 11 0000-0000';
}
