package com.smart.rh.service;

import com.smart.rh.dto.paie.PaieDto;
import com.smart.rh.dto.paie.PayrollGenerateRequest;
import com.smart.rh.entity.Contrat;
import com.smart.rh.entity.Employe;
import com.smart.rh.entity.Paie;
import com.smart.rh.exception.ResourceNotFoundException;
import com.smart.rh.mapper.PaieMapper;
import com.smart.rh.repository.ContratRepository;
import com.smart.rh.repository.EmployeRepository;
import com.smart.rh.repository.PaieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PaieService} — payroll generation + PDF output.
 * No Spring context. The PDF test uses the real iText library (on classpath)
 * against real entity instances, so no additional mocking is needed for PDF.
 */
@ExtendWith(MockitoExtension.class)
class PaieServiceTest {

    @Mock private PaieRepository    repository;
    @Mock private EmployeRepository employeRepository;
    @Mock private ContratRepository contratRepository;
    @Mock private PaieMapper        mapper;
    @Mock private AuditService      auditService;

    @InjectMocks private PaieService service;

    // ── generate ─────────────────────────────────────────────────────────────

    @Test
    void generate_twoEmployeesNoPriorRecords_createsTwoPayrollRows() {
        var request = new PayrollGenerateRequest(3, 2024);

        Employe emp1 = makeEmploye(1L, "Martin", "Jean");
        Employe emp2 = makeEmploye(2L, "Dupont", "Alice");
        when(employeRepository.findAll()).thenReturn(List.of(emp1, emp2));

        when(repository.existsByEmployeIdAndMoisAndAnnee(1L, 3, 2024)).thenReturn(false);
        when(repository.existsByEmployeIdAndMoisAndAnnee(2L, 3, 2024)).thenReturn(false);

        when(contratRepository.findByEmployeId(1L))
                .thenReturn(List.of(makeContrat(new BigDecimal("4500.00"), LocalDate.of(2020, 1, 1), null)));
        when(contratRepository.findByEmployeId(2L))
                .thenReturn(List.of(makeContrat(new BigDecimal("6000.00"), LocalDate.of(2019, 6, 1), null)));

        // Save returns the paie entity with id populated
        when(repository.save(any(Paie.class))).thenAnswer(inv -> {
            Paie p = inv.getArgument(0);
            p.setId(p.getEmploye().getId() * 100L);
            return p;
        });

        PaieDto dto1 = makePaieDto(100L, 1L, new BigDecimal("4500.00"), 3, 2024);
        PaieDto dto2 = makePaieDto(200L, 2L, new BigDecimal("6000.00"), 3, 2024);
        when(mapper.toDto(any(Paie.class))).thenReturn(dto1, dto2);

        List<PaieDto> result = service.generate(request);

        assertThat(result).hasSize(2);

        // Verify correct salary amounts are set on saved entities
        ArgumentCaptor<Paie> captor = ArgumentCaptor.forClass(Paie.class);
        verify(repository, times(2)).save(captor.capture());
        List<Paie> savedPaies = captor.getAllValues();
        assertThat(savedPaies).extracting(Paie::getMontant)
                .containsExactlyInAnyOrder(new BigDecimal("4500.00"), new BigDecimal("6000.00"));
        assertThat(savedPaies).allSatisfy(p -> {
            assertThat(p.getMois()).isEqualTo(3);
            assertThat(p.getAnnee()).isEqualTo(2024);
        });

        verify(auditService).log(eq("PAYROLL_GENERATE"), eq("Paie"), isNull(),
                contains("2 record(s) created"));
    }

