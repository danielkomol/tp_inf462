package com.banque.customer.controller;

import com.banque.customer.dto.CustomerDTO.*;
import com.banque.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Clients", description = "API de gestion des profils clients")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Créer un profil client")
    public ResponseEntity<ApiResponse<CustomerResponse>> create(
            @Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Profil client créé", response));
    }

    @GetMapping
    @Operation(summary = "Lister tous les clients")
    public ResponseEntity<ApiResponse<java.util.List<CustomerResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Clients récupérés", customerService.getAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un client par ID")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Client trouvé", customerService.getById(id)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Consulter un client par userId")
    public ResponseEntity<ApiResponse<CustomerResponse>> getByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success("Client trouvé", customerService.getByUserId(userId)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un profil client")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(
            @PathVariable Long id, @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profil mis à jour", customerService.updateCustomer(id, request)));
    }

    @PutMapping("/{id}/kyc")
    @Operation(summary = "Mettre à jour le statut KYC")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateKyc(
            @PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        boolean approuve = "VERIFIE".equals(body.get("statut"));
        return ResponseEntity.ok(ApiResponse.success("KYC mis à jour", customerService.verifierKYC(id, approuve)));
    }

    @PutMapping("/{id}/verify")
    @Operation(summary = "Vérifier le KYC d'un client (admin/opérateur)")
    public ResponseEntity<ApiResponse<CustomerResponse>> verify(
            @PathVariable Long id, @RequestParam boolean approuve) {
        return ResponseEntity.ok(ApiResponse.success("Vérification mise à jour", customerService.verifierKYC(id, approuve)));
    }
}
