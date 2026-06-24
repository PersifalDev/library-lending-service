package ru.haritonenko.librarylendingservice.lendings.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.haritonenko.librarylendingservice.books.domain.db.entity.BookEntity;
import ru.haritonenko.librarylendingservice.books.domain.service.BookService;
import ru.haritonenko.librarylendingservice.clients.domain.db.entity.ClientEntity;
import ru.haritonenko.librarylendingservice.clients.domain.service.ClientService;
import ru.haritonenko.librarylendingservice.config.PageConfig;
import ru.haritonenko.librarylendingservice.lendings.api.dto.LendingCreateRequestDto;
import ru.haritonenko.librarylendingservice.lendings.domain.Lending;
import ru.haritonenko.librarylendingservice.lendings.domain.db.entity.LendingEntity;
import ru.haritonenko.librarylendingservice.lendings.domain.db.repository.LendingRepository;
import ru.haritonenko.librarylendingservice.lendings.domain.exception.IllegalLendingArgumentException;
import ru.haritonenko.librarylendingservice.lendings.domain.exception.IllegalLendingStateException;
import ru.haritonenko.librarylendingservice.lendings.domain.exception.LendingNotFoundException;
import ru.haritonenko.librarylendingservice.lendings.domain.mapper.LendingMapper;

import java.time.LocalDateTime;

@Slf4j
@Service
public class LendingService {

    private final LendingRepository lendingRepository;
    private final BookService bookService;
    private final ClientService clientService;
    private final LendingMapper lendingMapper;
    private final PageConfig pageConfig;
    private final LendingCacheService lendingCacheService;

    public LendingService(
            LendingRepository lendingRepository,
            BookService bookService,
            ClientService clientService,
            LendingMapper lendingMapper,
            PageConfig pageConfig,
            LendingCacheService lendingCacheService
    ) {
        this.lendingRepository = lendingRepository;
        this.bookService = bookService;
        this.clientService = clientService;
        this.lendingMapper = lendingMapper;
        this.pageConfig = pageConfig;
        this.lendingCacheService = lendingCacheService;
    }

    @Transactional
    public Lending createLending(LendingCreateRequestDto requestDto) {
        if (requestDto == null) {
            log.warn("Lending create request is null");
            throw new IllegalLendingArgumentException("Lending create request is null");
        }

        ClientEntity clientEntity = clientService.getClientEntityById(requestDto.getClientId());
        BookEntity bookEntity = bookService.getBookEntityById(requestDto.getBookId());

        LendingEntity lendingEntity = LendingEntity.builder()
                .client(clientEntity)
                .book(bookEntity)
                .takenAt(requestDto.getTakenAt() == null ? LocalDateTime.now() : requestDto.getTakenAt())
                .build();

        Lending lending = mapToDomain(lendingRepository.save(lendingEntity));
        lendingCacheService.cacheLending(lending);
        log.info("Lending created with id={}", lending.getId());
        return lending;
    }

    @Transactional(readOnly = true)
    public Lending getLendingById(Long id) {
        if (id == null) {
            log.warn("Lending id is null");
            throw new IllegalLendingArgumentException("Lending id is null");
        }

        Lending cachedLending = lendingCacheService.getLending(id);
        if (cachedLending != null) {
            return cachedLending;
        }

        Lending lending = mapToDomain(findLendingById(id));
        lendingCacheService.cacheLending(lending);
        return lending;
    }

    @Transactional
    public Lending returnLending(Long id) {
        if (id == null) {
            log.warn("Lending id is null");
            throw new IllegalLendingArgumentException("Lending id is null");
        }

        LendingEntity lendingEntity = findLendingById(id);
        if (lendingEntity.getReturnedAt() != null) {
            log.warn("Lending with id={} already returned", id);
            throw new IllegalLendingStateException(String.format("Lending with id=%d already returned", id));
        }

        lendingEntity.setReturnedAt(LocalDateTime.now());
        Lending lending = mapToDomain(lendingRepository.save(lendingEntity));
        lendingCacheService.invalidateLending(id);
        lendingCacheService.cacheLending(lending);
        return lending;
    }

    @Transactional(readOnly = true)
    public Page<Lending> getActiveReaders(Integer pageNumber, Integer pageSize) {
        return lendingRepository
                .findByReturnedAtIsNullOrderByTakenAtDesc(pageConfig.pageable(pageNumber, pageSize))
                .map(lendingMapper::toDomain);
    }

    private LendingEntity findLendingById(Long id) {
        return lendingRepository.findWithClientAndBookById(id)
                .orElseThrow(() -> {
                    log.warn("Lending with id={} not found", id);
                    return new LendingNotFoundException(String.format("Lending with id=%d not found", id));
                });
    }

    private Lending mapToDomain(LendingEntity lendingEntity) {
        return lendingMapper.toDomain(lendingEntity);
    }

}
