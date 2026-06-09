export interface MenuItem {
  id: number;
  name: string;
  category: string;
  price: number;
  available: boolean;
  description: string;
}

export interface CartItem {
  menuItem: MenuItem;
  quantity: number;
  notes: string;
}

export interface OrderItem {
  id: number;
  menuItem: MenuItem;
  quantity: number;
  notes: string;
  subtotal: number;
}

export interface Payment {
  id: number;
  payerName: string;
  paymentMethod: string;
  amount: number;
  paidAt: string;
}

export interface Order {
  id: number;
  tableNo: string;
  orderType: 'DINE_IN' | 'TAKEAWAY';
  status: 'PENDING' | 'PREPARING' | 'READY' | 'COMPLETED' | 'CANCELLED';
  paymentMethod: 'CASH' | 'CARD' | 'E_WALLET';
  paymentStatus: 'UNPAID' | 'PARTIALLY_PAID' | 'PAID';
  totalAmount: number;
  paidAmount: number;
  createdAt: string;
  orderItems: OrderItem[];
  payments: Payment[];
}

export interface Recommendation {
  suggestion: string;
  recommendedItem: string;
  source: string;
}
