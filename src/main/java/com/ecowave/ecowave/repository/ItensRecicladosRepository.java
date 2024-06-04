package com.ecowave.ecowave.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecowave.ecowave.model.ItensReciclados;

@Repository
public interface ItensRecicladosRepository extends JpaRepository<ItensReciclados, Long> {
    Page<ItensReciclados> findByUsuario_IdUsuario(long idUsuario, Pageable pageable);
    Page<ItensReciclados> findByTipoItemContaining(String tipoItem, Pageable pageable);
    long countByUsuario_IdUsuario(long idUsuario);
}
