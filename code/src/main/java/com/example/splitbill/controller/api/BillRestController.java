package com.example.splitbill.controller.api;

import com.example.splitbill.config.WebConfig;
import com.example.splitbill.dto.request.BillRequest;
import com.example.splitbill.dto.response.BillResponse;
import com.example.splitbill.service.BillService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<BillResponse> create(@Valid @RequestBody BillRequest request,
                                               @SessionAttribute(WebConfig.USER_ID) Long userId) {
        BillResponse created = service.create(request, userId);
        return ResponseEntity.created(URI.create("/api/v1/bills/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    public BillResponse get(@PathVariable Long id, @SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.getById(id, userId);
    }

    // บิลของผู้ใช้ที่ล็อกอินอยู่ เรียงจากใหม่ไปเก่า
    @GetMapping
    public Page<BillResponse> list(@SessionAttribute(WebConfig.USER_ID) Long userId,
                                   @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.findByCreator(userId, pageable);
    }

    @PutMapping("/{id}")
    public BillResponse update(@PathVariable Long id, @Valid @RequestBody BillRequest request,
                               @SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.update(id, request, userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @SessionAttribute(WebConfig.USER_ID) Long userId) {
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
