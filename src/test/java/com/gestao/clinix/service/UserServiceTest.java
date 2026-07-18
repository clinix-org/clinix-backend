package com.gestao.clinix.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.gestao.clinix.dto.UserCreateRequest;
import com.gestao.clinix.dto.UserUpdateRequest;
import com.gestao.clinix.entity.Users;
import com.gestao.clinix.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	private static final AuditMetadata ADMIN_METADATA =
			new AuditMetadata("admin@clinix.com", LocalDateTime.of(2026, 6, 24, 21, 30));

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private UserAuditService userAuditService;

	private UserService service;

	@BeforeEach
	void setUp() {
		service = new UserService(userRepository, passwordEncoder, new ModelMapper(), new AccessPolicyService(),
				userAuditService);
	}

	@Test
	void createStoresCreatorTimestampAndDelegatesAudit() {
		UserCreateRequest request = createRequest();

		when(userAuditService.currentMetadata()).thenReturn(ADMIN_METADATA);
		when(passwordEncoder.encode("123456")).thenReturn("encoded");
		when(userRepository.findByUsuario("novo@clinix.com")).thenReturn(Optional.empty());
		when(userRepository.save(any(Users.class))).thenAnswer(invocation -> {
			Users user = invocation.getArgument(0);
			user.setId(10L);
			return user;
		});

		var response = service.create(request);

		assertThat(response.getCreatedBy()).isEqualTo("admin@clinix.com");
		assertThat(response.getCreatedAt()).isEqualTo(ADMIN_METADATA.performedAt());
		verify(userAuditService).registerCreated(any(Users.class), any(AuditMetadata.class));
	}

	@Test
	void updateStoresEditorTimestampAndDelegatesAudit() {
		Users existing = user(20L, "usuario@clinix.com", "USER", true);
		UserUpdateRequest request = updateRequest("usuario@clinix.com", "ADMIN", true);

		when(userAuditService.currentMetadata()).thenReturn(ADMIN_METADATA);
		when(userRepository.findById(20L)).thenReturn(Optional.of(existing));
		when(userRepository.save(existing)).thenReturn(existing);

		var response = service.update(20L, request);

		assertThat(response.getUpdatedBy()).isEqualTo("admin@clinix.com");
		assertThat(response.getUpdatedAt()).isEqualTo(ADMIN_METADATA.performedAt());
		verify(userAuditService).registerUpdated(existing, ADMIN_METADATA);
	}

	@Test
	void deleteDelegatesAuditWithResponsibleUser() {
		Users existing = user(30L, "remover@clinix.com", "USER", true);

		when(userAuditService.currentMetadata()).thenReturn(ADMIN_METADATA);
		when(userRepository.findById(30L)).thenReturn(Optional.of(existing));

		service.delete(30L);

		verify(userRepository).delete(existing);
		verify(userAuditService).registerDeleted(existing, ADMIN_METADATA);
	}

	@Test
	void deleteRejectsAuthenticatedUserSelfDeletion() {
		Users existing = user(40L, "admin@clinix.com", "ADMIN", true);

		when(userAuditService.currentMetadata()).thenReturn(ADMIN_METADATA);
		when(userRepository.findById(40L)).thenReturn(Optional.of(existing));

		assertThatThrownBy(() -> service.delete(40L))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Nao e permitido excluir o proprio usuario autenticado.");
		verifyNoInteractions(passwordEncoder);
	}

	@Test
	void updateRejectsAuthenticatedUserSelfDeactivation() {
		Users existing = user(50L, "admin@clinix.com", "ADMIN", true);
		UserUpdateRequest request = updateRequest("admin@clinix.com", "ADMIN", false);

		when(userAuditService.currentMetadata()).thenReturn(ADMIN_METADATA);
		when(userRepository.findById(50L)).thenReturn(Optional.of(existing));

		assertThatThrownBy(() -> service.update(50L, request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Nao e permitido desativar o proprio usuario autenticado.");
	}

	private UserCreateRequest createRequest() {
		UserCreateRequest request = new UserCreateRequest();
		request.setNome("Novo Usuario");
		request.setUsuario("Novo@Clinix.com");
		request.setSenha("123456");
		request.setRole("ADMIN");
		return request;
	}

	private UserUpdateRequest updateRequest(String usuario, String role, boolean ativo) {
		UserUpdateRequest request = new UserUpdateRequest();
		request.setNome("Usuario Editado");
		request.setUsuario(usuario);
		request.setSenha("");
		request.setRole(role);
		request.setAtivo(ativo);
		return request;
	}

	private Users user(Long id, String usuario, String role, boolean ativo) {
		Users user = new Users();
		user.setId(id);
		user.setNome("Usuario");
		user.setUsuario(usuario);
		user.setSenha("old");
		user.setRole(role);
		user.setAtivo(ativo);
		return user;
	}
}
