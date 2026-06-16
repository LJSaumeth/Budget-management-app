package com.budgetapp.simulation.infrastructure.rest;

import com.budgetapp.simulation.application.SimulationService;
import com.budgetapp.simulation.infrastructure.dto.SimulationRequest;
import com.budgetapp.simulation.infrastructure.dto.SimulationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulations")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/savings")
    public ResponseEntity<SimulationResponse> simulateSavings(
            @Valid @RequestBody SimulationRequest request) {
        SimulationResponse response = simulationService.simulate(request);
        return ResponseEntity.ok(response);
    }
}
