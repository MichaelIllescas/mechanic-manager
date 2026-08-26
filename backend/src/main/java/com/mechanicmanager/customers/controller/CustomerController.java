package com.mechanicmanager.customers.controller;

import com.mechanicmanager.customers.dto.CreateCustomerRequest;
import com.mechanicmanager.customers.dto.CustomerResponse;
import com.mechanicmanager.customers.dto.UpdateCustomerRequest;
import com.mechanicmanager.customers.service.CustomerService;
import com.mechanicmanager.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping(CustomerRoutes.BASE)
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class CustomerController implements CustomerApi {

    private final CustomerService customerService;

    @Override
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> findAll(
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(customerService.findAll(currentUser.id(), search));
    }

    @Override
    @PostMapping
    public ResponseEntity<CustomerResponse> create(
            @Valid @RequestBody CreateCustomerRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            UriComponentsBuilder uriBuilder
    ) {
        CustomerResponse created = customerService.create(currentUser.id(), request);

        return ResponseEntity
                .created(uriBuilder.path(CustomerRoutes.BASE + CustomerRoutes.BY_ID)
                        .buildAndExpand(created.id())
                        .toUri())
                .body(created);
    }

    @Override
    @PutMapping(CustomerRoutes.BY_ID)
    public ResponseEntity<CustomerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(customerService.update(currentUser.id(), id, request));
    }
}
