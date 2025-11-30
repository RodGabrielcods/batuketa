package com.ifsp.Leel.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.ifsp.Leel.Model.Cliente;
import com.ifsp.Leel.Model.Pessoa;
import com.ifsp.Leel.Model.Vendedor;
import com.ifsp.Leel.Repository.ClienteRepository;
import com.ifsp.Leel.Repository.PessoaRepository;
import com.ifsp.Leel.Repository.VendedorRepository;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VendedorRepository vendedorRepository;

    @Autowired
    private PessoaRepository pessoaRepository; // Novo repositório unificado

    @GetMapping("/loja")
    public String loja() {
        return "page.html";
    }

    @GetMapping("/login")
    public String mostrarLogin(HttpSession session) {
        if (session.getAttribute("usuarioLogado") != null) {
            if ("CLIENTE".equals(session.getAttribute("tipoUsuario"))) {
                return "redirect:/loja";
            } else {
                return "redirect:/meus-produtos";
            }
        }
        return "cadastroLogin.html";
    }

    @PostMapping("/login")
    public String processarLogin(@RequestParam String nome, @RequestParam String senha,
            Model model, HttpSession session) {

        // SOLUÇÃO DA REDUNDÂNCIA: Busca uma única vez na tabela Pessoa
        Pessoa pessoa = pessoaRepository.findByNomeAndSenha(nome, senha);

        if (pessoa != null) {
            // Verifica qual o tipo real do usuário (Polimorfismo)
            if (pessoa instanceof Cliente) {
                session.setAttribute("usuarioLogado", pessoa);
                session.setAttribute("tipoUsuario", "CLIENTE");
                session.setAttribute("usuarioNome", pessoa.getNome());
                return "redirect:/loja";
            } else if (pessoa instanceof Vendedor) {
                session.setAttribute("usuarioLogado", pessoa);
                session.setAttribute("tipoUsuario", "VENDEDOR");
                session.setAttribute("usuarioNome", pessoa.getNome());
                return "redirect:/meus-produtos";
            }
        }

        model.addAttribute("erro", "Usuário ou senha inválidos");
        return "cadastroLogin";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @PostMapping("/cadastrar")
    public String cadastrar(Cliente cliente, Model model) {
        try {
            clienteRepository.save(cliente);
        } catch (Exception e) {
            model.addAttribute("erroCadastro", "Erro ao cadastrar cliente.");
            return "cadastroLogin";
        }
        return "redirect:/login";
    }

    @GetMapping("/meu-perfil")
    public String mostrarMeuPerfil(HttpSession session, Model model) {
        Object usuarioLogado = session.getAttribute("usuarioLogado");
        String tipoUsuario = (String) session.getAttribute("tipoUsuario");

        if (usuarioLogado == null)
            return "redirect:/login";

        // Usamos findById com .orElse(null) pois agora é JpaRepository
        if ("CLIENTE".equals(tipoUsuario)) {
            Cliente c = (Cliente) usuarioLogado;
            model.addAttribute("cliente", clienteRepository.findById(c.getId()).orElse(null));
            model.addAttribute("tipoUsuario", "CLIENTE");
            return "painelUsuario.html";
        }

        if ("VENDEDOR".equals(tipoUsuario)) {
            Vendedor v = (Vendedor) usuarioLogado;
            model.addAttribute("vendedor", vendedorRepository.findById(v.getId()).orElse(null));
            model.addAttribute("tipoUsuario", "VENDEDOR");
            return "painelUsuario.html";
        }

        return "redirect:/login";
    }

    @PostMapping("/meu-perfil/cliente")
    public String atualizarPerfilCliente(Cliente formData, HttpSession session, RedirectAttributes redirectAttributes) {
        Cliente clienteLogado = (Cliente) session.getAttribute("usuarioLogado");
        if (clienteLogado == null || !"CLIENTE".equals(session.getAttribute("tipoUsuario")))
            return "redirect:/login";

        Cliente clienteDB = clienteRepository.findById(clienteLogado.getId()).orElse(null);
        if (clienteDB == null)
            return "redirect:/login";

        clienteDB.setNome(formData.getNome());
        clienteDB.setEmail(formData.getEmail());
        clienteDB.setCpf(formData.getCpf());

        if (formData.getSenha() != null && !formData.getSenha().isEmpty()) {
            clienteDB.setSenha(formData.getSenha());
        }

        try {
            clienteRepository.save(clienteDB); // 'save' substitui 'update'
            session.setAttribute("usuarioLogado", clienteDB);
            redirectAttributes.addFlashAttribute("sucesso", "Perfil atualizado!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("erro", "Falha ao atualizar o perfil.");
        }
        return "redirect:/meu-perfil";
    }

    @GetMapping("/minha-conta/deletar")
    public String deletarMinhaConta(HttpSession session, RedirectAttributes redirectAttributes) {
        Object usuarioLogado = session.getAttribute("usuarioLogado");
        String tipoUsuario = (String) session.getAttribute("tipoUsuario");

        if (usuarioLogado == null)
            return "redirect:/login";

        try {
            if ("CLIENTE".equals(tipoUsuario)) {
                // 'deleteById' substitui 'delete'
                clienteRepository.deleteById(((Cliente) usuarioLogado).getId());
            } else if ("VENDEDOR".equals(tipoUsuario)) {
                vendedorRepository.deleteById(((Vendedor) usuarioLogado).getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("erro", "Não foi possível deletar sua conta.");
            return "redirect:/meu-perfil";
        }

        session.invalidate();
        redirectAttributes.addFlashAttribute("sucesso", "Conta deletada com sucesso.");
        return "redirect:/login";
    }
}   