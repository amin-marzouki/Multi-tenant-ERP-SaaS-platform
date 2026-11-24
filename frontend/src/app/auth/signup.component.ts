import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../core/auth.service';
import { SignupRequest } from '../core/models';
import { errorMessage } from '../core/util';
import { ErrorAlertComponent } from '../shared/error-alert.component';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [FormsModule, RouterLink, ErrorAlertComponent],
  template: `
    <div class="flex min-h-screen items-center justify-center p-6">
      <div class="w-full max-w-md">
        <div class="mb-8 text-center">
          <div class="text-3xl font-semibold text-slate-900">Create a workspace</div>
          <p class="mt-1 text-sm text-slate-500">Your company gets its own OrbitERP, and you become its admin.</p>
        </div>
        <form class="card space-y-4 p-6" (ngSubmit)="submit()">
          <app-error-alert [message]="error" />
          <label class="block">
            <span class="label">Company name</span>
            <input class="input" name="companyName" [(ngModel)]="form.companyName" (ngModelChange)="suggestCode()" required />
          </label>
          <label class="block">
            <span class="label">Workspace</span>
            <input class="input" name="tenantCode" [(ngModel)]="form.tenantCode" (ngModelChange)="codeTouched = true" required />
            <span class="mt-1 block text-xs text-slate-500">Lowercase letters, digits and dashes. You sign in with it.</span>
          </label>
          <div class="grid grid-cols-2 gap-4">
            <label class="block">
              <span class="label">Your name</span>
              <input class="input" name="fullName" [(ngModel)]="form.fullName" required />
            </label>
            <label class="block">
              <span class="label">Username</span>
              <input class="input" name="username" [(ngModel)]="form.username" required autocomplete="username" />
            </label>
          </div>
          <label class="block">
            <span class="label">Email</span>
            <input class="input" type="email" name="email" [(ngModel)]="form.email" required autocomplete="email" />
          </label>
          <label class="block">
            <span class="label">Password</span>
            <input class="input" type="password" name="password" [(ngModel)]="form.password" required minlength="8" autocomplete="new-password" />
            <span class="mt-1 block text-xs text-slate-500">At least 8 characters.</span>
          </label>
          <button class="btn btn-primary w-full" [disabled]="loading">{{ loading ? 'Creating…' : 'Create workspace' }}</button>
        </form>
        <p class="mt-4 text-center text-sm text-slate-500">
          Already have one? <a routerLink="/login" class="font-medium text-indigo-600 hover:text-indigo-500">Sign in</a>
        </p>
      </div>
    </div>
  `,
})
export class SignupComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  form: SignupRequest = { tenantCode: '', companyName: '', fullName: '', username: '', email: '', password: '' };
  codeTouched = false;
  error: string | null = null;
  loading = false;

  /** Proposes a workspace code from the company name until the user edits it. */
  suggestCode(): void {
    if (!this.codeTouched) {
      this.form.tenantCode = this.form.companyName
        .toLowerCase()
        .normalize('NFD')
        .replace(/[̀-ͯ]/g, '')
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/^-+|-+$/g, '')
        .slice(0, 50);
    }
  }

  submit(): void {
    this.error = null;
    this.loading = true;
    this.auth.signup({ ...this.form, tenantCode: this.form.tenantCode.trim().toLowerCase() }).subscribe({
      next: () => void this.router.navigateByUrl('/'),
      error: (err: unknown) => {
        this.error = errorMessage(err);
        this.loading = false;
      },
    });
  }
}
