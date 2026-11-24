import { Component, EventEmitter, Input, Output } from '@angular/core';

const WIDTHS = { md: 'max-w-lg', lg: 'max-w-3xl', xl: 'max-w-5xl' } as const;

@Component({
  selector: 'app-modal',
  standalone: true,
  template: `
    @if (open) {
      <div
        class="fixed inset-0 z-40 flex items-start justify-center overflow-y-auto bg-slate-900/40 p-4 sm:p-10"
        (click)="closed.emit()"
      >
        <div
          class="w-full rounded-xl bg-white shadow-xl"
          [class]="widthClass"
          role="dialog"
          aria-modal="true"
          (click)="$event.stopPropagation()"
        >
          <div class="flex items-center justify-between border-b border-slate-200 px-6 py-4">
            <h2 class="text-lg font-semibold text-slate-900">{{ title }}</h2>
            <button type="button" class="text-xl leading-none text-slate-400 hover:text-slate-600" aria-label="Close" (click)="closed.emit()">
              &times;
            </button>
          </div>
          <div class="px-6 py-5"><ng-content /></div>
        </div>
      </div>
    }
  `,
})
export class ModalComponent {
  @Input() open = false;
  @Input() title = '';
  @Input() size: keyof typeof WIDTHS = 'md';
  @Output() readonly closed = new EventEmitter<void>();

  get widthClass(): string {
    return WIDTHS[this.size];
  }
}
