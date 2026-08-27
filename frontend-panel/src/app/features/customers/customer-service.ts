import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  CreateCustomerRequest,
  Customer,
  UpdateCustomerRequest,
} from './customer.models';

@Injectable({
  providedIn: 'root',
})
export class CustomerService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/customers`;

  findAll(search?: string): Observable<Customer[]> {
    const normalizedSearch = search?.trim();
    const params = normalizedSearch
      ? new HttpParams().set('search', normalizedSearch)
      : undefined;

    return this.http.get<Customer[]>(this.apiUrl, { params });
  }

  findFromListById(id: number): Observable<Customer | undefined> {
    return this.findAll().pipe(
      map((customers) => customers.find((customer) => customer.id === id)),
    );
  }

  create(request: CreateCustomerRequest): Observable<Customer> {
    return this.http.post<Customer>(this.apiUrl, request);
  }

  update(id: number, request: UpdateCustomerRequest): Observable<Customer> {
    return this.http.put<Customer>(`${this.apiUrl}/${id}`, request);
  }
}
