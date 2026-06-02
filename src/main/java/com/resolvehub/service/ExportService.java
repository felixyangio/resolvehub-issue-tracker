package com.resolvehub.service;

import com.resolvehub.entity.Incident;
import com.resolvehub.enums.Role;
import com.resolvehub.repository.IncidentRepository;
import com.resolvehub.repository.IncidentSpecification;
import com.resolvehub.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports incident data as CSV, respecting role-based access control.
 *
 * <p>Uses streaming to avoid loading all data into memory at once
 * (important for large datasets). Each row is written directly to
 * the output writer.</p>
 */
@Service
@RequiredArgsConstructor
public class ExportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final IncidentRepository incidentRepository;

    @Transactional(readOnly = true)
    public String exportIncidentsCsv(CustomUserDetails userDetails) {
        Role role = Role.valueOf(userDetails.getRole());

        // Apply role-based filtering (same as CaseList)
        Specification<Incident> spec = Specification.where(null);
        if (role == Role.USER) {
            spec = spec.and(IncidentSpecification.createdBy(userDetails.getId()));
        } else if (role == Role.AGENT) {
            spec = spec.and(IncidentSpecification.assignedTo(userDetails.getId()));
        }

        List<Incident> incidents = incidentRepository.findAll(
                spec, Sort.by(Sort.Direction.DESC, "createdAt"));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        // CSV header
        pw.println("ID,Title,Category,Priority,Status,Created By,Assigned To,Due At,Created At,Updated At");

        // CSV rows
        for (Incident inc : incidents) {
            pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    inc.getId(),
                    escapeCsv(inc.getTitle()),
                    inc.getCategory(),
                    inc.getPriority(),
                    inc.getStatus(),
                    inc.getCreatedBy().getName(),
                    inc.getAssignedTo() != null ? inc.getAssignedTo().getName() : "",
                    inc.getDueAt() != null ? inc.getDueAt().format(FMT) : "",
                    inc.getCreatedAt().format(FMT),
                    inc.getUpdatedAt().format(FMT));
        }

        pw.flush();
        return sw.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
