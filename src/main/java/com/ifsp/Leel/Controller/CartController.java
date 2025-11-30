package com.ifsp.Leel.Controller;

import com.ifsp.Leel.Model.Produto;
import com.ifsp.Leel.Repository.ProdutoRepository;
import com.ifsp.Leel.Service.CartService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.*;

@Controller
@RequestMapping("/carrinho")
public class CartController {

    private static final String CART_COOKIE = "cartId";
    private static final String REMEMBER_COOKIE = "rememberCart";

    @Autowired
    private CartService cartService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @GetMapping
    public String viewCart(
            @CookieValue(value = CART_COOKIE, required = false) String cartId,
            HttpServletRequest req,
            HttpServletResponse resp,
            Model model) {

        boolean persistent = "true".equals(getCookie(req, REMEMBER_COOKIE));

        String id = (cartId == null || cartId.isBlank())
                ? issueCartId(resp, persistent)
                : touchCartCookie(resp, cartId, persistent);

        Map<String, Integer> cartItemsRaw = cartService.getItems(id);

        List<Map<String, Object>> itensCarrinho = new ArrayList<>();
        double valorTotalCarrinho = 0.0;

        if (cartItemsRaw != null && !cartItemsRaw.isEmpty()) {
            for (Map.Entry<String, Integer> entry : cartItemsRaw.entrySet()) {
                try {
                    Long produtoId = Long.parseLong(entry.getKey());
                    Produto produto = produtoRepository.findById(produtoId);

                    if (produto != null) {
                        int quantidade = entry.getValue();
                        double subtotal = produto.getValor() * quantidade;

                        Map<String, Object> item = new HashMap<>();
                        item.put("produto", produto);
                        item.put("quantidade", quantidade);
                        item.put("subtotal", subtotal);

                        itensCarrinho.add(item);
                        valorTotalCarrinho += subtotal;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }

        model.addAttribute("itens", itensCarrinho);
        model.addAttribute("cartId", id);
        model.addAttribute("persistent", persistent);
        model.addAttribute("total", valorTotalCarrinho);

        return "carrinho";
    }

    @PostMapping("/add")
    public String addItem(@RequestParam String sku,
            HttpServletRequest req, HttpServletResponse resp) {
        boolean persistent = "true".equals(getCookie(req, REMEMBER_COOKIE));
        String id = getCookie(req, CART_COOKIE);
        if (id == null || id.isBlank())
            id = issueCartId(resp, persistent);
        else
            touchCartCookie(resp, id, persistent);

        cartService.addItem(id, sku, 1);
        return "redirect:/carrinho";
    }

    @PostMapping("/remove")
    public String removeItem(@RequestParam String sku,
            HttpServletRequest req, HttpServletResponse resp) {
        String id = getCookie(req, CART_COOKIE);
        if (id != null) {
            cartService.removeItem(id, sku);
        }
        return "redirect:/carrinho";
    }

    @PostMapping("/remember")
    public String remember(@RequestParam(name = "on", defaultValue = "false") boolean on,
            HttpServletRequest req, HttpServletResponse resp) {
        if (on) {
            ResponseCookie rc = ResponseCookie.from(REMEMBER_COOKIE, "true")
                    .path("/").sameSite("Lax").maxAge(Duration.ofDays(7)).build();
            resp.addHeader(HttpHeaders.SET_COOKIE, rc.toString());
        } else {
            ResponseCookie rc = ResponseCookie.from(REMEMBER_COOKIE, "")
                    .path("/").maxAge(Duration.ZERO).build();
            resp.addHeader(HttpHeaders.SET_COOKIE, rc.toString());
        }

        String id = getCookie(req, CART_COOKIE);
        if (id != null && !id.isBlank())
            touchCartCookie(resp, id, on);
        return "redirect:/carrinho";
    }

    @PostMapping("/limpar")
    public String clearCart(HttpServletRequest req, HttpServletResponse resp) {
        String id = getCookie(req, CART_COOKIE);
        if (id != null)
            cartService.clearCart(id);

        ResponseCookie delCart = ResponseCookie.from(CART_COOKIE, "")
                .path("/").maxAge(Duration.ZERO).build();
        ResponseCookie delRemember = ResponseCookie.from(REMEMBER_COOKIE, "")
                .path("/").maxAge(Duration.ZERO).build();
        resp.addHeader(HttpHeaders.SET_COOKIE, delCart.toString());
        resp.addHeader(HttpHeaders.SET_COOKIE, delRemember.toString());
        return "redirect:/carrinho";
    }

    private String issueCartId(HttpServletResponse resp, boolean persistent) {
        String id = UUID.randomUUID().toString();
        return touchCartCookie(resp, id, persistent);
    }

    private String touchCartCookie(HttpServletResponse resp, String id, boolean persistent) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(CART_COOKIE, id)
                .path("/")
                .sameSite("Lax")
                .httpOnly(true);
        if (persistent)
            b.maxAge(Duration.ofDays(7));
        ResponseCookie c = b.build();
        resp.addHeader(HttpHeaders.SET_COOKIE, c.toString());
        return id;
    }

    private static String getCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null)
            return null;
        for (Cookie c : req.getCookies()) {
            if (name.equals(c.getName()))
                return c.getValue();
        }
        return null;
    }
}