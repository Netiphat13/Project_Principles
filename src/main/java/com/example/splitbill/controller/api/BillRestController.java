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
import jakarta.servlet.http.HttpSession;
import com.example.splitbill.exception.ConflictException;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/bills")
public class BillRestController {
    private final BillService service;
    public BillRestController(BillService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<BillResponse> create(@Valid @RequestBody BillRequest request, HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("userId");
        if (!sessionUserId.equals(request.createdById())) throw new ConflictException("Bill creator must be the logged-in user");
        BillResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/bills/" + created.id())).body(created);
    }
    @GetMapping("/{id}")
    public BillResponse get(@PathVariable Long id, HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("userId");
        BillResponse bill = service.getById(id);
        if (!sessionUserId.equals(bill.createdById())) {
            throw new ConflictException("You can only view your own bills");
        }
        return bill;
    }
    @GetMapping
    public Page<BillResponse> list(@RequestParam(required = false) Long userId,
                                    @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
                                    HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("userId");
        if (userId != null && !userId.equals(sessionUserId)) throw new ConflictException("You can only view your own bills");
        return service.findByCreator(sessionUserId, pageable);
    }
    @PutMapping("/{id}")
    public BillResponse update(@PathVariable Long id, @Valid @RequestBody BillRequest request) {
        return service.update(id, request);
    }
    @PatchMapping("/{id}/status")
    public BillResponse status(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        return service.updateStatus(id, body.get("status"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("userId");
        BillResponse bill = service.getById(id);
        if (!sessionUserId.equals(bill.createdById())) {
            throw new ConflictException("You can only delete your own bills");
        }
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
