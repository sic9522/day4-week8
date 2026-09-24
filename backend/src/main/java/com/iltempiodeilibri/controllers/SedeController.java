package com.iltempiodeilibri.controllers;

import com.iltempiodeilibri.dto.SedeResponse;
import com.iltempiodeilibri.repositories.SedeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sedi")
@RequiredArgsConstructor
public class SedeController {

    private final SedeRepository sedeRepository;

    // Aperto a tutti: chi si registra deve poter scegliere la sede prima di avere un account
    @PreAuthorize("permitAll()")
    @GetMapping
    public List<SedeResponse> tutte() {
        return sedeRepository.findAll(Sort.by("nome")).stream()
                .map(SedeResponse::of)
                .toList();
    }
}
