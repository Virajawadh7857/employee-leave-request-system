package com.company.leaverequest.service;

import com.company.leaverequest.entity.LeaveRequest;
import com.company.leaverequest.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import com.company.leaverequest.exception.LeaveRequestNotFoundException;

@Service
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;

    public LeaveRequestService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public LeaveRequest createLeaveRequest(LeaveRequest leaveRequest) {

        if (leaveRequest.getFromDate() == null ||
                leaveRequest.getToDate() == null) {
            throw new IllegalArgumentException(
                    "From date and to date are required"
            );
        }

        if (leaveRequest.getToDate().isBefore(leaveRequest.getFromDate())) {
            throw new IllegalArgumentException(
                    "To date cannot be earlier than from date"
            );
        }

        return leaveRequestRepository.save(leaveRequest);
    }

    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    public LeaveRequest getLeaveRequestById(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() ->
                        new LeaveRequestNotFoundException(
                                "Leave request not found with id: " + id
                        ));
    }

    public LeaveRequest updateLeaveRequest(Long id, LeaveRequest updatedRequest) {

        LeaveRequest existingRequest = getLeaveRequestById(id);

        if (updatedRequest.getFromDate() == null ||
                updatedRequest.getToDate() == null) {
            throw new IllegalArgumentException(
                    "From date and to date are required"
            );
        }

        if (updatedRequest.getToDate().isBefore(updatedRequest.getFromDate())) {
            throw new IllegalArgumentException(
                    "To date cannot be earlier than from date"
            );
        }

        existingRequest.setEmployeeName(updatedRequest.getEmployeeName());
        existingRequest.setLeaveType(updatedRequest.getLeaveType());
        existingRequest.setFromDate(updatedRequest.getFromDate());
        existingRequest.setToDate(updatedRequest.getToDate());
        existingRequest.setReason(updatedRequest.getReason());

        return leaveRequestRepository.save(existingRequest);
    }

    public void deleteLeaveRequest(Long id) {
        LeaveRequest existingRequest = getLeaveRequestById(id);
        leaveRequestRepository.delete(existingRequest);
    }

}