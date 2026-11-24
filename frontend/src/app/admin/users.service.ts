import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { CreateUserRequest, User } from '../core/models';

@Injectable({ providedIn: 'root' })
export class UsersService {
  private readonly http = inject(HttpClient);

  users(): Observable<User[]> {
    return this.http.get<User[]>('/api/users');
  }

  createUser(request: CreateUserRequest): Observable<User> {
    return this.http.post<User>('/api/users', request);
  }
}
