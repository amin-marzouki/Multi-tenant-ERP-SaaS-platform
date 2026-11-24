// Shapes of the backend API. Dates are ISO strings ("2026-11-24"); amounts are numbers.

export type Role = 'ADMIN' | 'SALES_REP' | 'WAREHOUSE_CLERK' | 'ACCOUNTANT';

export const ROLES: Role[] = ['ADMIN', 'SALES_REP', 'WAREHOUSE_CLERK', 'ACCOUNTANT'];

export const ROLE_LABELS: Record<Role, string> = {
  ADMIN: 'Admin',
  SALES_REP: 'Sales rep',
  WAREHOUSE_CLERK: 'Warehouse clerk',
  ACCOUNTANT: 'Accountant',
};

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, unknown>;
  traceId?: string;
}

// ---- Auth ----

export interface UserInfo {
  id: number;
  username: string;
  fullName: string | null;
  email: string;
  tenantCode: string;
  tenantName: string;
  roles: Role[];
}

export interface LoginRequest {
  tenantCode: string;
  username: string;
  password: string;
}

export interface SignupRequest {
  tenantCode: string;
  companyName: string;
  fullName: string;
  username: string;
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  expiresAt: string;
  user: UserInfo;
}

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string | null;
  enabled: boolean;
  roles: Role[];
  createdAt: string;
}

export interface CreateUserRequest {
  username: string;
  email: string;
  fullName: string;
  password: string;
  roles: Role[];
}

// ---- Sales ----

export interface Customer {
  id: number;
  code: string;
  name: string;
  email: string | null;
  phone: string | null;
  address: string | null;
}

export interface CustomerRequest {
  name: string;
  email: string;
  phone: string;
  address: string;
}

export interface LineRequest {
  itemId: number;
  description: string;
  quantity: number;
  unitPrice: number;
  taxRate: number;
}

export interface Line {
  id: number;
  lineNo: number;
  itemId: number;
  description: string;
  quantity: number;
  unitPrice: number;
  taxRate: number;
  netAmount: number;
  taxAmount: number;
}

export interface DocumentSummary {
  id: number;
  number: string;
  customerId: number;
  customerName: string;
  date: string;
  status: string;
  total: number;
}

export interface SalesDocument {
  id: number;
  number: string;
  customerId: number;
  customerName: string;
  notes: string | null;
  subtotal: number;
  taxTotal: number;
  total: number;
  lines: Line[];
}

export type QuoteStatus = 'DRAFT' | 'CONVERTED' | 'REJECTED';
export type OrderStatus = 'DRAFT' | 'CONFIRMED' | 'SHIPPED' | 'INVOICED' | 'CANCELLED';
export type InvoiceStatus = 'DRAFT' | 'POSTED' | 'PAID';

export interface Quote extends SalesDocument {
  quoteDate: string;
  validUntil: string | null;
  status: QuoteStatus;
  orderId: number | null;
}

export interface SalesOrder extends SalesDocument {
  orderDate: string;
  status: OrderStatus;
  quoteId: number | null;
  invoiceId: number | null;
}

export interface Invoice extends SalesDocument {
  issueDate: string;
  dueDate: string;
  status: InvoiceStatus;
  orderId: number | null;
}

export interface QuoteRequest {
  customerId: number;
  quoteDate: string | null;
  validUntil: string | null;
  notes: string;
  lines: LineRequest[];
}

export interface OrderRequest {
  customerId: number;
  orderDate: string | null;
  notes: string;
  lines: LineRequest[];
}

export interface InvoiceRequest {
  issueDate: string | null;
  dueDate: string | null;
}

// ---- Inventory ----

export interface Item {
  id: number;
  sku: string;
  name: string;
  description: string | null;
  unit: string;
  salePrice: number;
  active: boolean;
  onHand: number;
  available: number;
}

export interface ItemRequest {
  sku: string;
  name: string;
  description: string;
  unit: string;
  salePrice: number;
  active: boolean;
}

export interface Warehouse {
  id: number;
  code: string;
  name: string;
  defaultWarehouse: boolean;
}

export interface WarehouseRequest {
  code: string;
  name: string;
  defaultWarehouse: boolean;
}

export interface StockLevel {
  id: number;
  itemId: number;
  sku: string;
  itemName: string;
  warehouseId: number;
  warehouseCode: string;
  onHand: number;
  reserved: number;
  available: number;
}

export type StockMoveType = 'RECEIPT' | 'ISSUE' | 'ADJUSTMENT';

export interface StockMove {
  id: number;
  movedAt: string;
  type: StockMoveType;
  itemId: number;
  sku: string;
  itemName: string;
  warehouseCode: string;
  quantity: number;
  reference: string | null;
  note: string | null;
  createdBy: string | null;
}

export interface StockMoveRequest {
  type: StockMoveType;
  itemId: number;
  warehouseId: number | null;
  quantity: number;
  reference: string;
  note: string;
}

// ---- Finance ----

export type AccountType = 'ASSET' | 'LIABILITY' | 'EQUITY' | 'REVENUE' | 'EXPENSE';
export const ACCOUNT_TYPES: AccountType[] = ['ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE'];

export interface Account {
  id: number;
  code: string;
  name: string;
  type: AccountType;
  active: boolean;
}

export interface AccountRequest {
  code: string;
  name: string;
  type: AccountType;
}

export type JournalSource = 'MANUAL' | 'INVOICE' | 'PAYMENT';

export interface JournalLine {
  id: number;
  lineNo: number;
  accountId: number;
  accountCode: string;
  accountName: string;
  description: string | null;
  debit: number;
  credit: number;
}

export interface JournalEntrySummary {
  id: number;
  number: string;
  entryDate: string;
  description: string;
  sourceType: JournalSource;
  sourceReference: string | null;
  amount: number;
}

export interface JournalEntry extends JournalEntrySummary {
  lines: JournalLine[];
}

export interface JournalEntryRequest {
  entryDate: string | null;
  description: string;
  lines: { accountId: number; description: string; debit: number; credit: number }[];
}

export type ReceivableStatus = 'OPEN' | 'PAID';

export interface Receivable {
  id: number;
  invoiceId: number;
  invoiceNumber: string;
  customerId: number;
  customerName: string;
  issueDate: string;
  dueDate: string;
  amount: number;
  openAmount: number;
  status: ReceivableStatus;
  daysOverdue: number;
}

export interface AgingBuckets {
  current: number;
  days1to30: number;
  days31to60: number;
  days61to90: number;
  over90: number;
  total: number;
}

export interface AgingReport {
  asOf: string;
  rows: { customerId: number; customerName: string; buckets: AgingBuckets }[];
  totals: AgingBuckets;
}

export type PaymentMethod = 'CASH' | 'BANK_TRANSFER' | 'CHECK' | 'CARD';
export const PAYMENT_METHODS: PaymentMethod[] = ['BANK_TRANSFER', 'CASH', 'CHECK', 'CARD'];

export interface Payment {
  id: number;
  receivableId: number;
  invoiceNumber: string;
  customerName: string;
  paymentDate: string;
  amount: number;
  method: PaymentMethod;
  reference: string | null;
  journalEntryId: number;
}

export interface PaymentRequest {
  receivableId: number;
  paymentDate: string | null;
  amount: number;
  method: PaymentMethod;
  reference: string;
}

export interface TrialBalanceRow {
  accountId: number;
  code: string;
  name: string;
  type: AccountType;
  debit: number;
  credit: number;
  balance: number;
}

export interface TrialBalance {
  rows: TrialBalanceRow[];
  totalDebit: number;
  totalCredit: number;
}
