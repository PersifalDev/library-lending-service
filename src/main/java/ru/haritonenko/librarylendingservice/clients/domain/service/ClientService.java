package ru.haritonenko.librarylendingservice.clients.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.haritonenko.librarylendingservice.clients.api.dto.ClientCreateRequestDto;
import ru.haritonenko.librarylendingservice.clients.api.dto.ClientUpdateRequestDto;
import ru.haritonenko.librarylendingservice.clients.domain.Client;
import ru.haritonenko.librarylendingservice.clients.domain.db.entity.ClientEntity;
import ru.haritonenko.librarylendingservice.clients.domain.db.repository.ClientRepository;
import ru.haritonenko.librarylendingservice.clients.domain.exception.ClientAlreadyExistsException;
import ru.haritonenko.librarylendingservice.clients.domain.exception.ClientNotFoundException;
import ru.haritonenko.librarylendingservice.clients.domain.exception.IllegalClientArgumentException;
import ru.haritonenko.librarylendingservice.clients.domain.mapper.ClientMapper;
import ru.haritonenko.librarylendingservice.clients.domain.role.ClientRole;
import ru.haritonenko.librarylendingservice.config.PageConfig;
import ru.haritonenko.librarylendingservice.config.properties.CacheProperties;
import ru.haritonenko.librarylendingservice.config.properties.SearchProperties;
import ru.haritonenko.librarylendingservice.lendings.domain.service.LendingCacheService;

@Slf4j
@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final PageConfig pageConfig;
    private final RedisTemplate<String, Client> redisClientTemplate;
    private final CacheProperties cacheProperties;
    private final SearchProperties searchProperties;
    private final LendingCacheService lendingCacheService;
    private final PasswordEncoder passwordEncoder;

    public ClientService(
            ClientRepository clientRepository,
            ClientMapper clientMapper,
            PageConfig pageConfig,
            ObjectProvider<RedisTemplate<String, Client>> redisClientTemplateProvider,
            CacheProperties cacheProperties,
            SearchProperties searchProperties,
            LendingCacheService lendingCacheService,
            PasswordEncoder passwordEncoder
    ) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
        this.pageConfig = pageConfig;
        this.redisClientTemplate = redisClientTemplateProvider.getIfAvailable();
        this.cacheProperties = cacheProperties;
        this.searchProperties = searchProperties;
        this.lendingCacheService = lendingCacheService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Client createClient(ClientCreateRequestDto requestDto) {
        if (requestDto == null) {
            log.warn("Client create request is null");
            throw new IllegalClientArgumentException("Client create request is null");
        }
        if (clientRepository.existsByLogin(requestDto.getLogin())) {
            log.warn("Client with login={} already exists", requestDto.getLogin());
            throw new ClientAlreadyExistsException(
                    String.format("Client with login=%s is already registered", requestDto.getLogin())
            );
        }

        ClientEntity clientEntity = ClientEntity.builder()
                .login(requestDto.getLogin())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .fullName(requestDto.getFullName())
                .birthDate(requestDto.getBirthDate())
                .clientRole(ClientRole.USER)
                .build();

        Client client = mapToDomain(clientRepository.save(clientEntity));
        cacheClient(client);
        log.info("Client created with id={}", client.getId());
        return client;
    }

    @Transactional(readOnly = true)
    public Client getClientById(Long id) {
        if (id == null) {
            log.warn("Client id is null");
            throw new IllegalClientArgumentException("Client id is null");
        }

        String key = getCacheKey(id);
        Client cachedClient = getClientFromCache(key);
        if (cachedClient != null) {
            return cachedClient;
        }

        Client client = mapToDomain(findClientById(id));
        cacheClient(client);
        return client;
    }

    @Transactional(readOnly = true)
    public Client findByLogin(String login) {
        if (login == null || login.trim().isEmpty()) {
            log.warn("Client login is null or blank");
            throw new IllegalClientArgumentException("Client login is null or blank");
        }

        return mapToDomain(clientRepository.findByLogin(login)
                .orElseThrow(() -> {
                    log.warn("Client with login={} not found", login);
                    return new ClientNotFoundException(String.format("Client with login=%s not found", login));
                }));
    }

    @Transactional(readOnly = true)
    public Page<Client> getClients(String fullName, Integer pageNumber, Integer pageSize) {
        String normalizedFullName = fullName == null ? searchProperties.getEmptyFilterValue() : fullName;
        return clientRepository
                .findByFullNameContainingIgnoreCase(
                        normalizedFullName,
                        pageConfig.pageable(pageNumber, pageSize)
                )
                .map(clientMapper::toDomain);
    }

    @Transactional
    public Client updateClient(Long id, ClientUpdateRequestDto requestDto) {
        if (id == null || requestDto == null) {
            log.warn("Client id or update request is null");
            throw new IllegalClientArgumentException("Client id or update request is null");
        }

        ClientEntity clientEntity = findClientById(id);
        clientEntity.setFullName(requestDto.getFullName());
        clientEntity.setBirthDate(requestDto.getBirthDate());

        Client client = mapToDomain(clientRepository.save(clientEntity));
        cacheClient(client);
        lendingCacheService.invalidateByClientId(client.getId());
        return client;
    }

    @Transactional(readOnly = true)
    public ClientEntity getClientEntityById(Long id) {
        if (id == null) {
            log.warn("Client id is null");
            throw new IllegalClientArgumentException("Client id is null");
        }
        return findClientById(id);
    }

    private ClientEntity findClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Client with id={} not found", id);
                    return new ClientNotFoundException(String.format("Client with id=%d not found", id));
                });
    }

    private Client mapToDomain(ClientEntity clientEntity) {
        return clientMapper.toDomain(clientEntity);
    }

    private Client getClientFromCache(String key) {
        if (redisClientTemplate == null) {
            return null;
        }
        try {
            return redisClientTemplate.opsForValue().get(key);
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable during client cache read, fallback to DB. key={}", key, ex);
            return null;
        }
    }

    private void cacheClient(Client client) {
        if (redisClientTemplate == null || client == null || client.getId() == null) {
            return;
        }
        String key = getCacheKey(client.getId());
        try {
            redisClientTemplate.opsForValue().set(key, client, cacheProperties.getClientsTtl());
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable during client cache write. key={}", key, ex);
        }
    }

    private String getCacheKey(Long id) {
        return cacheProperties.getClientKeyPrefix() + id;
    }
}
