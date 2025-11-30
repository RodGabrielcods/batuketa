package com.ifsp.Leel.Repository;

import com.ifsp.Leel.Model.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendedorRepository extends JpaRepository<Vendedor, Long> {
    Vendedor findByNomeAndSenha(String nome, String senha);
}