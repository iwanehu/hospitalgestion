package com.hospital.gestion.api.user.service;

import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.exception.ResourceNotFoundException;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;
import com.hospital.gestion.api.user.dto.PasswordChangeDTO;
import com.hospital.gestion.api.user.dto.UserRequestDTO;
import com.hospital.gestion.api.user.dto.UserResponseDTO;
import com.hospital.gestion.api.user.dto.UserUpdateDTO;
import com.hospital.gestion.api.user.entity.User;
import com.hospital.gestion.api.user.mapper.UserMapper;
import com.hospital.gestion.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Set;

import static com.hospital.gestion.api.user.specification.UserSpecification.createdFrom;
import static com.hospital.gestion.api.user.specification.UserSpecification.createdTo;
import static com.hospital.gestion.api.user.specification.UserSpecification.hasActiveStatus;
import static com.hospital.gestion.api.user.specification.UserSpecification.hasRole;
import static com.hospital.gestion.api.user.specification.UserSpecification.textContains;


import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {





    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final HospitalEntityHelper helper;



    private static final Set<String>
            ALLOWED_SORT_PROPERTIES = Set.of(
            "id",
            "role",
            "email",
            "isActive",
            "documentId",
            "firstName",
            "lastName",
            "phone",
            "createdAt",
            "updatedAt"
    );

    // ========================================
    // CREATE
    // ========================================
    // =======================================

    @Transactional
    public UserResponseDTO createUser(
            UserRequestDTO request
    ) {
        log.info(
                "Creating user with email: {} and role: {}",
                request.email(),
                request.role()
        );

        helper.validateRole(request.role());
        helper.validateName(request.firstName(), "First name");
        helper.validateName(request.lastName(), "Last name");
        helper.validatePassword(request.password());

        String normalizedEmail =
                normalizeEmail(request.email());

        String normalizedDocument =
                normalizeDocument(request.documentId());

        helper.validateUniqueEmail(normalizedEmail);
        helper.validateUniqueDocument(normalizedDocument);

        User user = userMapper.toEntity(request);

        user.setEmail(normalizedEmail);
        user.setDocumentId(normalizedDocument);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhone(
                helper.normalizeNullableText(request.phone())
        );
        user.setPassword(
                passwordEncoder.encode(request.password())
        );
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        log.info(
                "User created successfully with id: {}",
                savedUser.getId()
        );

        return userMapper.toResponseDTO(savedUser);
    }

    // ========================================
    // GET ALL
    // ========================================

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        log.info("Fetching all users");

        return userMapper.toResponseDTOList(
                userRepository.findAll()
        );
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getAllUsers(
            Pageable pageable
    ) {
        log.info(
                "Fetching all users with pagination: {}",
                pageable
        );

        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return userRepository.findAll(pageable)
                .map(userMapper::toResponseDTO);
    }

    // ========================================
    // GET ORDERED
    // ========================================

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsersOrdered() {
        log.info(
                "Fetching users ordered by last and first name"
        );

        return userMapper.toResponseDTOList(
                userRepository
                        .findAllByOrderByLastNameAscFirstNameAsc()
        );
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getActiveUsersOrdered() {
        log.info(
                "Fetching active users ordered by name"
        );

        return userMapper.toResponseDTOList(
                userRepository
                        .findByIsActiveTrueOrderByLastNameAscFirstNameAsc()
        );
    }

    // ========================================
    // GET BY ID
    // ========================================

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        log.info("Fetching user by id: {}", id);

        return userMapper.toResponseDTO(
                helper.findUserById(id)
        );
    }

    // ========================================
    // GET BY EMAIL
    // ========================================

    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(
            String email
    ) {
        String normalizedEmail =
                normalizeEmail(email);

        log.info(
                "Fetching user by email: {}",
                normalizedEmail
        );

        User user = userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: "
                                        + normalizedEmail
                        )
                );

        return userMapper.toResponseDTO(user);
    }

    // ========================================
    // GET BY DOCUMENT
    // ========================================

    @Transactional(readOnly = true)
    public UserResponseDTO getUserByDocumentId(
            String documentId
    ) {
        String normalizedDocument =
                normalizeDocument(documentId);

        log.info(
                "Fetching user by document: {}",
                normalizedDocument
        );

        User user = userRepository
                .findByDocumentIdIgnoreCase(
                        normalizedDocument
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with document ID: "
                                        + normalizedDocument
                        )
                );

        return userMapper.toResponseDTO(user);
    }

    // ========================================
    // GET BY ROLE
    // ========================================

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByRole(
            Role role
    ) {
        helper.validateRole(role);

        log.info("Fetching users by role: {}", role);

        return userMapper.toResponseDTOList(
                userRepository.findByRole(role)
        );
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getUsersByRole(
            Role role,
            Pageable pageable
    ) {
        helper.validateRole(role);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return userRepository
                .findByRole(role, pageable)
                .map(userMapper::toResponseDTO);
    }

    // ========================================
    // GET BY STATUS
    // ========================================

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByActiveStatus(
            Boolean isActive
    ) {
        helper.validateActiveStatus(isActive);

        log.info(
                "Fetching users by active status: {}",
                isActive
        );

        return userMapper.toResponseDTOList(
                userRepository.findByIsActive(isActive)
        );
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getUsersByActiveStatus(
            Boolean isActive,
            Pageable pageable
    ) {
        helper.validateActiveStatus(isActive);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return userRepository
                .findByIsActive(isActive, pageable)
                .map(userMapper::toResponseDTO);
    }

    // ========================================
    // GET BY ROLE AND STATUS
    // ========================================

    @Transactional(readOnly = true)
    public List<UserResponseDTO>
    getUsersByRoleAndActiveStatus(
            Role role,
            Boolean isActive
    ) {
        helper.validateRole(role);
        helper.validateActiveStatus(isActive);

        return userMapper.toResponseDTOList(
                userRepository.findByRoleAndIsActive(
                        role,
                        isActive
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDTO>
    getUsersByRoleAndActiveStatus(
            Role role,
            Boolean isActive,
            Pageable pageable
    ) {
        helper.validateRole(role);
        helper.validateActiveStatus(isActive);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return userRepository
                .findByRoleAndIsActive(
                        role,
                        isActive,
                        pageable
                )
                .map(userMapper::toResponseDTO);
    }

    // ========================================
    // SEARCH
    // ========================================

    @Transactional(readOnly = true)
    public List<UserResponseDTO> searchUsers(
            String text
    ) {
        helper.validateSearchText(text);

        log.info("Searching users with text: {}", text);

        return userMapper.toResponseDTOList(
                userRepository.searchUsers(text.trim())
        );
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> searchUsers(
            String text,
            Pageable pageable
    ) {
        helper.validateSearchText(text);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return userRepository
                .searchUsers(text.trim(), pageable)
                .map(userMapper::toResponseDTO);
    }

    // ========================================
    // UPDATE
    // ========================================

    @Transactional
    public UserResponseDTO updateUser(
            Long id,
            UserUpdateDTO request
    ) {
        log.info("Updating user by id: {}", id);

        User user = helper.findUserByIdForUpdate(id);

        helper.validateName(
                request.firstName(),
                "First name"
        );

        helper.validateName(
                request.lastName(),
                "Last name"
        );

        String normalizedEmail =
                normalizeEmail(request.email());

        boolean emailChanged =
                !normalizedEmail.equalsIgnoreCase(
                        user.getEmail()
                );

        if (emailChanged
                && userRepository
                .existsByEmailIgnoreCase(
                        normalizedEmail
                )) {
            throw new ConflictException(
                    "Email is already registered: "
                            + normalizedEmail
            );
        }

        userMapper.updateEntity(user, request);

        user.setEmail(normalizedEmail);
        user.setFirstName(
                request.firstName().trim()
        );
        user.setLastName(
                request.lastName().trim()
        );
        user.setPhone(
                helper.normalizeNullableText(request.phone())
        );

        User updatedUser = userRepository.saveAndFlush(user);

        log.info(
                "User updated successfully with id: {}",updatedUser.getId()
        );

        return userMapper.toResponseDTO(updatedUser);
    }

    // ========================================
    // CHANGE PASSWORD
    // ========================================

    @Transactional
    public void changePassword(
            Long id,
            PasswordChangeDTO request
    ) {
        log.info(
                "Changing password for user: {}",
                id
        );

        User user = helper.findUserByIdForUpdate(id);

        helper.validatePasswordChangeRequest(request);

        if (!passwordEncoder.matches(
                request.oldPassword(),
                user.getPassword()
        )) {
            throw new ConflictException(
                    "Current password is incorrect"
            );
        }

        if (!request.newPassword().equals(
                request.confirmPassword()
        )) {
            throw new IllegalArgumentException(
                    "New password and confirmation "
                            + "do not match"
            );
        }

        if (passwordEncoder.matches(
                request.newPassword(),
                user.getPassword()
        )) {
            throw new ConflictException(
                    "New password must be different "
                            + "from current password"
            );
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.newPassword()
                )
        );

        log.info(
                "Password changed successfully for user: {}",
                id
        );
    }

    // ========================================
    // ACTIVATE
    // ========================================

    @Transactional
    public void activateUser(Long id) {
        log.info("Activating user: {}", id);

        User user = helper.findUserByIdForUpdate(id);

        if (Boolean.TRUE.equals(user.getIsActive())) {
            throw new ConflictException(
                    "User is already active with id: " + id
            );
        }

        user.setIsActive(true);

        log.info(
                "User activated successfully: {}",
                id
        );
    }

    // ========================================
    // DEACTIVATE
    // ========================================

    @Transactional
    public void deactivateUser(Long id) {
        log.info("Deactivating user: {}", id);

        User user = helper.findUserByIdForUpdate(id);

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new ConflictException(
                    "User is already inactive with id: " + id
            );
        }

        user.setIsActive(false);

        log.info(
                "User deactivated successfully: {}",
                id
        );
    }

    // ========================================
    // DELETE
    // ========================================

    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user: {}", id);

        User user = helper.findUserByIdForUpdate(id);

        if (Boolean.TRUE.equals(user.getIsActive())) {
            throw new ConflictException(
                    "Active user cannot be deleted. "
                            + "Deactivate the user first"
            );
        }

        try {
            userRepository.delete(user);
            userRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException(
                    "User cannot be deleted because it is "
                            + "associated with hospital records"
            );
        }

        log.info(
                "User deleted successfully: {}",
                id
        );
    }

    // ========================================
    // EXISTS
    // ========================================

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCase(
                normalizeEmail(email)
        );
    }

    @Transactional(readOnly = true)
    public boolean existsByDocumentId(
            String documentId
    ) {
        return userRepository
                .existsByDocumentIdIgnoreCase(
                        normalizeDocument(documentId)
                );
    }

    // ========================================
    // COUNT
    // ========================================

    @Transactional(readOnly = true)
    public long countAllUsers() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public long countUsersByRole(Role role) {
        helper.validateRole(role);

        return userRepository.countByRole(role);
    }

    @Transactional(readOnly = true)
    public long countUsersByActiveStatus(
            Boolean isActive
    ) {
        helper.validateActiveStatus(isActive);

        return userRepository.countByIsActive(isActive);
    }

    @Transactional(readOnly = true)
    public long countUsersByRoleAndActiveStatus(
            Role role,
            Boolean isActive
    ) {
        helper.validateRole(role);
        helper.validateActiveStatus(isActive);

        return userRepository.countByRoleAndIsActive(
                role,
                isActive
        );
    }

    // ========================================
    // PRIVATE HELPERS
    // ========================================







    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email cannot be empty"
            );
        }

        if (email.trim().length() > 150) {
            throw new IllegalArgumentException(
                    "Email cannot exceed 150 characters"
            );
        }

        return email.trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeDocument(
            String documentId
    ) {
        if (documentId == null
                || documentId.isBlank()) {
            throw new IllegalArgumentException(
                    "Document ID cannot be empty"
            );
        }

        if (documentId.trim().length() > 50) {
            throw new IllegalArgumentException(
                    "Document ID cannot exceed 50 characters"
            );
        }

        return documentId.trim()
                .toUpperCase(Locale.ROOT);
    }


    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getUsers(
            String text,
            Role role,
            Boolean isActive,
            LocalDateTime createdFromValue,
            LocalDateTime createdToValue,
            Pageable pageable
    ) {
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        if (role != null) {
            helper.validateRole(role);
        }

        if (createdFromValue != null
                && createdToValue != null) {
            helper.validateDateRange(
                    createdFromValue,
                    createdToValue
            );
        }

        String normalizedText =
                normalizeOptionalFilter(text);

        log.info(
                "Fetching users with filters: "
                        + "text={}, role={}, active={}, "
                        + "createdFrom={}, createdTo={}",
                normalizedText,
                role,
                isActive,
                createdFromValue,
                createdToValue
        );

        Specification<User> specification =
                textContains(normalizedText)
                        .and(hasRole(role))
                        .and(hasActiveStatus(isActive))
                        .and(createdFrom(
                                createdFromValue
                        ))
                        .and(createdTo(
                                createdToValue
                        ));

        return userRepository
                .findAll(specification, pageable)
                .map(userMapper::toResponseDTO);
    }

    private String normalizeOptionalFilter(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }




}