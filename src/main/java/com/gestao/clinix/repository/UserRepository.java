package com.gestao.clinix.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.gestao.clinix.entity.Users;


public interface UserRepository extends JpaRepository<Users, Long> {
	
	Optional<Users> findByUsuario(String usuario);

	@Query("select u from Users u where lower(u.nome) like lower(concat('%', :term, '%')) or lower(u.usuario) like lower(concat('%', :term, '%'))")
	List<Users> searchByTerm(@Param("term") String term);

}
