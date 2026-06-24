package ru.haritonenko.librarylendingservice.clients.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.haritonenko.librarylendingservice.clients.api.dto.ClientCreateRequestDto;
import ru.haritonenko.librarylendingservice.clients.api.dto.ClientResponseDto;
import ru.haritonenko.librarylendingservice.clients.api.dto.ClientUpdateRequestDto;
import ru.haritonenko.librarylendingservice.clients.api.dto.authorization.ClientCredentials;
import ru.haritonenko.librarylendingservice.clients.api.dto.filter.ClientPageFilter;
import ru.haritonenko.librarylendingservice.clients.api.dto.response.ClientAuthDebugResponse;
import ru.haritonenko.librarylendingservice.clients.domain.mapper.ClientMapper;
import ru.haritonenko.librarylendingservice.clients.domain.service.ClientService;
import ru.haritonenko.librarylendingservice.clients.security.custom.authentification.AuthClient;
import ru.haritonenko.librarylendingservice.clients.security.jwt.response.JwtResponse;
import ru.haritonenko.librarylendingservice.clients.security.service.AuthenticationService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clients")
@Tag(name = "Clients", description = "Client registration, authentication and profile operations")
public class ClientController {

    private final ClientService clientService;
    private final ClientMapper clientMapper;
    private final AuthenticationService authenticationService;

    @Operation(summary = "Register client", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Client registered"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Client with login already exists")
    })
    @PostMapping
    public ResponseEntity<ClientResponseDto> createClient(@Valid @RequestBody ClientCreateRequestDto requestDto) {
        log.info("POST /api/clients");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(clientMapper.toResponseDto(clientService.createClient(requestDto)));
    }

    @Operation(summary = "Authenticate client", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JWT issued"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/auth")
    public ResponseEntity<JwtResponse> authenticateClient(@Valid @RequestBody ClientCredentials credentials) {
        log.info("POST /api/clients/auth login={}", credentials.getLogin());
        return ResponseEntity.ok(new JwtResponse(authenticationService.authenticate(credentials)));
    }

    @Operation(summary = "Show current authentication", security = {})
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Current authentication info",
                    content = @Content(schema = @Schema(implementation = ClientAuthDebugResponse.class))
            )
    })
    @GetMapping("/debug/auth")
    public ClientAuthDebugResponse auth(@Parameter(hidden = true) Authentication authentication) {
        log.info("GET /api/clients/debug/auth");
        if (authentication == null) {
            return new ClientAuthDebugResponse(null, null);
        }
        List<String> authorities = authentication.getAuthorities()
                .stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        return new ClientAuthDebugResponse(resolveAuthenticationName(authentication), authorities);
    }

    @Operation(summary = "Get client by id", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client found"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDto> getClientById(
            @PathVariable("id") @Positive(message = "Client id must be positive") Long id
    ) {
        log.info("GET /api/clients/{}", id);
        return ResponseEntity.ok(clientMapper.toResponseDto(clientService.getClientById(id)));
    }

    @Operation(summary = "Search clients", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clients page returned"),
            @ApiResponse(responseCode = "400", description = "Invalid page parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<ClientResponseDto>> getClients(
            @RequestParam(required = false) String fullName,
            @Valid @ModelAttribute ClientPageFilter pageFilter
    ) {
        log.info("GET /api/clients/search");
        return ResponseEntity.ok(
                clientService.getClients(fullName, pageFilter.getPageNumber(), pageFilter.getPageSize())
                        .map(clientMapper::toResponseDto)
        );
    }

    @Operation(summary = "Update client", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDto> updateClient(
            @PathVariable("id") @Positive(message = "Client id must be positive") Long id,
            @Valid @RequestBody ClientUpdateRequestDto requestDto
    ) {
        log.info("PUT /api/clients/{}", id);
        return ResponseEntity.ok(clientMapper.toResponseDto(clientService.updateClient(id, requestDto)));
    }

    private String resolveAuthenticationName(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthClient) {
            return ((AuthClient) principal).getLogin();
        }
        return authentication.getName();
    }
}
