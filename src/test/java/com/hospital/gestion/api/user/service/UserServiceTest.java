package com.hospital.gestion.api.user.service;

import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;
import com.hospital.gestion.api.user.dto.PasswordChangeDTO;
import com.hospital.gestion.api.user.dto.UserRequestDTO;
import com.hospital.gestion.api.user.dto.UserResponseDTO;
import com.hospital.gestion.api.user.dto.UserUpdateDTO;
import com.hospital.gestion.api.user.entity.User;
import com.hospital.gestion.api.user.mapper.UserMapper;
import com.hospital.gestion.api.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HospitalEntityHelper helper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserResponseDTO response;

    @BeforeEach
    void setUp() {
        response = mock(UserResponseDTO.class);

        user = User.builder()
                .id(1L)
                .role(Role.ADMIN)
                .email("admin@hospital.com")
                .password("encoded-password")
                .isActive(true)
                .documentId("12345678Z")
                .firstName("Carlos")
                .lastName("Administrador")
                .phone("600000001")
                .build();
    }

    @Test
    void createUserNormalizesEncodesAndSavesUser() {
        UserRequestDTO request =
                new UserRequestDTO(
                        Role.ADMIN,
                        " ADMIN@HOSPITAL.COM ",
                        "ValidPassword123!",
                        " 12345678z ",
                        " Carlos ",
                        " Administrador ",
                        " 600000001 "
                );

        User newUser = User.builder()
                .role(Role.ADMIN)
                .build();

        when(userMapper.toEntity(request))
                .thenReturn(newUser);

        when(
                helper.normalizeNullableText(
                        " 600000001 "
                )
        ).thenReturn("600000001");

        when(
                passwordEncoder.encode(
                        "ValidPassword123!"
                )
        ).thenReturn("encoded-password");

        when(userRepository.save(newUser))
                .thenReturn(newUser);

        when(userMapper.toResponseDTO(newUser))
                .thenReturn(response);

        UserResponseDTO result =
                userService.createUser(request);

        assertSame(response, result);

        assertEquals(
                "admin@hospital.com",
                newUser.getEmail()
        );

        assertEquals(
                "12345678Z",
                newUser.getDocumentId()
        );

        assertEquals("Carlos", newUser.getFirstName());

        assertEquals(
                "Administrador",
                newUser.getLastName()
        );

        assertEquals("600000001", newUser.getPhone());

        assertEquals(
                "encoded-password",
                newUser.getPassword()
        );

        assertTrue(newUser.getIsActive());

        verify(helper).validateRole(Role.ADMIN);

        verify(helper).validateName(
                " Carlos ",
                "First name"
        );

        verify(helper).validateName(
                " Administrador ",
                "Last name"
        );

        verify(helper).validatePassword(
                "ValidPassword123!"
        );

        verify(helper).validateUniqueEmail(
                "admin@hospital.com"
        );

        verify(helper).validateUniqueDocument(
                "12345678Z"
        );

        verify(userRepository).save(newUser);
    }

    @Test
    void getUserByEmailNormalizesEmail() {
        when(
                userRepository.findByEmailIgnoreCase(
                        "admin@hospital.com"
                )
        ).thenReturn(Optional.of(user));

        when(userMapper.toResponseDTO(user))
                .thenReturn(response);

        UserResponseDTO result =
                userService.getUserByEmail(
                        " ADMIN@HOSPITAL.COM "
                );

        assertSame(response, result);

        verify(userRepository)
                .findByEmailIgnoreCase(
                        "admin@hospital.com"
                );
    }

    @Test
    void updateUserNormalizesAndSavesChanges() {
        UserUpdateDTO request =
                new UserUpdateDTO(
                        " NEW.ADMIN@HOSPITAL.COM ",
                        " Carlos ",
                        " Actualizado ",
                        " 611111111 "
                );

        when(helper.findUserByIdForUpdate(1L))
                .thenReturn(user);

        when(
                userRepository.existsByEmailIgnoreCase(
                        "new.admin@hospital.com"
                )
        ).thenReturn(false);

        when(
                helper.normalizeNullableText(
                        " 611111111 "
                )
        ).thenReturn("611111111");

        when(userRepository.saveAndFlush(user))
                .thenReturn(user);

        when(userMapper.toResponseDTO(user))
                .thenReturn(response);

        UserResponseDTO result =
                userService.updateUser(
                        1L,
                        request
                );

        assertSame(response, result);

        assertEquals(
                "new.admin@hospital.com",
                user.getEmail()
        );

        assertEquals("Carlos", user.getFirstName());

        assertEquals(
                "Actualizado",
                user.getLastName()
        );

        assertEquals("611111111", user.getPhone());

        verify(userMapper).updateEntity(user, request);
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    void updateUserRejectsDuplicateEmail() {
        UserUpdateDTO request =
                new UserUpdateDTO(
                        " doctor@hospital.com ",
                        "Carlos",
                        "Administrador",
                        null
                );

        when(helper.findUserByIdForUpdate(1L))
                .thenReturn(user);

        when(
                userRepository.existsByEmailIgnoreCase(
                        "doctor@hospital.com"
                )
        ).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.updateUser(
                        1L,
                        request
                )
        );

        assertEquals(
                "Email is already registered: doctor@hospital.com",
                exception.getMessage()
        );

        verify(userMapper, never())
                .updateEntity(any(), any());

        verify(userRepository, never())
                .saveAndFlush(any(User.class));
    }

    @Test
    void changePasswordEncodesNewPassword() {
        PasswordChangeDTO request =
                new PasswordChangeDTO(
                        "OldPassword123!",
                        "NewPassword123!",
                        "NewPassword123!"
                );

        when(helper.findUserByIdForUpdate(1L))
                .thenReturn(user);

        when(
                passwordEncoder.matches(
                        "OldPassword123!",
                        "encoded-password"
                )
        ).thenReturn(true);

        when(
                passwordEncoder.matches(
                        "NewPassword123!",
                        "encoded-password"
                )
        ).thenReturn(false);

        when(
                passwordEncoder.encode(
                        "NewPassword123!"
                )
        ).thenReturn("new-encoded-password");

        userService.changePassword(1L, request);

        assertEquals(
                "new-encoded-password",
                user.getPassword()
        );

        verify(helper)
                .validatePasswordChangeRequest(request);

        verify(passwordEncoder).encode(
                "NewPassword123!"
        );
    }

    @Test
    void changePasswordRejectsIncorrectCurrentPassword() {
        PasswordChangeDTO request =
                new PasswordChangeDTO(
                        "WrongPassword123!",
                        "NewPassword123!",
                        "NewPassword123!"
                );

        when(helper.findUserByIdForUpdate(1L))
                .thenReturn(user);

        when(
                passwordEncoder.matches(
                        "WrongPassword123!",
                        "encoded-password"
                )
        ).thenReturn(false);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.changePassword(
                        1L,
                        request
                )
        );

        assertEquals(
                "Current password is incorrect",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    void changePasswordRejectsDifferentConfirmation() {
        PasswordChangeDTO request =
                new PasswordChangeDTO(
                        "OldPassword123!",
                        "NewPassword123!",
                        "DifferentPassword123!"
                );

        when(helper.findUserByIdForUpdate(1L))
                .thenReturn(user);

        when(
                passwordEncoder.matches(
                        "OldPassword123!",
                        "encoded-password"
                )
        ).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(
                        1L,
                        request
                )
        );

        assertEquals(
                "New password and confirmation do not match",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    void changePasswordRejectsCurrentPasswordAsNew() {
        PasswordChangeDTO request =
                new PasswordChangeDTO(
                        "OldPassword123!",
                        "OldPassword123!",
                        "OldPassword123!"
                );

        when(helper.findUserByIdForUpdate(1L))
                .thenReturn(user);

        when(
                passwordEncoder.matches(
                        "OldPassword123!",
                        "encoded-password"
                )
        ).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.changePassword(
                        1L,
                        request
                )
        );

        assertEquals(
                "New password must be different from current password",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    void activateUserActivatesInactiveUser() {
        user.setIsActive(false);

        when(helper.findUserByIdForUpdate(1L))
                .thenReturn(user);

        userService.activateUser(1L);

        assertTrue(user.getIsActive());
    }

    @Test
    void activateUserRejectsAlreadyActiveUser() {
        when(helper.findUserByIdForUpdate(1L))
                .thenReturn(user);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.activateUser(1L)
        );

        assertEquals(
                "User is already active with id: 1",
                exception.getMessage()
        );
    }

    @Test
    void deactivateUserDeactivatesActiveUser() {
        when(helper.findUserByIdForUpdate(1L))
                .thenReturn(user);

        userService.deactivateUser(1L);

        assertFalse(user.getIsActive());
    }

    @Test
    void deleteUserRejectsActiveUser() {
        when(helper.findUserByIdForUpdate(1L))
                .thenReturn(user);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.deleteUser(1L)
        );

        assertEquals(
                "Active user cannot be deleted. "
                        + "Deactivate the user first",
                exception.getMessage()
        );

        verify(userRepository, never())
                .delete(any(User.class));
    }

    @Test
    void deleteUserDeletesInactiveUser() {
        user.setIsActive(false);

        when(helper.findUserByIdForUpdate(1L))
                .thenReturn(user);

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
        verify(userRepository).flush();
    }

    @Test
    void deleteUserTranslatesDatabaseConstraintViolation() {
        user.setIsActive(false);

        when(helper.findUserByIdForUpdate(1L))
                .thenReturn(user);

        doThrow(
                new DataIntegrityViolationException(
                        "Foreign key constraint"
                )
        ).when(userRepository).flush();

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.deleteUser(1L)
        );

        assertEquals(
                "User cannot be deleted because it is "
                        + "associated with hospital records",
                exception.getMessage()
        );

        verify(userRepository).delete(user);
        verify(userRepository).flush();
    }

    @SuppressWarnings("unchecked")
    @Test
    void getUsersAppliesAllFilters() {
        Pageable pageable = PageRequest.of(
                0,
                20,
                Sort.by("lastName").ascending()
        );

        LocalDateTime from =
                LocalDateTime.of(
                        2026,
                        1,
                        1,
                        0,
                        0
                );

        LocalDateTime to =
                LocalDateTime.of(
                        2026,
                        12,
                        31,
                        23,
                        59
                );

        Page<User> userPage =
                new PageImpl<>(
                        List.of(user),
                        pageable,
                        1
                );

        when(
                userRepository.findAll(
                        any(Specification.class),
                        eq(pageable)
                )
        ).thenReturn(userPage);

        when(userMapper.toResponseDTO(user))
                .thenReturn(response);

        Page<UserResponseDTO> result =
                userService.getUsers(
                        " hospital ",
                        Role.ADMIN,
                        true,
                        from,
                        to,
                        pageable
                );

        assertEquals(1, result.getTotalElements());

        assertSame(
                response,
                result.getContent().getFirst()
        );

        verify(helper).validatePageable(
                eq(pageable),
                anySet()
        );

        verify(helper).validateRole(Role.ADMIN);

        verify(helper).validateDateRange(from, to);

        verify(userRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }
}
