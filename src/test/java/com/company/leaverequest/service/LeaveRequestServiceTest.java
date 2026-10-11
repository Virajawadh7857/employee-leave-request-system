package com.company.leaverequest.service;

import com.company.leaverequest.entity.LeaveRequest;
import com.company.leaverequest.repository.LeaveRequestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class LeaveRequestServiceTest {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Test
    void shouldCreateValidLeaveRequest() {

        LeaveRequest request = new LeaveRequest();
        request.setEmployeeName("Test Employee");
        request.setLeaveType("CASUAL");
        request.setFromDate(LocalDate.of(2026, 10, 20));
        request.setToDate(LocalDate.of(2026, 10, 22));
        request.setReason("Personal work");

        LeaveRequest savedRequest =
                leaveRequestService.createLeaveRequest(request);

        assertNotNull(savedRequest.getId());
        assertEquals("Test Employee", savedRequest.getEmployeeName());
        assertEquals("PENDING", savedRequest.getStatus());
    }

    @Test
    void shouldRejectInvalidDateRange() {

        long initialCount = leaveRequestRepository.count();

        LeaveRequest request = new LeaveRequest();
        request.setEmployeeName("Test Employee");
        request.setLeaveType("CASUAL");
        request.setFromDate(LocalDate.of(2026, 10, 23));
        request.setToDate(LocalDate.of(2026, 10, 20));
        request.setReason("Testing invalid dates");

        assertThrows(
                IllegalArgumentException.class,
                () -> leaveRequestService.createLeaveRequest(request)
        );

        assertEquals(initialCount, leaveRequestRepository.count());
    }
}