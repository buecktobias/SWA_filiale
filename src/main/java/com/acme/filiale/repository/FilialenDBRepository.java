package com.acme.filiale.repository;

import com.acme.filiale.entity.Filiale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface FilialenDBRepository extends JpaRepository<Filiale, Long> {
}
