package com.ifsp.Leel.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.ifsp.Leel.Model.Vendedor;
import com.ifsp.Leel.Repository.VendedorRepository;
import jakarta.servlet.http.HttpSession;

@Controller
public class VendedorController {

    @Autowired
    private VendedorRepository vendedorRepository;

    @GetMapping("/cadastrarVendedor")
    public String mostrarFormularioCadastroVendedor(Model model) {
        model.addAttribute("vendedor", new Vendedor());

        return "cadastroVendedor.html";
    }

    @PostMapping("/cadastrarVendedor")
    public String cadastrarVendedor(Vendedor vendedor, Model model, RedirectAttributes redirectAttributes) {
        try {
            vendedorRepository.save(vendedor);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("erro", "Falha ao cadastrar o vendedor.");
            model.addAttribute("vendedor", vendedor);
            return "cadastroVendedor";
        }
        redirectAttributes.addFlashAttribute("sucesso", "Cadastro realizado! Faça o login.");

        return "redirect:/login";
    }

    @PostMapping("/meu-perfil/vendedor")
    public String atualizarPerfilVendedor(Vendedor formData, HttpSession session,
            RedirectAttributes redirectAttributes) {

        Vendedor vendedorLogado = (Vendedor) session.getAttribute("usuarioLogado");
        if (vendedorLogado == null || !"VENDEDOR".equals(session.getAttribute("tipoUsuario"))) {

            return "redirect:/login";
        }

        Vendedor vendedorDB = vendedorRepository.findById((long) vendedorLogado.getId());
        if (vendedorDB == null) {

            return "redirect:/login";
        }

        vendedorDB.setNome(formData.getNome());
        vendedorDB.setEmail(formData.getEmail());
        vendedorDB.setCnpj(formData.getCnpj());

        if (formData.getSenha() != null && !formData.getSenha().isEmpty()) {
            vendedorDB.setSenha(formData.getSenha());
        }

        try {
            vendedorRepository.update(vendedorDB);
            session.setAttribute("usuarioLogado", vendedorDB);
            redirectAttributes.addFlashAttribute("sucesso", "Perfil atualizado!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("erro", "Falha ao atualizar o perfil.");
        }

        return "redirect:/meu-perfil";
    }
}