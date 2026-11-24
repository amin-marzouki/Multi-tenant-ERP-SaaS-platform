import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../core/auth.service';
import { TenantService } from '../core/tenant.service';
import { errorMessage } from '../core/util';
import { ErrorAlertComponent } from '../shared/error-alert.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink, ErrorAlertComponent],
  template: `
    <div class="flex min-h-screen items-center justify-center p-6">
      <div class="w-full max-w-sm">
        <div class="mb-8 text-center">
          <div class="text-3xl font-semibold text-slate-900">OrbitERP</div>
          <p class="mt-1 text-sm text-slate-500">Sign in to your workspace</p>
        </div>
        <form class="card space-y-4 p-6" (ngSubmit)="submit()">
          <app-error-alert [message]="error" />
          <label class="block">
            <span class="label">Workspace</span>
            <input class="input" name="tenantCode" [(ngModel)]="form.tenantCode" [readonly]="fromSubdomain" required autocomplete="organization" />
          </label>
          <label class="block">
            <span class="label">Username</span>
            <input class="input" name="username" [(ngModel)]="form.username" required autocomplete="username" />
          </label>
          <label class="block">
            <span class="label">Password</span>
            <input class="input" type="password" name="password" [(ngModel)]="form.password" required autocomplete="current-password" />
          </label>
          <button class="btn btn-primary w-full" [disabled]="loading">{{ loading ? 'Signing in…' : 'Sign in' }}</button>
        </form>
        <p class="mt-4 text-center text-sm text-slate-500">
          New company? <a routerLink="/signup" class="font-medium text-indigo-600 hover:text-indigo-500">Create a workspace</a>
        </p>
      </div>
    </div>
  `,
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly subdomain = inject(TenantService).subdomainTenant();

  readonly fromSubdomain = this.subdomain !== '';
  form = { tenantCode: this.subdomain, username: '', password: '' };
  error: string | null = null;
  loading = false;

  submit(): void {
    this.error = null;
    this.loading = true;
    this.auth
      .login({ ...this.form, tenantCode: this.form.tenantCode.trim().toLowerCase() })
      .subscribe({
        next: () => void this.router.navigateByUrl('/'),
        error: (err: unknown) => {
          this.error = errorMessage(err);
          this.loading = false;
        },
      });
  }
}
