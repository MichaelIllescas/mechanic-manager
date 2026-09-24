import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { WorkOrder } from '../work-orders/work-order.models';

@Injectable({
  providedIn: 'root',
})
export class HistoryService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/work-orders/history`;

  findByCustomer(customerId: number): Observable<WorkOrder[]> {
    return this.http.get<WorkOrder[]>(this.apiUrl, {
      params: new HttpParams().set('customerId', customerId),
    });
  }

  findByVehicle(vehicleId: number): Observable<WorkOrder[]> {
    return this.http.get<WorkOrder[]>(this.apiUrl, {
      params: new HttpParams().set('vehicleId', vehicleId),
    });
  }
}
