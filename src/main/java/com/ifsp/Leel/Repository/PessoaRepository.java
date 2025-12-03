package com.ifsp.Leel.Repository;

import com.ifsp.Leel.Model.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
    Pessoa findByNomeAndSenha(String nome, String senha);
}