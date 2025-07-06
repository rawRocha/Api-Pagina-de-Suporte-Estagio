package com.projeto.suporteAoCliente.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import com.projeto.suporteAoCliente.enums.Status;
import com.projeto.suporteAoCliente.model.PendingUser;
//import com.projeto.suporteAoCliente.model.User;

@Repository
public interface PendingUserRepository extends JpaRepository<PendingUser, Long> {
    Optional<PendingUser> findByEmail(String email);

    List<PendingUser> findAllByStatusIn(List<Status> statuses);

    boolean existsByEmail(String email);
}
