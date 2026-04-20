package com.smart.rh.service;

import com.smart.rh.dto.AttendanceRecordDto;
import com.smart.rh.entity.AttendanceRecord;
import com.smart.rh.entity.Employee;
import com.smart.rh.entity.RfidCard;
import com.smart.rh.entity.RfidReader;
import com.smart.rh.exception.CardNotFoundException;
import com.smart.rh.exception.EmployeeNotFoundException;
import com.smart.rh.exception.InactiveCardException;
import com.smart.rh.exception.DuplicateAttendanceException;
import com.smart.rh.repository.AttendanceRecordRepository;
import com.smart.rh.repository.EmployeeRepository;
import com.smart.rh.repository.RfidCardRepository;
import com.smart.rh.repository.RfidReaderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RfidAttendanceServiceTest {

    @Mock
    private RfidCardRepository rfidCardRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private RfidReaderRepository rfidReaderRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private RfidAttendanceService rfidAttendanceService;

    private Employee testEmployee;
    private RfidCard testCard;
    private RfidReader testReader;
    private AttendanceRecord testAttendanceRecord;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");

        testCard = new RfidCard();
        testCard.setId(1L);
        testCard.setCardId("CARD123");
        testCard.setEmployee(testEmployee);
        testCard.setIsActive(true);

        testReader = new RfidReader();
        testReader.setId(1L);
        testReader.setDeviceId("READER001");
        testReader.setName("Main Entrance");
        testReader.setIsActive(true);

        testAttendanceRecord = new AttendanceRecord();
        testAttendanceRecord.setId(1L);
        testAttendanceRecord.setEmployee(testEmployee);
        testAttendanceRecord.setEventType("IN");
        testAttendanceRecord.setTimestamp(Instant.now());
        testAttendanceRecord.setVerificationStatus("PENDING");
    }

    @Test
    void testFindEmployeeByRfidCard_Success() {
        when(rfidCardRepository.findByCardId("CARD123")).thenReturn(Optional.of(testCard));

        Employee result = rfidAttendanceService.findEmployeeByRfidCard("CARD123");

        assertNotNull(result);
        assertEquals(testEmployee.getId(), result.getId());
        verify(rfidCardRepository, times(1)).findByCardId("CARD123");
    }

    @Test
    void testFindEmployeeByRfidCard_CardNotFound() {
        when(rfidCardRepository.findByCardId("INVALID")).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> rfidAttendanceService.findEmployeeByRfidCard("INVALID"));
        verify(rfidCardRepository, times(1)).findByCardId("INVALID");
    }

    @Test
    void testFindEmployeeByRfidCard_InactiveCard() {
        testCard.setIsActive(false);
        when(rfidCardRepository.findByCardId("CARD123")).thenReturn(Optional.of(testCard));

        assertThrows(InactiveCardException.class, () -> rfidAttendanceService.findEmployeeByRfidCard("CARD123"));
    }

    @Test
    void testProcessRfidSwipe_Success() {
        when(rfidCardRepository.findByCardId("CARD123")).thenReturn(Optional.of(testCard));
        when(rfidReaderRepository.findByDeviceId("READER001")).thenReturn(Optional.of(testReader));
        when(attendanceRecordRepository.findLatestByEmployeeId(1L)).thenReturn(Optional.empty());
        when(attendanceRecordRepository.save(any(AttendanceRecord.class))).thenReturn(testAttendanceRecord);

        AttendanceRecordDto result = rfidAttendanceService.processRfidSwipe(
                "CARD123", "READER001", Instant.now());

        assertNotNull(result);
        assertEquals("IN", result.getEventType());
        verify(attendanceRecordRepository, times(1)).save(any(AttendanceRecord.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any());
    }

    @Test
    void testProcessRfidSwipe_DuplicateDetection() {
        AttendanceRecord recentRecord = new AttendanceRecord();
        recentRecord.setId(2L);
        recentRecord.setEmployee(testEmployee);
        recentRecord.setEventType("IN");
        recentRecord.setTimestamp(Instant.now().minus(10, ChronoUnit.SECONDS));

        when(rfidCardRepository.findByCardId("CARD123")).thenReturn(Optional.of(testCard));
        when(rfidReaderRepository.findByDeviceId("READER001")).thenReturn(Optional.of(testReader));
        when(attendanceRecordRepository.findLatestByEmployeeId(1L)).thenReturn(Optional.of(recentRecord));

        assertThrows(DuplicateAttendanceException.class,
                () -> rfidAttendanceService.processRfidSwipe("CARD123", "READER001", Instant.now()));

        verify(attendanceRecordRepository, never()).save(any(AttendanceRecord.class));
    }

    @Test
    void testRegisterRfidCard_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(rfidCardRepository.findByCardId("CARD123")).thenReturn(Optional.empty());
        when(rfidCardRepository.save(any(RfidCard.class))).thenReturn(testCard);

        RfidCard result = rfidAttendanceService.registerRfidCard(1L, "CARD123");

        assertNotNull(result);
        assertEquals("CARD123", result.getCardId());
        verify(rfidCardRepository, times(1)).save(any(RfidCard.class));
    }

    @Test
    void testRegisterRfidCard_EmployeeNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> rfidAttendanceService.registerRfidCard(99L, "CARD123"));
    }

    @Test
    void testGetReaderStatus() {
        when(rfidReaderRepository.findByDeviceId("READER001")).thenReturn(Optional.of(testReader));

        RfidReader result = rfidAttendanceService.getReaderStatus("READER001");

        assertNotNull(result);
        assertTrue(result.getIsActive());
        verify(rfidReaderRepository, times(1)).findByDeviceId("READER001");
    }
}
