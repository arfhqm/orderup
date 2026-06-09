import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CartItem, MenuItem, Order, Recommendation } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  getMenu(): Observable<MenuItem[]> {
    return this.http.get<MenuItem[]>('/api/menu');
  }

  getOrders(): Observable<Order[]> {
    return this.http.get<Order[]>('/api/orders');
  }

  saveOrder(cart: CartItem[], tableNo: string, orderType: string, paymentMethod: string, orderId?: number): Observable<Order> {
    const body = {
      tableNo,
      orderType,
      paymentMethod,
      items: cart.map((item) => ({
        menuItemId: item.menuItem.id,
        quantity: item.quantity,
        notes: item.notes
      }))
    };

    return orderId
      ? this.http.put<Order>(`/api/orders/${orderId}`, body)
      : this.http.post<Order>('/api/orders', body);
  }

  updateStatus(orderId: number, status: string): Observable<Order> {
    return this.http.put<Order>(`/api/orders/${orderId}/status`, { status });
  }

  payOrder(orderId: number, paymentMethod: string, splits: { payerName: string; amount: number }[]): Observable<Order> {
    return this.http.post<Order>(`/api/orders/${orderId}/payments`, { paymentMethod, splits });
  }

  recommend(cartItemNames: string[]): Observable<Recommendation> {
    return this.http.post<Recommendation>('/api/ai/recommend', { cartItemNames });
  }
}
