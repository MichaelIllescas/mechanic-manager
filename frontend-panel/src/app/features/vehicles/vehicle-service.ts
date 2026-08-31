import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  CreateVehicleRequest,
  UpdateVehicleRequest,
  Vehicle,
} from './vehicle.models';

@Injectable({
  providedIn: 'root',
})
export class VehicleService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/vehicles`;

  findAll(search?: string): Observable<Vehicle[]> {
    const normalizedSearch = search?.trim();
    const params = normalizedSearch
      ? new HttpParams().set('search', normalizedSearch)
      : undefined;

    return this.http.get<Vehicle[]>(this.apiUrl, { params });
  }

  findFromListById(id: number): Observable<Vehicle | undefined> {
    return this.findAll().pipe(
      map((vehicles) => vehicles.find((vehicle) => vehicle.id === id)),
    );
  }

  create(request: CreateVehicleRequest): Observable<Vehicle> {
    return this.http.post<Vehicle>(this.apiUrl, request);
  }

  update(id: number, request: UpdateVehicleRequest): Observable<Vehicle> {
    return this.http.put<Vehicle>(`${this.apiUrl}/${id}`, request);
  }
}
