package com.acme.filiale.repository;

import com.acme.filiale.entity.Filiale;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface FilialenDBRepository extends JpaRepository<Filiale, Long>, JpaSpecificationExecutor<Filiale> {
    @EntityGraph(attributePaths = {"adresse"})
    @Override
    List<Filiale> findAll();

    @EntityGraph(attributePaths = {"adresse"})
    @Override
    Optional<Filiale> findById(Long id);

    @Query("""
        SELECT f
        FROM   Filiale f
        WHERE  lower(f.email) LIKE concat(lower(:email), '%')
        """)
    @EntityGraph(attributePaths = {"adresse"})
    Optional<Filiale> findByEmail(String email);
    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    boolean existsByEmail(String email);

    @Query("""
        SELECT   f
        FROM     Filiale f
        WHERE    lower(f.name) LIKE concat('%', lower(:name), '%')
        ORDER BY f.id
        """)
    @EntityGraph(attributePaths = {"adresse", "interessen"})
    Collection<Filiale> findByNachname(CharSequence name);

    @Query("""
        SELECT DISTINCT f.name
        FROM     Filiale f
        WHERE    lower(f.name) LIKE concat(lower(:prefix), '%')
        ORDER BY f.name
        """)
    Collection<String> findNachnamenByPrefix(String prefix);

}
