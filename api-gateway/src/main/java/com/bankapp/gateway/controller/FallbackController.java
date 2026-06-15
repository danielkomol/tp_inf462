package com.bankapp.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Réponses de fallback retournées par le Circuit Breaker
 * quand un service est indisponible.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/customers")
    public Mono<ResponseEntity<Map<String, String>>> customerFallback() {
        return fallback("customer-service");
    }

    @GetMapping("/accounts")
    public Mono<ResponseEntity<Map<String, String>>> accountFallback() {
        return fallback("account-service");
    }

    @GetMapping("/transactions")
    public Mono<ResponseEntity<Map<String, String>>> transactionFallback() {
        return fallback("transaction-service");
    }

    @GetMapping("/loans")
    public Mono<ResponseEntity<Map<String, String>>> loanFallback() {
        return fallback("loan-service");
    }

    @GetMapping("/operators")
    public Mono<ResponseEntity<Map<String, String>>> operatorFallback() {
        return fallback("operator-service");
    }

    @GetMapping("/documents")
    public Mono<ResponseEntity<Map<String, String>>> documentFallback() {
        return fallback("document-service");
    }

    @GetMapping("/reports")
    public Mono<ResponseEntity<Map<String, String>>> reportingFallback() {
        return fallback("reporting-service");
    }

    @GetMapping("/ocr")
    public Mono<ResponseEntity<Map<String, String>>> ocrFallback() {
        return fallback("ocr-service");
    }

    private Mono<ResponseEntity<Map<String, String>>> fallback(String service) {
        return Mono.just(ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "status", "error",
                "message", "Le service " + service + " est temporairement indisponible. Veuillez réessayer plus tard."
            ))
        );
    }
}
