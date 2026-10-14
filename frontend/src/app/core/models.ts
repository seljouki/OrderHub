export type Role = 'ADMIN' | 'STAFF';

export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';

export const ORDER_STATUSES: readonly OrderStatus[] = [
  'PENDING',
  'CONFIRMED',
  'SHIPPED',
  'DELIVERED',
  'CANCELLED',
];

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: Role;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresAt: string;
  user: User;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Product {
  id: number;
  sku: string;
  name: string;
  description: string | null;
  price: number;
  stockQuantity: number;
  reorderLevel: number;
  lowStock: boolean;
  updatedAt: string;
}

export interface ProductInput {
  sku?: string;
  name: string;
  description: string | null;
  price: number;
  stockQuantity: number;
  reorderLevel: number;
}

export interface Customer {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  city: string | null;
  createdAt: string;
}

export type CustomerInput = Omit<Customer, 'id' | 'createdAt'>;

export interface OrderSummary {
  id: number;
  orderNumber: string;
  customerId: number;
  customerName: string;
  status: OrderStatus;
  itemCount: number;
  totalAmount: number;
  createdAt: string;
}

export interface OrderLine {
  productId: number;
  sku: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface Order {
  id: number;
  orderNumber: string;
  status: OrderStatus;
  allowedTransitions: OrderStatus[];
  customer: { id: number; name: string; email: string };
  items: OrderLine[];
  totalAmount: number;
  notes: string | null;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateOrderInput {
  customerId: number;
  items: { productId: number; quantity: number }[];
  notes?: string | null;
}

export interface DashboardSummary {
  revenueLast30Days: number;
  ordersLast30Days: number;
  activeProducts: number;
  customers: number;
  ordersByStatus: Record<OrderStatus, number>;
  dailyRevenue: { date: string; revenue: number }[];
  topProducts: {
    productId: number;
    sku: string;
    name: string;
    unitsSold: number;
    revenue: number;
  }[];
  lowStock: Product[];
}

/** RFC 9457 problem detail, as returned by the API for every error. */
export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  errors?: Record<string, string>;
}
