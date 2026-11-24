import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-error-alert',
  standalone: true,
  template: `
    @if (message) {
      <div class="mb-4 rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700" role="alert">
        {{ message }}
      </div>
    }
  `,
})
export class ErrorAlertComponent {
  @Input() message: string | null = null;
}
