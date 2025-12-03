package com.ifsp.Leel.Controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.ifsp.Leel.Model.Produto;
import com.ifsp.Leel.Repository.ProdutoRepository;

@Controller
public class HomeController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @GetMapping("/")
    public String index(Model model) {
        return "page.html";
    }

    @GetMapping("/produtos")
    public String listarProdutosPublico(
            @RequestParam(name = "categoria", required = false) String categoria,
            Model model) {

        List<Produto> produtos;

        if (categoria != null && !categoria.isEmpty()) {
            produtos = produtoRepository.findByCategoria(categoria);
            model.addAttribute("categoriaAtiva", categoria);
        } else {
            produtos = produtoRepository.findAll();
        }

        model.addAttribute("produtos", produtos);

        return "listaProdutos.html";
    }
}