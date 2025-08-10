package com.gradingsystem.tesla.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gradingsystem.tesla.dto.InstitutionDTO;
import com.gradingsystem.tesla.repository.InstitutionRepository;

@RestController
@RequestMapping("/api")
public class InstitutionController {

    @Autowired
    private InstitutionRepository institutionRepository;

    @GetMapping("/institutions")
    public List<InstitutionDTO> getAllInstitutions() {
        return institutionRepository.findAll().stream()
                .map(inst -> new InstitutionDTO(inst.getId(), inst.getName(), null, null, null, null))
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/institutions-data")
    public List<InstitutionDTO> getAllInstitutionsData() {
        return institutionRepository.findAll().stream()
                .map(inst -> new InstitutionDTO(inst.getId(), inst.getName(), inst.getEmail(), inst.getEmailDomain(), inst.getInviteCode(), inst.getVerificationMode()))
                .collect(Collectors.toList());
    }
}
