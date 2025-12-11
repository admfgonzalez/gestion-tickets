package com.institucion.ticketero.module_executives.api;

import com.institucion.ticketero.module_executives.application.ExecutiveService;

import com.institucion.ticketero.module_tickets.application.TicketService;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;



import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import java.util.List;



@RestController

@RequestMapping("/api/executives")

public class ExecutiveController {



    private final TicketService ticketService;

    private final ExecutiveService executiveService;



    public ExecutiveController(TicketService ticketService, ExecutiveService executiveService) {

        this.ticketService = ticketService;

        this.executiveService = executiveService;

    }



    // CRUD Operations for Executives



    @PostMapping

    public ResponseEntity<ExecutiveDetailsResponse> createExecutive(@Valid @RequestBody CreateExecutiveRequest request) {

        ExecutiveDetailsResponse newExecutive = executiveService.createExecutive(request);

        return new ResponseEntity<>(newExecutive, HttpStatus.CREATED);

    }



    @GetMapping

    public ResponseEntity<List<ExecutiveDetailsResponse>> getAllExecutives() {

        List<ExecutiveDetailsResponse> executives = executiveService.getAllExecutives();

        return ResponseEntity.ok(executives);

    }



    @GetMapping("/{id}")

    public ResponseEntity<ExecutiveDetailsResponse> getExecutiveById(@PathVariable Long id) {

        ExecutiveDetailsResponse executive = executiveService.getExecutiveById(id);

        return ResponseEntity.ok(executive);

    }



    @PutMapping("/{id}")

    public ResponseEntity<ExecutiveDetailsResponse> updateExecutive(@PathVariable Long id, @Valid @RequestBody UpdateExecutiveRequest request) {

        ExecutiveDetailsResponse updatedExecutive = executiveService.updateExecutive(id, request);

        return ResponseEntity.ok(updatedExecutive);

    }



    @DeleteMapping("/{id}")

    public ResponseEntity<Void> deleteExecutive(@PathVariable Long id) {

        executiveService.deleteExecutive(id);

        return ResponseEntity.noContent().build();

    }





    // Existing Executive Controls



    @PostMapping("/{id}/end-ticket")

    public ResponseEntity<Void> endCurrentTicket(@PathVariable Long id) {

        ticketService.closeCurrentTicketForExecutive(id);

        return ResponseEntity.ok().build();

    }



    @PutMapping("/{id}/attention-types")

    public ResponseEntity<Void> updateAttentionTypes(@PathVariable Long id, @RequestBody UpdateAttentionTypesRequest request) {

        executiveService.updateSupportedAttentionTypes(id, request.attentionTypes());

        return ResponseEntity.ok().build();

    }

}
