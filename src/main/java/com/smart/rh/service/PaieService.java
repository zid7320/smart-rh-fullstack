package com.smart.rh.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaieService {

    private final PaieRepository     repository;
    private final EmployeRepository  employeRepository;
    private final ContratRepository  contratRepository;
    private final PaieMapper         mapper;
    private final AuditService       auditService;

    // ── List ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<PaieDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public PaieDto findById(Long id) {
        return mapper.toDto(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<PaieDto> findByEmploye(Long employeId, Pageable pageable) {
        return repository.findByEmployeId(employeId, pageable).map(mapper::toDto);
    }

    // ── Generate ─────────────────────────────────────────────────────────────

    @Transactional
    public List<PaieDto> generate(PayrollGenerateRequest request) {
        List<Employe> employees = employeRepository.findAll();
        List<PaieDto> created = new ArrayList<>();

        for (Employe emp : employees) {
            // Skip if already generated
            if (repository.existsByEmployeIdAndMoisAndAnnee(emp.getId(), request.mois(), request.annee())) {
                continue;
            }

            // Get salary from most recent contract still active on the first day of the target month.
            // A contract is "active" when: no end date (CDI), OR end date >= first day of target month.
            LocalDate targetMonthStart = LocalDate.of(request.annee(), request.mois(), 1);
            BigDecimal salaire = contratRepository.findByEmployeId(emp.getId()).stream()
                    .filter(c -> c.getDateFin() == null
                              || !c.getDateFin().isBefore(targetMonthStart))
                    .max(Comparator.comparing(Contrat::getDateDebut))
                    .map(Contrat::getSalaire)
                    .orElse(BigDecimal.ZERO);

            Paie paie = new Paie();
            paie.setEmploye(emp);
            paie.setMontant(salaire);
            paie.setMois(request.mois());
            paie.setAnnee(request.annee());
            created.add(mapper.toDto(repository.save(paie)));
        }

        auditService.log("PAYROLL_GENERATE", "Paie", null,
                "Payroll generated for " + monthLabel(request.mois()) + "/" + request.annee()
                        + " — " + created.size() + " record(s) created");

        return created;
    }

    // ── PDF ───────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id) {
        Paie paie = getOrThrow(id);
        Employe emp = paie.getEmploye();

        try {
            Document doc = new Document(PageSize.A4);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // Header
            Font titleFont   = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font headerFont  = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font normalFont  = new Font(Font.FontFamily.HELVETICA, 11);
            Font smallFont   = new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC);

            doc.add(new Paragraph("SMART RH 4.0 — Bulletin de Paie", titleFont));
            doc.add(new Paragraph(" "));

            // Employee info table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            addCell(infoTable, "Employé", headerFont);
            addCell(infoTable, emp.getPrenom() + " " + emp.getNom(), normalFont);
            addCell(infoTable, "Email", headerFont);
            addCell(infoTable, emp.getEmail(), normalFont);
            addCell(infoTable, "Poste", headerFont);
            String poste = emp.getPoste() != null ? emp.getPoste().getTitre()
                         : (emp.getPosteLibelle() != null ? emp.getPosteLibelle() : "N/A");
            addCell(infoTable, poste, normalFont);
            doc.add(infoTable);
            doc.add(new Paragraph(" "));

            // Payroll table
            PdfPTable payTable = new PdfPTable(2);
            payTable.setWidthPercentage(100);
            addCell(payTable, "Mois / Année", headerFont);
            addCell(payTable, monthLabel(paie.getMois()) + " / " + paie.getAnnee(), normalFont);
            addCell(payTable, "Salaire Brut", headerFont);
            addCell(payTable, paie.getMontant().toPlainString() + " MAD", normalFont);
            addCell(payTable, "CNSS (4.48%)", headerFont);
            BigDecimal cnss = paie.getMontant().multiply(new BigDecimal("0.0448"))
                                  .setScale(2, java.math.RoundingMode.HALF_UP);
            addCell(payTable, "- " + cnss.toPlainString() + " MAD", normalFont);
            addCell(payTable, "AMO (2.26%)", headerFont);
            BigDecimal amo = paie.getMontant().multiply(new BigDecimal("0.0226"))
                                 .setScale(2, java.math.RoundingMode.HALF_UP);
            addCell(payTable, "- " + amo.toPlainString() + " MAD", normalFont);
            BigDecimal net = paie.getMontant().subtract(cnss).subtract(amo)
                                 .setScale(2, java.math.RoundingMode.HALF_UP);
            addCell(payTable, "Net à Payer", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
            addCell(payTable, net.toPlainString() + " MAD",
                              new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
            doc.add(payTable);
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Généré automatiquement par SMART RH 4.0", smallFont));
            doc.close();
            return baos.toByteArray();

        } catch (DocumentException e) {
            log.error("PDF generation failed for paie {}", id, e);
            throw new RuntimeException("Failed to generate payslip PDF", e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Paie getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paie", "id", id));
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setBorderColor(new BaseColor(200, 200, 200));
        table.addCell(cell);
    }

    private String monthLabel(int mois) {
        return new String[]{"","Jan","Fév","Mar","Avr","Mai","Jun",
                            "Jul","Aoû","Sep","Oct","Nov","Déc"}[mois];
    }
}
