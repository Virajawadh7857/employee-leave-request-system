package com.company.leaverequest.controller;

import com.company.leaverequest.exception.GlobalExceptionHandler;
import com.company.leaverequest.exception.LeaveRequestNotFoundException;
import com.company.leaverequest.service.LeaveRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class LeaveRequestControllerTest {

    private LeaveRequestService leaveRequestService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        leaveRequestService = mock(LeaveRequestService.class);

        mockMvc = standaloneSetup(
                new LeaveRequestController(leaveRequestService)
        )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateLeaveRequest() throws Exception {

        when(leaveRequestService.createLeaveRequest(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/leave-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeName": "Test Employee",
                                  "leaveType": "CASUAL",
                                  "fromDate": "2026-10-20",
                                  "toDate": "2026-10-22",
                                  "reason": "Personal work"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeName")
                        .value("Test Employee"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldRejectMissingRequiredFields() throws Exception {

        mockMvc.perform(post("/api/leave-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeName": "",
                                  "leaveType": "CASUAL",
                                  "fromDate": "2026-10-20",
                                  "toDate": "2026-10-22",
                                  "reason": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Validation Failed"));

        verifyNoInteractions(leaveRequestService);
    }

    @Test
    void shouldReturn404WhenRequestDoesNotExist() throws Exception {

        when(leaveRequestService.getLeaveRequestById(9999L))
                .thenThrow(new LeaveRequestNotFoundException(
                        "Leave request not found with id: 9999"
                ));

        mockMvc.perform(get("/api/leave-requests/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Leave request not found with id: 9999"));
    }
}