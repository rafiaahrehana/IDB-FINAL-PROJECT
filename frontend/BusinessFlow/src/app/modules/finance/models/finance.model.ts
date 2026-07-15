export interface ChartOfAccount {
  id: number;
  companyId: number;
  accountCode: string;
  accountName: string;
  type: string;
  balance: number;
  isHeaderAccount: boolean;
  allowDirectPosting: boolean;
  active: boolean;
  description?: string;
  notes?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface Expense {
  id: number;
  companyId: number;
  expenseNumber: string;
  submittedByName?: string;
  description: string;
  amount: number;
  vendorName?: string;
  category?: string;
  expenseDate: string;
  receiptUrl?: string;
  status: string;
  approvedByName?: string;
  approvalNotes?: string;
  submittedAt?: string;
  notes?: string;
  createdAt: string;
  title?: string;
  currency?: string;
  employeeId?: number;
  reimbursedDate?: string;
  reimbursementMethod?: string;
  referenceNumber?: string;
  updatedAt?: string;
}

// Matches the backend's actual ClientInvoiceResponse (not a generic "Invoice" -
// this platform only has client invoices). Field names below were previously out
// of sync with the server (claimed outstandingAmount/discountAmount, which don't
// exist; the real remaining-balance field is balanceAmount).
export interface InvoiceItem {
  id?: number;
  description: string;
  quantity: number;
  unitPrice: number;
  lineTotal?: number;
  notes?: string;
}

export interface Invoice {
  id: number;
  companyId?: number;
  invoiceNumber: string;
  clientId?: number;
  clientName?: string;
  invoiceDate?: string;
  dueDate?: string;
  items?: InvoiceItem[];
  subtotal: number;
  taxAmount: number;
  totalAmount: number;
  paidAmount: number;
  balanceAmount: number;
  status: string;
  paymentTerms?: string;
  description?: string;
  notes?: string;
  sentDate?: string;
  paidDate?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface InvoiceItemRequest {
  description: string;
  quantity: number;
  unitPrice: number;
  notes?: string;
}

export interface InvoiceRequest {
  clientId: number;
  invoiceDate: string;
  dueDate: string;
  items: InvoiceItemRequest[];
  taxAmount?: number;
  paymentTerms?: string;
  description?: string;
  notes?: string;
}

// NOTE: Vendor and VendorPayment were removed - there is no separate Vendor entity in the
// backend. Vendor identity is a plain free-text 'vendorName' field on Expense, and vendor
// payments now go through Expense's existing approve -> reject -> mark-as-paid lifecycle
// (see ExpenseService.getByVendor/markAsPaid and components/expenses).

export interface JournalEntry {
  id: number;
  companyId: number;
  journalEntryNumber: string;
  entryDate: string;
  debitAccountId: number;
  debitAccountName?: string;
  creditAccountId: number;
  creditAccountName?: string;
  amount: number;
  description?: string;
  notes?: string;
  createdBy?: string;
  createdDate?: string;
  approvedBy?: string;
  approvedDate?: string;
  approved: boolean;
  posted: boolean;
  postedDate?: string;
}

export type PaymentMethod = 'BKASH' | 'NAGAD' | 'ROCKET' | 'SSLCOMMERZ' | 'BANK_TRANSFER' | 'CASH' | 'WALLET' | 'CHEQUE';
export const PAYMENT_METHODS: PaymentMethod[] = ['BKASH', 'NAGAD', 'ROCKET', 'SSLCOMMERZ', 'BANK_TRANSFER', 'CASH', 'WALLET', 'CHEQUE'];

export type PaymentReceiptStatus = 'PENDING' | 'CONFIRMED' | 'DEPOSITED' | 'FAILED' | 'REVERSED';

export interface PaymentReceipt {
  id: number;
  receiptNumber: string;
  invoiceId?: number;
  invoiceNumber?: string;
  clientId: number;
  clientName?: string;
  amount: number;
  paymentDate: string;
  paymentMethod: PaymentMethod;
  transactionReference?: string;
  status: PaymentReceiptStatus;
  depositedToBank?: string;
  notes?: string;
  createdAt: string;
  companyId?: number;
  updatedAt?: string;
}

export interface PaymentReceiptRequest {
  clientId: number;
  invoiceId?: number;
  amount: number;
  paymentDate: string;
  paymentMethod: PaymentMethod;
  transactionReference?: string;
  notes?: string;
}

export interface GeneralLedgerEntry {
  id: number;
  transactionDate: string;
  accountId: number;
  accountName: string;
  accountCode: string;
  accountType?: string;
  debitAmount: number;
  creditAmount: number;
  description?: string;
  referenceType?: string;
  referenceId?: number;
  referenceNumber?: string;
  isReconciled: boolean;
  reconciliationNotes?: string;
  postedBy?: string;
  postedDate?: string;
  posted: boolean;
}

export interface BankReconciliation {
  id: number;
  bankAccountId: number;
  bankAccountName?: string;
  reconciliationDate: string;
  glBalance: number;
  bankStatementBalance: number;
  difference: number;
  outstandingDeposits?: string;
  outstandingChecks?: string;
  reconciled: boolean;
  reconciledDate?: string;
  reconciledBy?: string;
  discrepancyNotes?: string;
  companyId?: number;
}

export interface BankReconciliationRequest {
  bankAccountId: number;
  bankStatementBalance: number;
  outstandingDeposits?: string;
  outstandingChecks?: string;
}

export type WalletTransactionType = 'CREDIT' | 'DEBIT' | 'CREDIT_APPLIED' | 'REFUND_CREDIT' | 'REFERRAL_REWARD';
export const WALLET_TRANSACTION_TYPES: WalletTransactionType[] = ['CREDIT', 'DEBIT', 'CREDIT_APPLIED', 'REFUND_CREDIT', 'REFERRAL_REWARD'];

export interface Wallet {
  id: number;
  balance: number;
  creditBalance: number;
  totalAvailable: number;
  currency: string;
}

export interface WalletTransaction {
  id: number;
  type: WalletTransactionType;
  amount: number;
  balanceAfter: number;
  reference?: string;
  notes?: string;
  transactedAt: string;
}

export interface ProfitLossReport {
  periodStart: string;
  periodEnd: string;
  totalRevenue: number;
  totalExpense: number;
  netProfit: number;
  generatedDate: string;
}

export interface BalanceSheetReport {
  asOfDate: string;
  totalAssets: number;
  totalLiabilities: number;
  totalEquity: number;
  generatedDate: string;
}

export interface TrialBalanceAccountBalance {
  accountId: number;
  accountCode: string;
  accountName: string;
  debitBalance: number;
  creditBalance: number;
}

export interface TrialBalanceReport {
  asOfDate: string;
  accounts: TrialBalanceAccountBalance[];
  totalDebit: number;
  totalCredit: number;
  generatedDate: string;
}

export interface AgeingLine {
  invoiceId: number;
  invoiceNumber: string;
  clientId?: number;
  clientName?: string;
  dueDate: string;
  balanceAmount: number;
  daysOverdue: number;
  bucket: string;
}

export interface AgeingReport {
  asOfDate: string;
  current: number;
  days1to30: number;
  days31to60: number;
  days61to90: number;
  over90: number;
  totalOutstanding: number;
  lines: AgeingLine[];
}

export interface CashFlowLine {
  category: string;
  inflow: number;
  outflow: number;
}

export interface CashFlowReport {
  periodStart: string;
  periodEnd: string;
  openingBalance: number;
  closingBalance: number;
  totalInflows: number;
  totalOutflows: number;
  netChange: number;
  lines: CashFlowLine[];
}
