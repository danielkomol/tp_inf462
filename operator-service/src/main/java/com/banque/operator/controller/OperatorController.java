package com.banque.operator.controller;

import com.banque.operator.dto.OperatorDTO.*;
import com.banque.operator.service.OperatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/operators")
@RequiredArgsConstructor
@Tag(name = "Opérateurs", description = "API de gestion des opérateurs financiers")
public class OperatorController {

    private final OperatorService operatorService;

    @PostMapping
    @Operation(summary = "Créer un opérateur financier")
    public ResponseEntity<ApiResponse<OperatorResponse>> create(
            @Valid @RequestBody CreateOperatorRequest request) {
        OperatorResponse response = operatorService.createOperator(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Opérateur créé", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un opérateur par ID")
    public ResponseEntity<ApiResponse<OperatorResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Opérateur trouvé", operatorService.getById(id)));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Consulter un opérateur par code")
    public ResponseEntity<ApiResponse<OperatorResponse>> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success("Opérateur trouvé", operatorService.getByCode(code)));
    }

    @GetMapping
    @Operation(summary = "Lister tous les opérateurs")
    public ResponseEntity<ApiResponse<List<OperatorResponse>>> getAll() {
        List<OperatorResponse> operators = operatorService.getAll();
        return ResponseEntity.ok(ApiResponse.success(operators.size() + " opérateur(s)", operators));
    }

    @GetMapping("/active")
    @Operation(summary = "Lister les opérateurs actifs")
    public ResponseEntity<ApiResponse<List<OperatorResponse>>> getActive() {
        List<OperatorResponse> operators = operatorService.getAllActive();
        return ResponseEntity.ok(ApiResponse.success(operators.size() + " opérateur(s) actif(s)", operators));
    }

    @PutMapping("/{id}/rules")
    @Operation(summary = "Modifier les règles métier d'un opérateur")
    public ResponseEntity<ApiResponse<OperatorResponse>> updateRules(
            @PathVariable Long id, @RequestBody UpdateRulesRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Règles mises à jour", operatorService.updateRules(id, request)));
    }

    @PutMapping("/{id}/suspend")
    @Operation(summary = "Suspendre un opérateur")
    public ResponseEntity<ApiResponse<OperatorResponse>> suspend(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Opérateur suspendu", operatorService.suspendre(id)));
    }

    @PutMapping("/{id}/reactivate")
    @Operation(summary = "Réactiver un opérateur")
    public ResponseEntity<ApiResponse<OperatorResponse>> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Opérateur réactivé", operatorService.reactiver(id)));
    }
}
