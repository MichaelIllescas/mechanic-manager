import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { UpdateWorkshopRequest, Workshop } from './workshop.models';

@Injectable({
  providedIn: 'root',
})
export class WorkshopService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/workshops/me`;

  findMine(): Observable<Workshop> {
    return this.http.get<Workshop>(this.apiUrl);
  }

  update(request: UpdateWorkshopRequest): Observable<Workshop> {
    return this.http.put<Workshop>(this.apiUrl, request);
  }
}
