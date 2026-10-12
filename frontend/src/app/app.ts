import { Component, computed, OnInit, signal } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';

import { LeaveRequest } from './models/leave-request.model';
import { LeaveRequestService } from './services/leave-request.service';

const dateRangeValidator: ValidatorFn = (
  control: AbstractControl
): ValidationErrors | null => {
  const fromDate = control.get('fromDate')?.value as string;
  const toDate = control.get('toDate')?.value as string;

  if (fromDate && toDate && toDate < fromDate) {
    return { invalidDateRange: true };
  }

  return null;
};

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {

  // Data loaded from the backend
  readonly requests = signal<LeaveRequest[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal('');

  // Form and operation state
  readonly showForm = signal(false);
  readonly isSubmitting = signal(false);
  readonly editingId = signal<number | null>(null);
  readonly deletingId = signal<number | null>(null);

  readonly formError = signal('');
  readonly actionError = signal('');
  readonly successMessage = signal('');

  // Search
  readonly searchTerm = signal('');

  readonly filteredRequests = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();

    if (!term) {
      return this.requests();
    }

    return this.requests().filter(request =>
      request.employeeName.toLowerCase().includes(term) ||
      request.leaveType.toLowerCase().includes(term) ||
      request.reason.toLowerCase().includes(term) ||
      (request.status ?? '').toLowerCase().includes(term)
    );
  });

  // Dashboard metrics
  readonly totalRequests = computed(
    () => this.requests().length
  );

  readonly pendingCount = computed(
    () => this.requests().filter(
      request => request.status === 'PENDING'
    ).length
  );

  readonly approvedCount = computed(
    () => this.requests().filter(
      request => request.status === 'APPROVED'
    ).length
  );

  readonly rejectedCount = computed(
    () => this.requests().filter(
      request => request.status === 'REJECTED'
    ).length
  );

  // Reactive form
  readonly leaveForm = new FormGroup({
    employeeName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required]
    }),

    leaveType: new FormControl('CASUAL', {
      nonNullable: true,
      validators: [Validators.required]
    }),

    fromDate: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required]
    }),

    toDate: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required]
    }),

    reason: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.maxLength(500)
      ]
    })
  }, {
    validators: dateRangeValidator
  });

  constructor(
    private readonly leaveRequestService: LeaveRequestService
  ) {}

  ngOnInit(): void {
    this.loadRequests();
  }

  // Load requests from Spring Boot
  loadRequests(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.leaveRequestService.getAll().subscribe({
      next: data => {
        this.requests.set(data);
        this.isLoading.set(false);
      },

      error: error => {
        console.error('Failed to load leave requests:', error);

        this.errorMessage.set(
          'Unable to load leave requests. Check that the backend is running.'
        );

        this.isLoading.set(false);
      }
    });
  }

  // Search employees and request details
  onSearch(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchTerm.set(input.value);
  }

  // Open form for a new request
  openCreateForm(): void {
    this.editingId.set(null);

    this.leaveForm.reset({
      employeeName: '',
      leaveType: 'CASUAL',
      fromDate: '',
      toDate: '',
      reason: ''
    });

    this.formError.set('');
    this.actionError.set('');
    this.successMessage.set('');
    this.showForm.set(true);
  }

  // Open form with existing request data
  openEditForm(request: LeaveRequest): void {
    if (request.id == null) {
      return;
    }

    this.editingId.set(request.id);

    this.leaveForm.reset({
      employeeName: request.employeeName,
      leaveType: request.leaveType,
      fromDate: request.fromDate,
      toDate: request.toDate,
      reason: request.reason
    });

    this.formError.set('');
    this.actionError.set('');
    this.successMessage.set('');
    this.showForm.set(true);

    setTimeout(() => {
      document.getElementById('new-request')?.scrollIntoView({
        behavior: 'smooth',
        block: 'start'
      });
    });
  }

  closeCreateForm(): void {
    this.showForm.set(false);
    this.formError.set('');
  }

  // Create or update a request
  submitLeaveRequest(): void {
    this.formError.set('');

    if (this.leaveForm.invalid) {
      this.leaveForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);

    const request: LeaveRequest = this.leaveForm.getRawValue();
    const id = this.editingId();

    const operation = id === null
      ? this.leaveRequestService.create(request)
      : this.leaveRequestService.update(id, request);

    operation.subscribe({
      next: savedRequest => {
        this.isSubmitting.set(false);
        this.showForm.set(false);

        this.successMessage.set(
          id === null
            ? `Leave request #${savedRequest.id} created successfully.`
            : `Leave request #${id} updated successfully.`
        );

        this.editingId.set(null);
        this.loadRequests();
      },

      error: error => {
        console.error('Failed to save leave request:', error);

        const body = error.error as {
          message?: string;
          messages?: Record<string, string>;
        } | null;

        const validationMessages = body?.messages
          ? Object.values(body.messages).join(' ')
          : '';

        this.formError.set(
          body?.message ||
          validationMessages ||
          'Unable to save the leave request. Please try again.'
        );

        this.isSubmitting.set(false);
      }
    });
  }

  // Delete a request after user confirmation
  deleteRequest(request: LeaveRequest): void {
    if (request.id == null || this.deletingId() !== null) {
      return;
    }

    const confirmed = window.confirm(
      `Delete the leave request for ${request.employeeName}?`
    );

    if (!confirmed) {
      return;
    }

    this.actionError.set('');
    this.successMessage.set('');
    this.deletingId.set(request.id);

    this.leaveRequestService.delete(request.id).subscribe({
      next: () => {
        this.deletingId.set(null);

        this.successMessage.set(
          `Leave request #${request.id} deleted successfully.`
        );

        this.loadRequests();
      },

      error: error => {
        console.error('Failed to delete leave request:', error);

        this.actionError.set(
          'Unable to delete the leave request. Please try again.'
        );

        this.deletingId.set(null);
      }
    });
  }

  // Inclusive calendar-day count
  getLeaveDays(fromDate: string, toDate: string): number {
    const start = new Date(`${fromDate}T00:00:00`);
    const end = new Date(`${toDate}T00:00:00`);

    const difference = end.getTime() - start.getTime();

    return Math.floor(
      difference / (1000 * 60 * 60 * 24)
    ) + 1;
  }

  getInitials(name: string): string {
    return name
      .trim()
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map(part => part.charAt(0).toUpperCase())
      .join('');
  }
}
