package com.pi.projeto_quarto_semestre.controller;

import com.pi.projeto_quarto_semestre.model.Pedido;
import com.pi.projeto_quarto_semestre.repository.PedidoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
public class PedidoBackofficeController {

    @Autowired
    private PedidoRepository pedidoRepository;

    // Lista de pedidos
    @GetMapping("/listaPedidos")
    public String listarPedidos(HttpSession session, Model model) {

        String grupoUsuario = (String) session.getAttribute("grupoUsuario");
        if (grupoUsuario == null) {
            return "redirect:/login";
        }


        List<Pedido> pedidos = pedidoRepository.findAll(
                Sort.by(Sort.Direction.DESC, "dataPedido")
        );

        model.addAttribute("pedidos", pedidos);
        return "listaPedidos";
    }



    // ====== EDITAR PEDIDO (POST) - SALVAR STATUS ======
    @PostMapping("/listaPedidos/editar/{pedidoId}")
    public String salvarStatusPedido(@PathVariable Long pedidoId,
                                     @RequestParam("status") String novoStatus,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {

        String grupoUsuario = (String) session.getAttribute("grupoUsuario");
        if (grupoUsuario == null) {
            return "redirect:/login";
        }

        List<String> statusPermitidos = Arrays.asList(
                "AGUARDANDO PAGAMENTO",
                "PAGAMENTO REJEITADO",
                "PAGAMENTO COM SUCESSO",
                "AGUARDANDO RETIRADA",
                "EM TRANSITO",
                "ENTREGUE"
        );

        if (!statusPermitidos.contains(novoStatus)) {
            redirectAttributes.addFlashAttribute("erroStatus", "Status inválido.");
            return "redirect:/listaPedidos/editar/" + pedidoId;
        }

        Pedido pedido = pedidoRepository.findById(pedidoId).orElse(null);
        if (pedido == null) {
            redirectAttributes.addFlashAttribute("erroStatus", "Pedido não encontrado.");
            return "redirect:/listaPedidos";
        }

        // >>> AQUI é onde o status é alterado e gravado no banco <<<
        pedido.setStatus(novoStatus);
        pedidoRepository.save(pedido); // persiste no banco

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Status do pedido atualizado com sucesso!");
        return "redirect:/listaPedidos";
    }
}
