package com.ifsp.Leel.Controller;

import com.ifsp.Leel.Model.*;
import com.ifsp.Leel.Repository.PedidoRepository;
import com.ifsp.Leel.Repository.ProdutoRepository;
import com.ifsp.Leel.Service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CartService cartService;

    private Cliente getClienteLogado(HttpSession session) {
        Object usuario = session.getAttribute("usuarioLogado");
        if (usuario instanceof Cliente) {
            return (Cliente) usuario;
        }
        return null;
    }

    @GetMapping("/meus-pedidos")
    public String listarMeusPedidos(Model model, HttpSession session) {
        Cliente cliente = getClienteLogado(session);
        if (cliente == null) {
            return "redirect:/login";
        }

        List<Pedido> pedidos = pedidoRepository.listarPorCliente(cliente.getId());
        model.addAttribute("pedidos", pedidos);
        return "meusPedidos";
    }

    @GetMapping("/{id}")
    public String detalhesPedido(@PathVariable Long id, Model model, HttpSession session) {
        Cliente cliente = getClienteLogado(session);
        if (cliente == null)
            return "redirect:/login";

        Pedido pedido = pedidoRepository.findById(id);

        if (pedido == null || !pedido.getCliente().getId().equals(cliente.getId())) {
            return "redirect:/pedidos/meus-pedidos";
        }

        model.addAttribute("pedido", pedido);
        return "detalhesPedido";
    }

    @PostMapping("/finalizar")
    public String finalizarPedido(
            @CookieValue(value = "cartId", required = false) String cartId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Cliente cliente = getClienteLogado(session);
        if (cliente == null) {
            redirectAttributes.addFlashAttribute("erro", "Você precisa estar logado para finalizar a compra.");
            return "redirect:/login";
        }

        if (cartId == null || !cartService.hasItems(cartId)) {
            redirectAttributes.addFlashAttribute("erro", "Seu carrinho está vazio.");
            return "redirect:/carrinho";
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setStatus("AGUARDANDO PAGAMENTO");

        Map<String, Integer> cartItems = cartService.getItems(cartId);

        for (Map.Entry<String, Integer> entry : cartItems.entrySet()) {
            try {
                Long produtoId = Long.parseLong(entry.getKey());
                int quantidade = entry.getValue();

                Produto produto = produtoRepository.findById(produtoId);

                if (produto != null) {
                    ItemPedido item = new ItemPedido();
                    item.setProduto(produto);
                    item.setQuantidade(quantidade);
                    item.setPrecoUnitario(produto.getValor());

                    pedido.adicionarItem(item);
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }

        if (pedido.getItens().isEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Não foi possível processar os itens do carrinho.");
            return "redirect:/carrinho";
        }

        pedidoRepository.salvar(pedido);
        cartService.clearCart(cartId);

        redirectAttributes.addFlashAttribute("sucesso", "Pedido realizado com sucesso!");
        return "redirect:/pedidos/meus-pedidos";
    }

    @PostMapping("/cancelar/{id}")
    public String cancelarPedido(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Cliente cliente = getClienteLogado(session);
        if (cliente == null)
            return "redirect:/login";

        Pedido pedido = pedidoRepository.findById(id);

        if (pedido != null && pedido.getCliente().getId().equals(cliente.getId())) {
            if ("AGUARDANDO PAGAMENTO".equals(pedido.getStatus()) || "PAGO".equals(pedido.getStatus())) {
                pedido.setStatus("CANCELADO");
                pedidoRepository.atualizar(pedido);
                redirectAttributes.addFlashAttribute("sucesso", "Pedido cancelado.");
            } else {
                redirectAttributes.addFlashAttribute("erro", "Não é possível cancelar este pedido.");
            }
        }
        return "redirect:/pedidos/meus-pedidos";
    }
}