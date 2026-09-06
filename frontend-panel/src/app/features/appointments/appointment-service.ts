import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  Appointment,
  CreateAppointmentRequest,
  UpdateAppointmentRequest,
} from './appointment.models';

@Injectable({
  providedIn: 'root',
})
export class AppointmentService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/appointments`;

  /**
   * Las tres vistas del calendario usan este método: la diaria pide un rango
   * de un día, la semanal de siete y la mensual la grilla completa.
   *
   * Las fechas son inclusivas y van en formato ISO (yyyy-MM-dd).
   */
  findByRange(from: string, to: string): Observable<Appointment[]> {
    const params = new HttpParams().set('from', from).set('to', to);

    return this.http.get<Appointment[]>(this.apiUrl, { params });
  }

  findById(id: number): Observable<Appointment> {
    return this.http.get<Appointment>(`${this.apiUrl}/${id}`);
  }

  create(request: CreateAppointmentRequest): Observable<Appointment> {
    return this.http.post<Appointment>(this.apiUrl, request);
  }

  update(id: number, request: UpdateAppointmentRequest): Observable<Appointment> {
    return this.http.put<Appointment>(`${this.apiUrl}/${id}`, request);
  }

  complete(id: number): Observable<Appointment> {
    return this.http.post<Appointment>(`${this.apiUrl}/${id}/complete`, {});
  }

  cancel(id: number): Observable<Appointment> {
    return this.http.post<Appointment>(`${this.apiUrl}/${id}/cancel`, {});
  }
}
