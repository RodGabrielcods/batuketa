package com.ifsp.Leel.Repository;

import com.ifsp.Leel.Model.Pedido;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public class PedidoRepository {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void salvar(Pedido pedido) {
        if (pedido.getId() == null) {
            em.persist(pedido);
        } else {
            em.merge(pedido);
        }
    }

    @Transactional
    public Pedido atualizar(Pedido pedido) {
        return em.merge(pedido);
    }

    @Transactional
    public void deletar(Long id) {
        Pedido pedido = em.find(Pedido.class, id);
        if (pedido != null) {
            em.remove(pedido);
        }
    }

    @Transactional(readOnly = true)
    public Pedido findById(Long id) {
        return em.find(Pedido.class, id);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPorCliente(Long clienteId) {
        return em
                .createQuery("SELECT p FROM Pedido p WHERE p.cliente.id = :clienteId ORDER BY p.dataPedido DESC",
                        Pedido.class)
                .setParameter("clienteId", clienteId)
                .getResultList();
    }
}