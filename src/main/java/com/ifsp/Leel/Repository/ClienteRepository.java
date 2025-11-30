package com.ifsp.Leel.Repository;

import com.ifsp.Leel.Model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    // Método mantido para compatibilidade, mas o ideal é usar o PessoaRepository
    // para login
    Cliente findByNomeAndSenha(String nome, String senha);
}