    @Test
    void generate_employeeAlreadyHasPayrollForPeriod_skipsAndDoesNotDuplicate() {
        var request = new PayrollGenerateRequest(3, 2024);

        Employe emp1 = makeEmploye(1L, "Martin", "Jean");
        Employe emp2 = makeEmploye(2L, "Dupont", "Alice");
        when(employeRepository.findAll()).thenReturn(List.of(emp1, emp2));

        // emp1 already has a payroll for March 2024, emp2 does not
        when(repository.existsByEmployeIdAndMoisAndAnnee(1L, 3, 2024)).thenReturn(true);
        when(repository.existsByEmployeIdAndMoisAndAnnee(2L, 3, 2024)).thenReturn(false);

        when(contratRepository.findByEmployeId(2L))
                .thenReturn(List.of(makeContrat(new BigDecimal("5000.00"), LocalDate.of(2021, 3, 1), null)));
        when(repository.save(any(Paie.class))).thenAnswer(inv -> {
            Paie p = inv.getArgument(0);
            p.setId(999L);
            return p;
        });
        when(mapper.toDto(any(Paie.class))).thenReturn(
                makePaieDto(999L, 2L, new BigDecimal("5000.00"), 3, 2024));

        List<PaieDto> result = service.generate(request);

        assertThat(result).hasSize(1); // Only emp2 was created
        verify(repository, times(1)).save(any());  // emp1 was skipped

        ArgumentCaptor<Paie> captor = ArgumentCaptor.forClass(Paie.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEmploye()).isSameAs(emp2);
    }

