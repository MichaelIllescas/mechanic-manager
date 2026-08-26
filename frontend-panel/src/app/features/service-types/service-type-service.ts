import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  CreateServiceTypeRequest,
  ServiceType,
  UpdateServiceTypeRequest,
  UpdateServiceTypeStatusRequest,
} from './service-type.models';

@Injectable({
  providedIn: 'root',
})
export class ServiceTypeService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/service-types`;

  /**
   * Sin `active` devuelve el catálogo completo, que es lo que muestra la pantalla
   * de administración. Con `active: true` devuelve solo los que se pueden cargar
   * en presupuestos y órdenes de trabajo.
   */
  findAll(active?: boolean): Observable<ServiceType[]> {
    const params =
      active === undefined ? undefined : new HttpParams().set('active', active);

    return this.http.get<ServiceType[]>(this.apiUrl, { params });
  }

  findById(id: number): Observable<ServiceType> {
    return this.http.get<ServiceType>(`${this.apiUrl}/${id}`);
  }

  create(request: CreateServiceTypeRequest): Observable<ServiceType> {
    return this.http.post<ServiceType>(this.apiUrl, request);
  }

  update(id: number, request: UpdateServiceTypeRequest): Observable<ServiceType> {
    return this.http.put<ServiceType>(`${this.apiUrl}/${id}`, request);
  }

  updateStatus(id: number, request: UpdateServiceTypeStatusRequest): Observable<ServiceType> {
    return this.http.patch<ServiceType>(`${this.apiUrl}/${id}/status`, request);
  }
}
