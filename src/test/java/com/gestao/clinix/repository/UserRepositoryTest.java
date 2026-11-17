package com.gestao.clinix.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.gestao.clinix.entity.Users;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void usersEntityUsesExistingTableAndSearchQuery() {
        Users user = new Users();
        user.setNome("Maria Silva");
        user.setUsuario("maria@clinix.com");
        user.setSenha("encoded-password");
        user.setRole("USER");
        repository.saveAndFlush(user);
        entityManager.clear();

        assertThat(repository.findByUsuario("maria@clinix.com")).isPresent();
        assertThat(repository.searchByTerm("SILVA")).extracting(Users::getId).containsExactly(user.getId());
        assertThat(repository.searchByTerm("MARIA@CLINIX.COM")).extracting(Users::getId)
                .containsExactly(user.getId());
        assertThat(entityManager.getEntityManager()
                .createNativeQuery("select username from users where id = :id")
                .setParameter("id", user.getId()).getSingleResult()).isEqualTo("maria@clinix.com");
    }
}
