package com.pi.projeto_quarto_semestre.controller;

import com.pi.projeto_quarto_semestre.model.Pedido;
import com.pi.projeto_quarto_semestre.repository.PedidoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class PedidoBackofficeController {

    @Autowired
    private PedidoRepository pedidoRepository;

    // LISTA DE PEDIDOS PARA ESTOQUISTA
    @GetMapping("/listaPedidos")
    public String listarPedidos(HttpSession session, Model model) {

        String grupoUsuario = (String) session.getAttribute("grupoUsuario");
        if (grupoUsuario == null) {
            return "redirect:/login";
        }

        // se quiser travar só pra ESTOQUISTA, descomente:
        // if (!grupoUsuario.equals("ESTOQUISTA")) {
        //     return "redirect:/paginabko";
        // }

        // usa o JpaRepository: findAll(Sort)
        List<Pedido> pedidos = pedidoRepository.findAll(
                Sort.by(Sort.Direction.DESC, "dataPedido")
        );

        model.addAttribute("pedidos", pedidos);

        return "listaPedidos"; // templates/listaPedidos.html
    }

    // EDITAR PEDIDO (a rota já existe, depois você define o que editar)
    @GetMapping("/listaPedidos/editar/{pedidoId}")
    public String editarPedido(@PathVariable Long pedidoId,
                               HttpSession session,
                               Model model) {

        String grupoUsuario = (String) session.getAttribute("grupoUsuario");
        if (grupoUsuario == null) {
            return "redirect:/login";
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElse(null);

        if (pedido == null) {
            return "redirect:/listaPedidos";
        }

        model.addAttribute("pedido", pedido);

        return "editarPedido"; // depois você cria esse HTML
    }
}
