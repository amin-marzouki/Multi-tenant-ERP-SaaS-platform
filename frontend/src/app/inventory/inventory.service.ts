import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import {
  Item,
  ItemRequest,
  StockLevel,
  StockMove,
  StockMoveRequest,
  Warehouse,
  WarehouseRequest,
} from '../core/models';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/inventory';

  items(): Observable<Item[]> {
    return this.http.get<Item[]>(`${this.base}/items`);
  }

  createItem(request: ItemRequest): Observable<Item> {
    return this.http.post<Item>(`${this.base}/items`, request);
  }

  updateItem(id: number, request: ItemRequest): Observable<Item> {
    return this.http.put<Item>(`${this.base}/items/${id}`, request);
  }

  warehouses(): Observable<Warehouse[]> {
    return this.http.get<Warehouse[]>(`${this.base}/warehouses`);
  }

  createWarehouse(request: WarehouseRequest): Observable<Warehouse> {
    return this.http.post<Warehouse>(`${this.base}/warehouses`, request);
  }

  stockLevels(): Observable<StockLevel[]> {
    return this.http.get<StockLevel[]>(`${this.base}/stock`);
  }

  stockMoves(): Observable<StockMove[]> {
    return this.http.get<StockMove[]>(`${this.base}/stock-moves`);
  }

  recordMove(request: StockMoveRequest): Observable<StockMove> {
    return this.http.post<StockMove>(`${this.base}/stock-moves`, request);
  }
}
