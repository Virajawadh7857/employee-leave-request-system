package com.company.leaverequest.controller;

import com.company.leaverequest.entity.LeaveRequest;
import com.company.leaverequest.service.LeaveRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    // Create a new leave request
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveRequest createLeaveRequest(
            @Valid @RequestBody LeaveRequest leaveRequest) {

        return leaveRequestService.createLeaveRequest(leaveRequest);
    }

    // Get all leave requests
    @GetMapping
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestService.getAllLeaveRequests();
    }

    // Get a leave request by ID
    @GetMapping("/{id}")
    public LeaveRequest getLeaveRequestById(@PathVariable Long id) {
        return leaveRequestService.getLeaveRequestById(id);
    }

    @PutMapping("/{id}")
    public LeaveRequest updateLeaveRequest(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequest leaveRequest) {

        return leaveRequestService.updateLeaveRequest(id, leaveRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLeaveRequest(@PathVariable Long id) {
        leaveRequestService.deleteLeaveRequest(id);
    }

}