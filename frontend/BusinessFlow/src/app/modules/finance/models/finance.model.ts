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
}

export interface Invoice {
  id: number;
  invoiceNumber: string;
  status: string;
  type: string;
  subtotal: number;
  taxRate: number;
  taxAmount: number;
  discountAmount: number;
  totalAmount: number;
  paidAmount: number;
  outstandingAmount: number;
  dueDate?: string;
  issuedAt?: string;
  paidAt?: string;
  notes?: string;
  clientId: number;
  clientName?: string;
  serviceRequestId?: number;
  serviceRequestTitle?: string;
  createdByName?: string;
  payments?: Payment[];
  createdAt: string;
}

export interface Payment {
  id: number;
  amount: number;
  method: string;
  status: string;
  reference?: string;
  paidAt: string;
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
  reconciledBy?: string;
  discrepancyNotes?: string;
}

export interface BankReconciliationRequest {
  bankAccountId: number;
  bankStatementBalance: number;
  outstandingDeposits?: number;
  outstandingChecks?: number;
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

export interface WalletTopUpRequest {
  amount: number;
  reference?: string;
  notes?: string;
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
