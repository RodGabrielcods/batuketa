package com.ifsp.Leel.Repository;

import com.ifsp.Leel.Model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByCategoria(String categoria);

    List<Produto> findByNomeContainingIgnoreCase(String nome);

    List<Produto> findByValorBetween(double minimo, double maximo);

    List<Produto> findByVendedorId(Long vendedorId);
}