import { Component, Input } from '@angular/core';

const COLORS: Record<string, string> = {
  DRAFT: 'bg-slate-100 text-slate-700',
  CONFIRMED: 'bg-sky-100 text-sky-700',
  SHIPPED: 'bg-indigo-100 text-indigo-700',
  INVOICED: 'bg-violet-100 text-violet-700',
  POSTED: 'bg-sky-100 text-sky-700',
  OPEN: 'bg-amber-100 text-amber-800',
  PAID: 'bg-emerald-100 text-emerald-700',
  CONVERTED: 'bg-emerald-100 text-emerald-700',
  REJECTED: 'bg-rose-100 text-rose-700',
  CANCELLED: 'bg-rose-100 text-rose-700',
  RECEIPT: 'bg-emerald-100 text-emerald-700',
  ISSUE: 'bg-indigo-100 text-indigo-700',
  ADJUSTMENT: 'bg-amber-100 text-amber-800',
  MANUAL: 'bg-slate-100 text-slate-700',
  INVOICE: 'bg-violet-100 text-violet-700',
  PAYMENT: 'bg-emerald-100 text-emerald-700',
};

@Component({
  selector: 'app-status-badge',
  standalone: true,
  template: `<span class="inline-flex rounded-full px-2 py-0.5 text-xs font-medium" [class]="color">{{ label }}</span>`,
})
export class StatusBadgeComponent {
  @Input({ required: true }) status = '';

  get color(): string {
    return COLORS[this.status] ?? 'bg-slate-100 text-slate-700';
  }

  get label(): string {
    const text = this.status.toLowerCase().replace(/_/g, ' ');
    return text.charAt(0).toUpperCase() + text.slice(1);
  }
}
