import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  CreateQuoteRequest,
  Quote,
  QuotePage,
  QuoteStatus,
  UpdateQuoteRequest,
  UpdateQuoteStatusRequest,
} from './quote.models';

@Injectable({
  providedIn: 'root',
})
export class QuoteService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/quotes`;

  search(
    search?: string,
    status?: QuoteStatus,
    page = 0,
    size = 20,
  ): Observable<QuotePage> {
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

    return this.http.get<QuotePage>(this.apiUrl, { params });
  }

  /** Presupuestos del vehículo que se pueden vincular a un turno nuevo. */
  findAvailableForAppointment(vehicleId: number): Observable<QuotePage> {
    const params = new HttpParams()
      .set('vehicleId', vehicleId)
      .set('available', true)
      .set('page', 0)
      .set('size', 50);

    return this.http.get<QuotePage>(this.apiUrl, { params });
  }

  findById(id: number): Observable<Quote> {
    return this.http.get<Quote>(`${this.apiUrl}/${id}`);
  }

  create(request: CreateQuoteRequest): Observable<Quote> {
    return this.http.post<Quote>(this.apiUrl, request);
  }

  update(id: number, request: UpdateQuoteRequest): Observable<Quote> {
    return this.http.put<Quote>(`${this.apiUrl}/${id}`, request);
  }

  updateStatus(
    id: number,
    request: UpdateQuoteStatusRequest,
  ): Observable<Quote> {
    return this.http.patch<Quote>(`${this.apiUrl}/${id}/status`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
