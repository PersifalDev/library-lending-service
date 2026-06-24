package ru.haritonenko.librarylendingservice.lendings.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.haritonenko.librarylendingservice.lendings.api.dto.ActiveReaderResponseDto;
import ru.haritonenko.librarylendingservice.lendings.api.dto.LendingCreateRequestDto;
import ru.haritonenko.librarylendingservice.lendings.api.dto.LendingResponseDto;
import ru.haritonenko.librarylendingservice.lendings.api.dto.filter.LendingPageFilter;
import ru.haritonenko.librarylendingservice.lendings.domain.mapper.LendingMapper;
import ru.haritonenko.librarylendingservice.lendings.domain.service.LendingService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lendings")
@Tag(name = "Lendings", description = "Book lending and active reader report operations")
public class LendingController {

    private final LendingService lendingService;
    private final LendingMapper lendingMapper;

    @Operation(summary = "Create lending", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lending created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Client or book not found")
    })
    @PostMapping
    public ResponseEntity<LendingResponseDto> createLending(@Valid @RequestBody LendingCreateRequestDto requestDto) {
        log.info("POST /api/lendings");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lendingMapper.toResponseDto(lendingService.createLending(requestDto)));
    }

    @Operation(summary = "Get lending by id", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lending found"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Lending not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LendingResponseDto> getLendingById(
            @PathVariable("id") @Positive(message = "Lending id must be positive") Long id
    ) {
        log.info("GET /api/lendings/{}", id);
        return ResponseEntity.ok(lendingMapper.toResponseDto(lendingService.getLendingById(id)));
    }

    @Operation(summary = "Return book", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Lending not found"),
            @ApiResponse(responseCode = "409", description = "Lending already returned")
    })
    @PatchMapping("/{id}/return")
    public ResponseEntity<LendingResponseDto> returnLending(
            @PathVariable("id") @Positive(message = "Lending id must be positive") Long id
    ) {
        log.info("PATCH /api/lendings/{}/return", id);
        return ResponseEntity.ok(lendingMapper.toResponseDto(lendingService.returnLending(id)));
    }

    @Operation(summary = "Get active readers", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active readers page returned"),
            @ApiResponse(responseCode = "400", description = "Invalid page parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping("/active-readers")
    public ResponseEntity<Page<ActiveReaderResponseDto>> getActiveReaders(
            @Valid @ModelAttribute LendingPageFilter pageFilter
    ) {
        log.info("GET /api/lendings/active-readers");
        return ResponseEntity.ok(
                lendingService.getActiveReaders(pageFilter.getPageNumber(), pageFilter.getPageSize())
                        .map(lendingMapper::toActiveReaderResponseDto)
        );
    }
}
