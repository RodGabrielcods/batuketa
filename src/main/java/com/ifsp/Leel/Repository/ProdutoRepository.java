package com.ifsp.Leel.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.ifsp.Leel.Model.Produto;
import java.util.List;

@Repository
public class ProdutoRepository {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void salvar(Produto produto) {
        em.persist(produto);
    }

    @Transactional
    public Produto atualizar(Produto produto) {
        return em.merge(produto);
    }

    @Transactional
    public void deletar(Long id) {
        em.createQuery("DELETE FROM Produto p WHERE p.id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }

    @Transactional(readOnly = true)
    public Produto findById(Long id) {
        return em.find(Produto.class, id);
    }

    @Transactional(readOnly = true)
    public List<Produto> list() {
        return em.createQuery("SELECT p FROM Produto p", Produto.class)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public List<Produto> listByCat(String categoria) {
        return em.createQuery(
                "SELECT p FROM Produto p WHERE p.categoria = :categoria", Produto.class)
                .setParameter("categoria", categoria)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public List<Produto> listByName(String nome) {
        return em.createQuery(
                "SELECT p FROM Produto p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%'))", Produto.class)
                .setParameter("nome", nome)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public List<Produto> listByPrice(double minimo, double maximo) {
        return em.createQuery(
                "SELECT p FROM Produto p WHERE p.valor BETWEEN :minimo AND :maximo", Produto.class)
                .setParameter("minimo", minimo)
                .setParameter("maximo", maximo)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public List<Produto> listByVendedorId(Long vendedorId) {
        return em.createQuery(
                "SELECT p FROM Produto p WHERE p.vendedor.id = :vendedorId", Produto.class)
                .setParameter("vendedorId", vendedorId)
                .getResultList();
    }
}