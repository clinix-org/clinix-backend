package com.gestao.clinix.service;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.gestao.clinix.entity.Users;
import com.gestao.clinix.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Users user = userRepository.findByUsuario(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado: " + username));

		if (!user.isAtivo()) {
			throw new DisabledException("Conta inativa ou bloqueada");
		}

		return User.builder()
				.username(user.getUsuario())
				.password(user.getSenha())
				.roles(user.getRole())
				.build();
	}
}
