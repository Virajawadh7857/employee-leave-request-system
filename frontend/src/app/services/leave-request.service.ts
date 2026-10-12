import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LeaveRequest } from '../models/leave-request.model';

@Injectable({
  providedIn: 'root'
})
export class LeaveRequestService {

  private readonly apiUrl =
    'http://localhost:8080/api/leave-requests';

  constructor(private http: HttpClient) {}

  // Retrieve all leave requests
  getAll(): Observable<LeaveRequest[]> {
    return this.http.get<LeaveRequest[]>(this.apiUrl);
  }

  // Retrieve one leave request
  getById(id: number): Observable<LeaveRequest> {
    return this.http.get<LeaveRequest>(`${this.apiUrl}/${id}`);
  }

  // Create a leave request
  create(request: LeaveRequest): Observable<LeaveRequest> {
    return this.http.post<LeaveRequest>(this.apiUrl, request);
  }

  // Update a leave request
  update(
    id: number,
    request: LeaveRequest
  ): Observable<LeaveRequest> {
    return this.http.put<LeaveRequest>(
      `${this.apiUrl}/${id}`,
      request
    );
  }

  // Delete a leave request
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
