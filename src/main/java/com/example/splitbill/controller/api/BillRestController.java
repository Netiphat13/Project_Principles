package com.example.splitbill.controller.api;

import com.example.splitbill.dto.request.BillRequest;
import com.example.splitbill.dto.response.BillResponse;
import com.example.splitbill.service.BillService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/bills")
public class BillRestController {
    private final BillService service;
    public BillRestController(BillService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<BillResponse> create(@Valid @RequestBody BillRequest request) {
        BillResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/bills/" + created.id())).body(created);
    }
    @GetMapping("/{id}")
    public BillResponse get(@PathVariable Long id) { return service.getById(id); }
    @GetMapping
    public Page<BillResponse> list(@RequestParam(required = false) Long userId,
                                    @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return userId == null ? service.findAll(pageable) : service.findByCreator(userId, pageable);
    }
    @PutMapping("/{id}")
    public BillResponse update(@PathVariable Long id, @Valid @RequestBody BillRequest request) {
        return service.update(id, request);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