    @Test
    void generate_employeeWithNoActiveContract_setsZeroSalary() {
        var request = new PayrollGenerateRequest(4, 2024);

        Employe emp = makeEmploye(3L, "Roux", "Paul");
        when(employeRepository.findAll()).thenReturn(List.of(emp));
        when(repository.existsByEmployeIdAndMoisAndAnnee(3L, 4, 2024)).thenReturn(false);
        // Expired contract — end date before target month start
        when(contratRepository.findByEmployeId(3L))
                .thenReturn(List.of(makeContrat(new BigDecimal("3000.00"),
                        LocalDate.of(2022, 1, 1), LocalDate.of(2023, 12, 31))));

        when(repository.save(any(Paie.class))).thenAnswer(inv -> {
            Paie p = inv.getArgument(0);
            p.setId(77L);
            return p;
        });
        when(mapper.toDto(any(Paie.class)))
                .thenReturn(makePaieDto(77L, 3L, BigDecimal.ZERO, 4, 2024));

        service.generate(request);

        ArgumentCaptor<Paie> captor = ArgumentCaptor.forClass(Paie.class);
        verify(repository).save(captor.capture());
        // No active contract → salary = ZERO
        assertThat(captor.getValue().getMontant()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void generate_emptyEmployeeList_createsNothingButStillAudits() {
        when(employeRepository.findAll()).thenReturn(List.of());

        List<PaieDto> result = service.generate(new PayrollGenerateRequest(1, 2024));

        assertThat(result).isEmpty();
        verify(repository, never()).save(any());
        verify(auditService).log(eq("PAYROLL_GENERATE"), eq("Paie"), isNull(),
                contains("0 record(s) created"));
    }

    @Test
    void generate_contractEndDateEqualsTargetMonthStart_contractIsStillActive() {
        // Contract ends exactly on 2024-03-01 → still active for March 2024
        var request = new PayrollGenerateRequest(3, 2024);
        Employe emp = makeEmploye(4L, "Lemaire", "Sophie");
        when(employeRepository.findAll()).thenReturn(List.of(emp));
        when(repository.existsByEmployeIdAndMoisAndAnnee(4L, 3, 2024)).thenReturn(false);
        when(contratRepository.findByEmployeId(4L))
                .thenReturn(List.of(makeContrat(new BigDecimal("3800.00"),
                        LocalDate.of(2020, 1, 1), LocalDate.of(2024, 3, 1))));
        // dateFin (2024-03-01) is NOT before targetMonthStart (2024-03-01) → included
        when(repository.save(any(Paie.class))).thenAnswer(inv -> {
            Paie p = inv.getArgument(0);
            p.setId(55L);
            return p;
        });
        when(mapper.toDto(any(Paie.class)))
                .thenReturn(makePaieDto(55L, 4L, new BigDecimal("3800.00"), 3, 2024));

        service.generate(request);

        ArgumentCaptor<Paie> captor = ArgumentCaptor.forClass(Paie.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getMontant()).isEqualByComparingTo("3800.00");
    }

    // ── generatePdf ──────────────────────────────────────────────────────────

    @Test
    void generatePdf_existingPayroll_returnsPdfBytes() {
        Paie paie = makeFullPaie(10L, new BigDecimal("4500.00"), 3, 2024);
        when(repository.findById(10L)).thenReturn(Optional.of(paie));

        byte[] pdf = service.generatePdf(10L);

        assertThat(pdf).isNotEmpty();
        // PDF magic bytes: %PDF (0x25 0x50 0x44 0x46)
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void generatePdf_largePayroll_producesValidPdfOfSubstantialSize() {
        // iText compresses content streams (FlateDecode); salary values are inside the
        // compressed stream and cannot be asserted on as plain strings.
        // Instead we verify the output is a well-formed, non-trivial PDF document.
        Paie paie = makeFullPaie(11L, new BigDecimal("10000.00"), 6, 2024);
        when(repository.findById(11L)).thenReturn(Optional.of(paie));

        byte[] pdf = service.generatePdf(11L);
        String pdfAsString = new String(pdf);

        // Must be a properly structured PDF
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
        assertThat(pdfAsString).contains("%%EOF");
        // A payslip with a table + salary row + employee details must exceed 500 bytes
        assertThat(pdf.length).isGreaterThan(500);
        // Font and page objects are uncompressed in PDF structure
        assertThat(pdfAsString).contains("/Type/Page");
        assertThat(pdfAsString).contains("Helvetica");
    }

    @Test
    void generatePdf_payrollNotFound_throwsResourceNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generatePdf(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Paie");
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_existingId_returnsMappedDto() {
        Paie paie = new Paie();
        paie.setId(5L);
        when(repository.findById(5L)).thenReturn(Optional.of(paie));
        PaieDto dto = makePaieDto(5L, 1L, new BigDecimal("3000.00"), 2, 2024);
        when(mapper.toDto(paie)).thenReturn(dto);

        PaieDto result = service.findById(5L);

        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.montant()).isEqualByComparingTo("3000.00");
    }

    @Test
    void findById_missingId_throwsResourceNotFound() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Paie");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Employe makeEmploye(Long id, String nom, String prenom) {
        Employe e = new Employe();
        e.setId(id);
        e.setNom(nom);
        e.setPrenom(prenom);
        e.setEmail(prenom.toLowerCase() + "." + nom.toLowerCase() + "@test.com");
        return e;
    }

    /** Creates a Paie with a real (non-mocked) Employe so iText can call getters. */
    private Paie makeFullPaie(Long id, BigDecimal montant, int mois, int annee) {
        Employe emp = new Employe();
        emp.setId(1L);
        emp.setNom("Doe");
        emp.setPrenom("John");
        emp.setEmail("john.doe@test.com");
        emp.setPosteLibelle("Développeur Java");

        Paie paie = new Paie();
        paie.setId(id);
        paie.setEmploye(emp);
        paie.setMontant(montant);
        paie.setMois(mois);
        paie.setAnnee(annee);
        return paie;
    }

    private Contrat makeContrat(BigDecimal salaire, LocalDate debut, LocalDate fin) {
        Contrat c = new Contrat();
        c.setType("CDI");
        c.setSalaire(salaire);
        c.setDateDebut(debut);
        c.setDateFin(fin);
        return c;
    }

    private PaieDto makePaieDto(Long id, Long employeId, BigDecimal montant, int mois, int annee) {
        return new PaieDto(id, employeId, "John Doe", montant, mois, annee,
                null, Instant.now(), null);
    }
}
