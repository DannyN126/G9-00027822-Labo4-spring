package com.server.app.services;

import com.server.app.dto.user.LoginDto;
import com.server.app.dto.user.UpdatePasswordDto;
import com.server.app.dto.user.UpdateProfileDto;
import com.server.app.dto.user.UserCreateDto;
import com.server.app.dto.user.UserUpdateDto;
import com.server.app.entities.Role;
import com.server.app.entities.User;
import com.server.app.exceptions.ConfictException;
import com.server.app.exceptions.NotFoundException;
import com.server.app.exceptions.UnauthorizedException;
import com.server.app.repositories.RoleRepository;
import com.server.app.repositories.UserRepository;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserService {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  /**
   * Crea usuarios desde el módulo administrativo.
   * Permite recibir un rol explícito.
   */
  @Transactional
  public User create(UserCreateDto dto) {
    uniqueUsername(dto.getUsername(), null);
    uniqueEmail(dto.getEmail(), null);

    User user = new User();
    user.setUsername(dto.getUsername().trim());
    user.setName(dto.getName().trim());
    user.setSurname(dto.getSurname().trim());
    user.setEmail(dto.getEmail().trim().toLowerCase());
    user.setPassword(passwordEncoder.encode(dto.getPassword()));

    if (dto.getRole() != null) {
      Role role = roleRepository.findById(dto.getRole())
              .orElseThrow(() ->
                      new NotFoundException("Rol no encontrado")
              );

      user.setRole(role);
    }

    return userRepository.save(user);
  }

  /**
   * Registro público solicitado por la guía.
   * Asigna el rol ADMIN por defecto.
   */
  @Transactional
  public User signUp(UserCreateDto dto) {
    uniqueUsername(dto.getUsername(), null);
    uniqueEmail(dto.getEmail(), null);

    Role defaultRole = roleRepository.findByNameIgnoreCase("ADMIN")
            .orElseThrow(() ->
                    new NotFoundException(
                            "El rol ADMIN no se encuentra registrado"
                    )
            );

    validateRoleActive(defaultRole);

    User user = new User();
    user.setUsername(dto.getUsername().trim());
    user.setName(dto.getName().trim());
    user.setSurname(dto.getSurname().trim());
    user.setEmail(dto.getEmail().trim().toLowerCase());
    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    user.setBlocked(false);
    user.setRole(defaultRole);

    return userRepository.save(user);
  }

  /**
   * Comprueba las credenciales y devuelve el usuario.
   */
  @Transactional(readOnly = true)
  public User login(LoginDto dto) {
    User user = userRepository
            .findUserByUsernameIgnoreCase(dto.getUsername().trim())
            .orElseThrow(() ->
                    new BadCredentialsException("Credenciales inválidas")
            );

    if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
      throw new BadCredentialsException("Credenciales inválidas");
    }

    validateUserAccess(user);

    return user;
  }

  @Transactional(readOnly = true)
  public Page<User> findAll(int page, int size, String search) {
    return userRepository.findAll(
            PageRequest.of(page, size),
            search == null ? "" : search.trim()
    );
  }

  @Transactional(readOnly = true)
  public User findById(int id) {
    return userRepository.findById(id)
            .orElseThrow(() ->
                    new NotFoundException("Usuario no encontrado")
            );
  }

  @Transactional
  public User updateUser(int userId, UserUpdateDto dto) {
    User user = findById(userId);

    if (user.isBlocked()) {
      throw new ConfictException(
              "The user: " + user.getUsername() + " is locked"
      );
    }

    if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
      uniqueUsername(dto.getUsername(), userId);
      user.setUsername(dto.getUsername().trim());
    }

    if (dto.getName() != null && !dto.getName().isBlank()) {
      user.setName(dto.getName().trim());
    }

    if (dto.getSurname() != null && !dto.getSurname().isBlank()) {
      user.setSurname(dto.getSurname().trim());
    }

    if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
      uniqueEmail(dto.getEmail(), userId);
      user.setEmail(dto.getEmail().trim().toLowerCase());
    }

    if (dto.getBlocked() != null) {
      user.setBlocked(dto.getBlocked());
    }

    if (dto.getRole() != null) {
      Role role = roleRepository.findById(dto.getRole())
              .orElseThrow(() ->
                      new NotFoundException("Rol no encontrado")
              );

      user.setRole(role);
    }

    if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(dto.getPassword()));
    }

    return userRepository.save(user);
  }

  /**
   * Actualiza exclusivamente el perfil del usuario autenticado.
   */
  @Transactional
  public User updateProfile(int userId, UpdateProfileDto dto) {
    User user = findById(userId);

    validateUserAccess(user);

    uniqueUsername(dto.getUsername(), userId);
    uniqueEmail(dto.getEmail(), userId);

    user.setUsername(dto.getUsername().trim());
    user.setName(dto.getName().trim());
    user.setSurname(dto.getSurname().trim());
    user.setEmail(dto.getEmail().trim().toLowerCase());

    return userRepository.save(user);
  }

  /**
   * Cambia la contraseña del usuario autenticado.
   */
  @Transactional
  public User updatePassword(int userId, UpdatePasswordDto dto) {
    User user = findById(userId);

    validateUserAccess(user);

    if (!passwordEncoder.matches(
            dto.getOldpassword(),
            user.getPassword()
    )) {
      throw new BadCredentialsException(
              "La contraseña actual es incorrecta"
      );
    }

    if (!dto.getNewpassword().equals(dto.getConfirmpassword())) {
      throw new ConfictException(
              "La nueva contraseña y su confirmación no coinciden"
      );
    }

    if (passwordEncoder.matches(
            dto.getNewpassword(),
            user.getPassword()
    )) {
      throw new ConfictException(
              "La nueva contraseña debe ser diferente de la actual"
      );
    }

    user.setPassword(
            passwordEncoder.encode(dto.getNewpassword())
    );

    return userRepository.save(user);
  }

  private void validateUserAccess(User user) {
    if (user.isBlocked()) {
      throw new UnauthorizedException(
              "Your account has been blocked"
      );
    }

    if (user.getRole() == null) {
      throw new UnauthorizedException(
              "Your account does not have a role assigned"
      );
    }

    validateRoleActive(user.getRole());
  }

  private void validateRoleActive(Role role) {
    if (!Boolean.TRUE.equals(role.getActive())) {
      throw new UnauthorizedException(
              "Your account role is not active"
      );
    }
  }

  private void uniqueUsername(String username, Integer id) {
    userRepository
            .findUserByUsernameIgnoreCase(username.trim())
            .ifPresent(existing -> {
              if (
                      id == null
                              || existing.getId() != id
              ) {
                throw new ConfictException(
                        "El nombre de usuario ya está en uso"
                );
              }
            });
  }

  private void uniqueEmail(String email, Integer id) {
    userRepository
            .findUserByEmailIgnoreCase(email.trim())
            .ifPresent(existing -> {
              if (
                      id == null
                              || existing.getId() != id
              ) {
                throw new ConfictException(
                        "El correo electrónico ya está en uso"
                );
              }
            });
  }
}