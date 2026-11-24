import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ROLES, ROLE_LABELS, Role, User } from '../core/models';
import { errorMessage } from '../core/util';
import { ErrorAlertComponent } from '../shared/error-alert.component';
import { ModalComponent } from '../shared/modal.component';
import { PageHeaderComponent } from '../shared/page-header.component';
import { UsersService } from './users.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [FormsModule, DatePipe, PageHeaderComponent, ModalComponent, ErrorAlertComponent],
  template: `
    <app-page-header title="Users" subtitle="People who can sign in to this workspace, and what they can do.">
      <button class="btn btn-primary" (click)="openForm()">New user</button>
    </app-page-header>
    <app-error-alert [message]="error" />

    <div class="card overflow-hidden">
      <table class="data-table">
        <thead><tr><th>Username</th><th>Name</th><th>Email</th><th>Roles</th><th>Since</th></tr></thead>
        <tbody>
          @for (user of users; track user.id) {
            <tr>
              <td class="font-medium">{{ user.username }}</td>
              <td>{{ user.fullName }}</td>
              <td>{{ user.email }}</td>
              <td>
                @for (role of user.roles; track role) {
                  <span class="mr-1 inline-flex rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-700">{{ labels[role] }}</span>
                }
              </td>
              <td class="text-slate-500">{{ user.createdAt | date: 'mediumDate' }}</td>
            </tr>
          }
        </tbody>
      </table>
    </div>

    <app-modal [open]="showForm" title="New user" (closed)="showForm = false">
      <form class="space-y-4" (ngSubmit)="save()">
        <app-error-alert [message]="formError" />
        <div class="grid grid-cols-2 gap-4">
          <label class="block"><span class="label">Username</span><input class="input" name="username" [(ngModel)]="form.username" required /></label>
          <label class="block"><span class="label">Full name</span><input class="input" name="fullName" [(ngModel)]="form.fullName" /></label>
        </div>
        <label class="block"><span class="label">Email</span><input class="input" type="email" name="email" [(ngModel)]="form.email" required /></label>
        <label class="block">
          <span class="label">Temporary password</span>
          <input class="input" type="password" name="password" [(ngModel)]="form.password" required minlength="8" autocomplete="new-password" />
        </label>
        <fieldset>
          <legend class="label">Roles</legend>
          <div class="grid grid-cols-2 gap-2">
            @for (role of roles; track role) {
              <label class="flex items-center gap-2 text-sm">
                <input type="checkbox" [name]="'role-' + role" [ngModel]="selected.has(role)" (ngModelChange)="toggleRole(role, $event)" />
                {{ labels[role] }}
              </label>
            }
          </div>
        </fieldset>
        <div class="flex justify-end gap-2 pt-2">
          <button type="button" class="btn btn-secondary" (click)="showForm = false">Cancel</button>
          <button class="btn btn-primary" [disabled]="saving">Create user</button>
        </div>
      </form>
    </app-modal>
  `,
})
export class UsersComponent implements OnInit {
  private readonly usersService = inject(UsersService);

  readonly roles = ROLES;
  readonly labels = ROLE_LABELS;
  users: User[] = [];
  error: string | null = null;
  showForm = false;
  form = { username: '', fullName: '', email: '', password: '' };
  selected = new Set<Role>();
  formError: string | null = null;
  saving = false;

  ngOnInit(): void {
    this.load();
  }

  openForm(): void {
    this.form = { username: '', fullName: '', email: '', password: '' };
    this.selected = new Set<Role>(['SALES_REP']);
    this.formError = null;
    this.showForm = true;
  }

  toggleRole(role: Role, checked: boolean): void {
    if (checked) {
      this.selected.add(role);
    } else {
      this.selected.delete(role);
    }
  }

  save(): void {
    if (this.selected.size === 0) {
      this.formError = 'Give the user at least one role.';
      return;
    }
    this.saving = true;
    this.usersService.createUser({ ...this.form, roles: [...this.selected] }).subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.load();
      },
      error: (e: unknown) => {
        this.saving = false;
        this.formError = errorMessage(e);
      },
    });
  }

  private load(): void {
    this.usersService.users().subscribe({ next: (u) => (this.users = u), error: (e: unknown) => (this.error = errorMessage(e)) });
  }
}
