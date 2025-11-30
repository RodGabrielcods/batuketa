package com.ifsp.Leel.Controller;

import com.ifsp.Leel.Model.Produto;
import com.ifsp.Leel.Model.Vendedor;
import com.ifsp.Leel.Repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Controller
public class ProdutoController {

    @Autowired
    private ProdutoRepository produtoRepository;

    private static final String UPLOAD_DIRECTORY = "src/main/resources/static/img/uploads/";

    private Vendedor getVendedorLogado(HttpSession session) {
        Object tipo = session.getAttribute("tipoUsuario");
        if (tipo != null && tipo.equals("VENDEDOR")) {
            return (Vendedor) session.getAttribute("usuarioLogado");
        }
        return null;
    }

    @GetMapping("/meus-produtos")
    public String mostrarMeusProdutos(Model model, HttpSession session) {
        Vendedor vendedor = getVendedorLogado(session);
        if (vendedor == null) {
            return "redirect:/login";
        }

        List<Produto> produtos = produtoRepository.listByVendedorId((long) vendedor.getId());
        model.addAttribute("produtos", produtos);
        model.addAttribute("nomeVendedor", vendedor.getNome());
        return "meusProdutos.html";
    }

    @GetMapping("/cadastrarProduto")
    public String showCadastroProdutoForm(Model model, HttpSession session) {
        if (getVendedorLogado(session) == null) {
            return "redirect:/login";
        }
        model.addAttribute("produto", new Produto());
        return "cadastroProduto.html";
    }

    @PostMapping("/cadastrarProduto")
    public String cadastrarProduto(
            Produto produto,
            @RequestParam("imagemFile") MultipartFile imagemFile,
            Model model, HttpSession session) {

        Vendedor vendedor = getVendedorLogado(session);
        if (vendedor == null) {
            return "redirect:/login";
        }

        if (imagemFile != null && !imagemFile.isEmpty()) {
            try {
                String nomeArquivoImagem = salvarImagem(imagemFile);
                produto.setImagem(nomeArquivoImagem);
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("erro", "Falha ao salvar a imagem.");
                model.addAttribute("produto", produto);
                return "cadastroProduto";
            }
        } else {
            produto.setImagem("/img/default.png");
        }

        try {
            produto.setVendedor(vendedor);
            produtoRepository.salvar(produto);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("erro", "Falha ao salvar o produto no banco de dados.");
            model.addAttribute("produto", produto);
            return "cadastroProduto";
        }

        return "redirect:/meus-produtos";
    }

    @GetMapping("/produto/editar/{id}")
    public String mostrarFormularioEdicao(@PathVariable("id") Long id, Model model, HttpSession session) {
        Vendedor vendedor = getVendedorLogado(session);
        Produto produto = produtoRepository.findById(id);

        if (vendedor == null || produto == null || produto.getVendedor().getId() != vendedor.getId()) {
            return "redirect:/meus-produtos";
        }

        model.addAttribute("produto", produto);
        return "editarProduto.html";
    }

    @PostMapping("/produto/editar")
    public String atualizarProduto(
            Produto produto,
            @RequestParam("imagemFile") MultipartFile imagemFile,
            Model model, HttpSession session, RedirectAttributes redirectAttributes) {

        Vendedor vendedor = getVendedorLogado(session);
        Produto produtoExistente = produtoRepository.findById((long) produto.getId());

        if (vendedor == null || produtoExistente == null
                || produtoExistente.getVendedor().getId() != vendedor.getId()) {
            redirectAttributes.addFlashAttribute("erro", "Você não pode editar este produto.");
            return "redirect:/meus-produtos";
        }

        produto.setVendedor(vendedor);

        if (imagemFile != null && !imagemFile.isEmpty()) {
            try {
                produto.setImagem(salvarImagem(imagemFile));
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("erro", "Falha ao salvar a imagem.");
                model.addAttribute("produto", produto);
                return "editarProduto";
            }
        } else {
            produto.setImagem(produtoExistente.getImagem());
        }

        try {
            produtoRepository.atualizar(produto);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("erro", "Falha ao atualizar o produto.");
            model.addAttribute("produto", produto);
            return "editarProduto";
        }

        return "redirect:/meus-produtos";
    }

    @GetMapping("/produto/deletar/{id}")
    public String deletarProduto(@PathVariable("id") Long id, HttpSession session,
            RedirectAttributes redirectAttributes) {
        Vendedor vendedor = getVendedorLogado(session);
        Produto produto = produtoRepository.findById(id);

        if (vendedor == null || produto == null || produto.getVendedor().getId() != vendedor.getId()) {
            redirectAttributes.addFlashAttribute("erro", "Você não pode deletar este produto.");
            return "redirect:/meus-produtos";
        }

        try {
            produtoRepository.deletar(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/meus-produtos";
    }

    private String salvarImagem(MultipartFile imagemFile) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIRECTORY);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String extensao = "";
        String nomeOriginal = imagemFile.getOriginalFilename();
        if (nomeOriginal != null && nomeOriginal.contains(".")) {
            extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
        }
        String nomeArquivoImagem = UUID.randomUUID().toString() + extensao;

        Path caminhoArquivo = uploadPath.resolve(nomeArquivoImagem);
        Files.copy(imagemFile.getInputStream(), caminhoArquivo, StandardCopyOption.REPLACE_EXISTING);

        return "/img/uploads/" + nomeArquivoImagem;
    }

    @GetMapping("/mostrarproduto")
    public String mostrarproduto() {
        return "listaProdutos.html";
    }
}