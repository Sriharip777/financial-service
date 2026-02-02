package com.tcon.financial_service.payout.controller;

import com.tcon.financial_service.payout.dto.EarningsDto;
import com.tcon.financial_service.payout.dto.PayoutDto;
import com.tcon.financial_service.payout.dto.PayoutRequest;
import com.tcon.financial_service.payout.service.EarningsCalculationService;
import com.tcon.financial_service.payout.service.PayoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payouts")
@RequiredArgsConstructor
public class PayoutController {

    private final PayoutService payoutService;
    private final EarningsCalculationService earningsService;

    @PostMapping
    public ResponseEntity<PayoutDto> createPayout(@Valid @RequestBody PayoutRequest request) {
        try {
            log.info("Creating payout for teacher: {}", request.getTeacherId());
            PayoutDto payout = payoutService.createPayout(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(payout);
        } catch (IllegalArgumentException e) {
            log.error("Payout creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Payout creation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{payoutId}/process")
    public ResponseEntity<PayoutDto> processPayout(@PathVariable String payoutId) {
        try {
            log.info("Processing payout: {}", payoutId);
            PayoutDto payout = payoutService.processPayout(payoutId);
            return ResponseEntity.ok(payout);
        } catch (IllegalArgumentException e) {
            log.error("Payout not found: {}", payoutId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Payout processing failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Payout processing failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{payoutId}")
    public ResponseEntity<PayoutDto> getPayout(@PathVariable String payoutId) {
        try {
            PayoutDto payout = payoutService.getPayout(payoutId);
            return ResponseEntity.ok(payout);
        } catch (IllegalArgumentException e) {
            log.error("Payout not found: {}", payoutId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<Page<PayoutDto>> getTeacherPayouts(
            @PathVariable String teacherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<PayoutDto> payouts = payoutService.getPayoutsByTeacher(teacherId, pageable);
        return ResponseEntity.ok(payouts);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PayoutDto>> getPendingPayouts() {
        List<PayoutDto> payouts = payoutService.getPendingPayouts();
        return ResponseEntity.ok(payouts);
    }

    @GetMapping("/earnings/teacher/{teacherId}")
    public ResponseEntity<EarningsDto> getTeacherEarnings(@PathVariable String teacherId) {
        try {
            EarningsDto earnings = earningsService.getTeacherEarnings(teacherId);
            return ResponseEntity.ok(earnings);
        } catch (IllegalArgumentException e) {
            log.error("Earnings not found for teacher: {}", teacherId);
            return ResponseEntity.notFound().build();
        }
    }
}
