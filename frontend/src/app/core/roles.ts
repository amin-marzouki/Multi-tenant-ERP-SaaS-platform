import { Role } from './models';

// Who can use which part of the app; mirrors the backend's Access rules.
export const SALES: Role[] = ['SALES_REP', 'ADMIN'];
export const WAREHOUSE: Role[] = ['WAREHOUSE_CLERK', 'ADMIN'];
export const FINANCE: Role[] = ['ACCOUNTANT', 'ADMIN'];
export const SALES_OR_FINANCE: Role[] = ['SALES_REP', 'ACCOUNTANT', 'ADMIN'];
export const ADMIN: Role[] = ['ADMIN'];
export const ANY_ROLE: Role[] = [];
