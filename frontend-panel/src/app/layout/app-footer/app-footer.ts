import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  templateUrl: './app-footer.html',
  styleUrl: './app-footer.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppFooter {
  readonly authorName = 'Jonathan Illescas';
  readonly authorUrl = 'https://github.com/MichaelIllescas';
}
