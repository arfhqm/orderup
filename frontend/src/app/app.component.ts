import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from './api.service';
import { CartItem, MenuItem, Order, Recommendation } from './models';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {
  menu: MenuItem[] = [];
  orders: Order[] = [];
  cart: CartItem[] = [];
  tableNo = 'A1';
  orderType = 'DINE_IN';
  paymentMethod = 'CASH';
  editingOrderId?: number;
  recommendation?: Recommendation;
  message = '';

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.loadData();
  }

  get categories(): string[] {
    return [...new Set(this.menu.map((item) => item.category))];
  }

  get cartTotal(): number {
    return this.cart.reduce((total, item) => total + item.menuItem.price * item.quantity, 0);
  }

  loadData(): void {
    this.api.getMenu().subscribe((menu) => this.menu = menu);
    this.refreshOrders();
  }

  refreshOrders(): void {
    this.api.getOrders().subscribe((orders) => this.orders = orders);
  }

  menuByCategory(category: string): MenuItem[] {
    return this.menu.filter((item) => item.category === category);
  }

  addToCart(menuItem: MenuItem): void {
    const existing = this.cart.find((item) => item.menuItem.id === menuItem.id);
    if (existing) {
      existing.quantity += 1;
    } else {
      this.cart.push({ menuItem, quantity: 1, notes: '' });
    }
    this.getRecommendation();
  }

  removeFromCart(index: number): void {
    this.cart.splice(index, 1);
    this.getRecommendation();
  }

  saveOrder(): void {
    if (!this.cart.length) {
      this.message = 'Add at least one item first.';
      return;
    }

    this.api.saveOrder(this.cart, this.tableNo, this.orderType, this.paymentMethod, this.editingOrderId)
      .subscribe({
        next: () => {
          this.message = this.editingOrderId ? 'Order updated.' : 'Order sent to kitchen.';
          this.clearCart();
          this.refreshOrders();
        },
        error: (error) => this.message = error.error?.message || 'Could not save order.'
      });
  }

  editOrder(order: Order): void {
    this.editingOrderId = order.id;
    this.tableNo = order.tableNo;
    this.orderType = order.orderType;
    this.paymentMethod = order.paymentMethod || 'CASH';
    this.cart = order.orderItems.map((item) => ({
      menuItem: item.menuItem,
      quantity: item.quantity,
      notes: item.notes || ''
    }));
    this.getRecommendation();
  }

  clearCart(): void {
    this.cart = [];
    this.editingOrderId = undefined;
    this.recommendation = undefined;
  }

  nextStatus(order: Order): void {
    const flow: Record<string, string> = {
      PENDING: 'PREPARING',
      PREPARING: 'READY',
      READY: 'COMPLETED'
    };
    const next = flow[order.status];
    if (!next) {
      return;
    }
    this.api.updateStatus(order.id, next).subscribe(() => this.refreshOrders());
  }

  pay(order: Order, splitCount: number): void {
    const remaining = Number((order.totalAmount - order.paidAmount).toFixed(2));
    const amount = Number((remaining / splitCount).toFixed(2));
    const splits = Array.from({ length: splitCount }, (_, index) => ({
      payerName: splitCount === 1 ? 'Guest' : `Guest ${index + 1}`,
      amount: index === splitCount - 1
        ? Number((remaining - amount * (splitCount - 1)).toFixed(2))
        : amount
    }));

    this.api.payOrder(order.id, this.paymentMethod, splits).subscribe({
      next: () => {
        this.message = splitCount === 1 ? 'Payment captured.' : `Bill split ${splitCount} ways.`;
        this.refreshOrders();
      },
      error: (error) => this.message = error.error?.message || 'Could not capture payment.'
    });
  }

  getRecommendation(): void {
    if (!this.cart.length) {
      this.recommendation = undefined;
      return;
    }

    this.api.recommend(this.cart.map((item) => item.menuItem.name))
      .subscribe((recommendation) => this.recommendation = recommendation);
  }
}
