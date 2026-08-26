import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { environment } from '../../../environments/environment';
import {
  CreateSupplyRequest,
  Supply,
  UpdateSupplyRequest,
  UpdateSupplyStatusRequest,
} from './supply.models';

type SupplyApiResponse = Partial<Supply> & {
  nombre?: string;
  precio?: number | string;
};

type SupplyListApiResponse = SupplyApiResponse[] | { data?: SupplyApiResponse[]; content?: SupplyApiResponse[] };

@Injectable({
  providedIn: 'root',
})
export class SupplyService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/supplies`;

  findAll(active?: boolean): Observable<Supply[]> {
    const params =
      active === undefined ? undefined : new HttpParams().set('active', active);

    return this.http
      .get<SupplyListApiResponse>(this.apiUrl, { params })
      .pipe(map((response) => this.toSupplyList(response)));
  }

  findById(id: number): Observable<Supply> {
    return this.http
      .get<SupplyApiResponse>(`${this.apiUrl}/${id}`)
      .pipe(map((response) => this.toSupply(response)));
  }

  create(request: CreateSupplyRequest): Observable<Supply> {
    return this.http.post<Supply>(this.apiUrl, request);
  }

  update(id: number, request: UpdateSupplyRequest): Observable<Supply> {
    return this.http.put<Supply>(`${this.apiUrl}/${id}`, request);
  }

  updateStatus(id: number, request: UpdateSupplyStatusRequest): Observable<Supply> {
    return this.http.patch<Supply>(`${this.apiUrl}/${id}/status`, request);
  }

  private toSupplyList(response: SupplyListApiResponse): Supply[] {
    const items = Array.isArray(response) ? response : (response.content ?? response.data ?? []);

    return items.map((item) => this.toSupply(item));
  }

  private toSupply(response: SupplyApiResponse): Supply {
    const price = response.price ?? response.precio ?? 0;

    return {
      id: Number(response.id),
      name: response.name ?? response.nombre ?? '',
      price: Number(price),
      active: response.active ?? true,
      createdAt: response.createdAt ?? '',
      updatedAt: response.updatedAt ?? '',
    };
  }
}
