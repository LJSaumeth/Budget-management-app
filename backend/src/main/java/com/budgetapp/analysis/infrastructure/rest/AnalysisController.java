package com.budgetapp.analysis.infrastructure.rest;

import com.budgetapp.analysis.application.AnalysisService;
import com.budgetapp.analysis.infrastructure.dto.AnalysisResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
@Validated
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/suggestions")
    public ResponseEntity<AnalysisResponse> getSuggestions(@RequestParam Long budgetId) {
        AnalysisResponse response = analysisService.analyze(budgetId);
        return ResponseEntity.ok(response);
    }
}
