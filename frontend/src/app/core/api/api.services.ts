import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CreateOrderInput,
  Customer,
  CustomerInput,
  DashboardSummary,
  Order,
  OrderStatus,
  OrderSummary,
  Page,
  Product,
  ProductInput,
} from '../models';

export interface PageQuery {
  page?: number;
  size?: number;
  search?: string;
  sort?: string;
}

function toParams(query: object): HttpParams {
  let params = new HttpParams();
  for (const [key, value] of Object.entries(query)) {
    if (value !== undefined && value !== null && value !== '') {
      params = params.set(key, String(value));
    }
  }
  return params;
}

@Injectable({ providedIn: 'root' })
export class ProductsApi {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/products';

  list(query: PageQuery = {}): Observable<Page<Product>> {
    return this.http.get<Page<Product>>(this.url, { params: toParams(query) });
  }

  create(input: ProductInput): Observable<Product> {
    return this.http.post<Product>(this.url, input);
  }

  update(id: number, input: ProductInput): Observable<Product> {
    return this.http.put<Product>(`${this.url}/${id}`, input);
  }

  archive(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}

@Injectable({ providedIn: 'root' })
export class CustomersApi {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/customers';

  list(query: PageQuery = {}): Observable<Page<Customer>> {
    return this.http.get<Page<Customer>>(this.url, { params: toParams(query) });
  }

  create(input: CustomerInput): Observable<Customer> {
    return this.http.post<Customer>(this.url, input);
  }

  update(id: number, input: CustomerInput): Observable<Customer> {
    return this.http.put<Customer>(`${this.url}/${id}`, input);
  }
}

@Injectable({ providedIn: 'root' })
export class OrdersApi {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/orders';

  list(query: PageQuery & { status?: OrderStatus } = {}): Observable<Page<OrderSummary>> {
    return this.http.get<Page<OrderSummary>>(this.url, { params: toParams(query) });
  }

  get(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.url}/${id}`);
  }

  create(input: CreateOrderInput): Observable<Order> {
    return this.http.post<Order>(this.url, input);
  }

  changeStatus(id: number, status: OrderStatus): Observable<Order> {
    return this.http.patch<Order>(`${this.url}/${id}/status`, { status });
  }
}

@Injectable({ providedIn: 'root' })
export class DashboardApi {
  private readonly http = inject(HttpClient);

  summary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>('/api/dashboard/summary');
  }
}
