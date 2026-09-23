import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  CreateWorkOrderRequest,
  UpdateWorkOrderRequest,
  UpdateWorkOrderStatusRequest,
  WorkOrder,
  WorkOrderPage,
  WorkOrderStatus,
} from './work-order.models';

@Injectable({
  providedIn: 'root',
})
export class WorkOrderService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/work-orders`;

  search(
    search?: string,
    status?: WorkOrderStatus,
    vehicleId?: number,
    from?: string,
    to?: string,
    page = 0,
    size = 20,
  ): Observable<WorkOrderPage> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    const normalizedSearch = search?.trim();

    if (normalizedSearch) {
      params = params.set('search', normalizedSearch);
    }

    if (status) {
      params = params.set('status', status);
    }

    if (vehicleId) {
      params = params.set('vehicleId', vehicleId);
    }

    if (from) {
      params = params.set('from', from);
    }

    if (to) {
      params = params.set('to', to);
    }

    return this.http.get<WorkOrderPage>(this.apiUrl, { params });
  }

  findById(id: number): Observable<WorkOrder> {
    return this.http.get<WorkOrder>(`${this.apiUrl}/${id}`);
  }

  create(request: CreateWorkOrderRequest): Observable<WorkOrder> {
    return this.http.post<WorkOrder>(this.apiUrl, request);
  }

  update(id: number, request: UpdateWorkOrderRequest): Observable<WorkOrder> {
    return this.http.put<WorkOrder>(`${this.apiUrl}/${id}`, request);
  }

  updateStatus(
    id: number,
    request: UpdateWorkOrderStatusRequest,
  ): Observable<WorkOrder> {
    return this.http.patch<WorkOrder>(`${this.apiUrl}/${id}/status`, request);
  }
}
