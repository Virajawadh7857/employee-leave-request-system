export interface LeaveRequest {
  id?: number;
  employeeName: string;
  leaveType: string;
  fromDate: string;
  toDate: string;
  reason: string;
  status?: string;
  createdAt?: string;
}
