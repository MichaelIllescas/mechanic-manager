package com.mechanicmanager.quotes.controller;

import com.mechanicmanager.quotes.dto.CreateQuoteRequest;
import com.mechanicmanager.quotes.dto.QuotePageResponse;
import com.mechanicmanager.quotes.dto.QuoteResponse;
import com.mechanicmanager.quotes.dto.UpdateQuoteRequest;
import com.mechanicmanager.quotes.dto.UpdateQuoteStatusRequest;
import com.mechanicmanager.quotes.model.QuoteStatus;
import com.mechanicmanager.quotes.service.QuoteService;
import com.mechanicmanager.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping(QuoteRoutes.BASE)
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class QuoteController implements QuoteApi {

    private final QuoteService quoteService;

    @Override
    @GetMapping
    public ResponseEntity<QuotePageResponse> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) QuoteStatus status,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(defaultValue = "false") boolean available,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(quoteService.search(
                currentUser.id(),
                search,
                status,
                vehicleId,
                available,
                page,
                size
        ));
    }

    @Override
    @GetMapping(QuoteRoutes.BY_ID)
    public ResponseEntity<QuoteResponse> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(quoteService.findById(currentUser.id(), id));
    }

    @Override
    @PostMapping
    public ResponseEntity<QuoteResponse> create(
            @Valid @RequestBody CreateQuoteRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            UriComponentsBuilder uriBuilder
    ) {
        QuoteResponse created = quoteService.create(currentUser.id(), request);

        return ResponseEntity
                .created(uriBuilder.path(QuoteRoutes.BASE + QuoteRoutes.BY_ID)
                        .buildAndExpand(created.id())
                        .toUri())
                .body(created);
    }

    @Override
    @PutMapping(QuoteRoutes.BY_ID)
    public ResponseEntity<QuoteResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuoteRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(quoteService.update(
                currentUser.id(),
                id,
                request
        ));
    }

    @Override
    @PatchMapping(QuoteRoutes.STATUS)
    public ResponseEntity<QuoteResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuoteStatusRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(quoteService.updateStatus(
                currentUser.id(),
                id,
                request
        ));
    }

    @Override
    @DeleteMapping(QuoteRoutes.BY_ID)
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        quoteService.delete(currentUser.id(), id);

        return ResponseEntity.noContent().build();
    }
}
