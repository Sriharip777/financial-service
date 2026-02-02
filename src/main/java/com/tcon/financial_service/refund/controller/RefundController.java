package com.tcon.financial_service.refund.controller;


import com.tcon.financial_service.refund.dto.RefundDto;
import com.tcon.financial_service.refund.dto.RefundRequest;
import com.tcon.financial_service.refund.service.RefundProcessingService;
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

@Slf4j
@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundProcessingService refundService;

    @PostMapping
    public ResponseEntity<RefundDto> createRefund(@Valid @RequestBody RefundRequest request) {
        try {
            log.info("Processing refund for payment: {}", request.getPaymentId());
            RefundDto refund = refundService.processRefund(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(refund);
        } catch (IllegalArgumentException e) {
            log.error("Refund processing failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.error("Refund already exists or payment not completed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Refund processing failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{refundId}")
    public ResponseEntity<RefundDto> getRefund(@PathVariable String refundId) {
        try {
            RefundDto refund = refundService.getRefund(refundId);
            return ResponseEntity.ok(refund);
        } catch (IllegalArgumentException e) {
            log.error("Refund not found: {}", refundId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<RefundDto> getRefundByPaymentId(@PathVariable String paymentId) {
        try {
            RefundDto refund = refundService.getRefundByPaymentId(paymentId);
            return ResponseEntity.ok(refund);
        } catch (IllegalArgumentException e) {
            log.error("Refund not found for payment: {}", paymentId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<Page<RefundDto>> getStudentRefunds(
            @PathVariable String studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<RefundDto> refunds = refundService.getRefundsByStudent(studentId, pageable);
        return ResponseEntity.ok(refunds);
    }
}
