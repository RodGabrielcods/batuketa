package com.ifsp.Leel.Repository;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.ifsp.Leel.Model.Vendedor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class VendedorRepository {
    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void save(Vendedor vendedor) {
        em.persist(vendedor);
    }

    @Transactional
    public Vendedor update(Vendedor vendedor) {
        return em.merge(vendedor);
    }

    @Transactional
    public void delete(Long id) {
        em.createQuery(
                "DELETE FROM Vendedor v WHERE v.id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }

    @Transactional
    public Vendedor findById(Long id) {
        try {
            return em.find(Vendedor.class, id);
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public List<Vendedor> list() {
        try {
            return em.createQuery("SELECT v FROM Vendedor v", Vendedor.class)
                    .getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public Vendedor findByNomeAndSenha(String nome, String senha) {
        try {
            return em.createQuery(
                    "SELECT v FROM Vendedor v WHERE v.nome = :nome AND v.senha = :senha", Vendedor.class)
                    .setParameter("nome", nome)
                    .setParameter("senha", senha)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}