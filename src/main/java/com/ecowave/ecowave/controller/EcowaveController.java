package com.ecowave.ecowave.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ecowave.ecowave.model.Localizacao;
import com.ecowave.ecowave.model.Amigos;
import com.ecowave.ecowave.service.AmigosService;
import com.ecowave.ecowave.service.LocalizacaoService;
import com.ecowave.ecowave.model.Usuario;
import com.ecowave.ecowave.service.UsuarioService;
import com.ecowave.ecowave.service.ItensRecicladosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.ecowave.ecowave.model.ItensReciclados;

import javax.validation.Valid;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@Validated
@RequestMapping("/api")
public class EcowaveController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ItensRecicladosService itensRecicladosService;

    @Autowired
    private AmigosService amigosService;

    @Autowired
    private LocalizacaoService localizacaoService;

    @PostMapping("/registrar")
    public ResponseEntity<String> registrarUsuario(@Valid @RequestBody Usuario usuario) {
        usuarioService.registrarUsuario(usuario);
        return new ResponseEntity<>("Usuário registrado com sucesso.", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String nomeUsuario, @RequestParam String senha) {
        boolean loginSucesso = usuarioService.verificarCredenciais(nomeUsuario, senha);
        if (loginSucesso) {
            return new ResponseEntity<>("Login bem-sucedido.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Credenciais inválidas.", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/item")
    public ResponseEntity<Page<ItensReciclados>> getAllItensReciclados(@RequestParam(required = false) String tipoItem,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ItensReciclados> itensRecicladosPage;

            if (tipoItem == null)
                itensRecicladosPage = itensRecicladosService.findAllPaginado(page, size);
            else
                itensRecicladosPage = itensRecicladosService.findByTipoItemContainingPaginado(tipoItem, page, size);

            if (itensRecicladosPage.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(itensRecicladosPage, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/item/total")
    public ResponseEntity<Long> getTotalItens() {
        try {
            long totalItens = itensRecicladosService.findTotalQuantidadeItens();
            return new ResponseEntity<>(totalItens, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/usuarios")
    public ResponseEntity<Page<Usuario>> obterTodosUsuariosPaginado(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Usuario> usuariosPage = usuarioService.obterTodosUsuariosPaginado(PageRequest.of(page, size));
            if (usuariosPage.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(usuariosPage, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/usuario/{id}/item")
    public ResponseEntity<Page<ItensReciclados>> getItensByUsuarioId(@PathVariable("id") long id,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ItensReciclados> itensRecicladosPage = itensRecicladosService.findByUsuarioIdPaginado(id, page, size);
            if (itensRecicladosPage.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(itensRecicladosPage, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/usuario/{id}/item")
    public ResponseEntity<String> createItemReciclado(@PathVariable long id, @RequestBody ItensReciclados itensReciclados) {
        try {
            Usuario usuario = usuarioService.findByIdUsuario(id);
            if(usuario == null) {
                return new ResponseEntity<>("Usuário não encontrado.", HttpStatus.NOT_FOUND);
            }
            itensReciclados.setUsuario(usuario);
            itensRecicladosService.save(itensReciclados);
            return new ResponseEntity<>("Item Reciclado foi adicionado com sucesso.", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao adicionar item reciclado.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/usuario/{id}/item/total")
    public ResponseEntity<Long> getTotalItensByUsuarioId(@PathVariable("id") long id) {
        try {
            long totalItens = itensRecicladosService.findTotalQuantidadeItensByUsuarioId(id);
            return new ResponseEntity<>(totalItens, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/usuario/{id}/item/{itemId}")
    public ResponseEntity<String> updateItemReciclado(@PathVariable long id, @PathVariable long itemId, @RequestBody ItensReciclados updatedItem) {
        try {
            ItensReciclados existingItem = itensRecicladosService.findById(itemId);
            if (existingItem == null) {
                return new ResponseEntity<>("Item Reciclado não encontrado.", HttpStatus.NOT_FOUND);
            }

            if (existingItem.getUsuario().getIdUsuario() != id) {
                return new ResponseEntity<>("Não autorizado a atualizar este item.", HttpStatus.UNAUTHORIZED);
            }

            existingItem.setTipoItem(updatedItem.getTipoItem());
            existingItem.setLocalizacaoItem(updatedItem.getLocalizacaoItem());
            existingItem.setQuantidadeItem(updatedItem.getQuantidadeItem());

            itensRecicladosService.save(existingItem);

            return new ResponseEntity<>("Item Reciclado foi atualizado com sucesso.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao atualizar o item reciclado.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/usuario/{id}/item/{itemId}")
    public ResponseEntity<ItensReciclados> getItemByUsuarioIdAndItemId(@PathVariable("id") long id, @PathVariable("itemId") long itemId) {
        try {
            ItensReciclados item = itensRecicladosService.findById(itemId);

            if (item == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            if (item.getUsuario().getIdUsuario() != id) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            return new ResponseEntity<>(item, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/usuario/{id}/item/{itemId}")
    public ResponseEntity<String> deleteItemByUsuarioIdAndItemId(@PathVariable("id") long id, @PathVariable("itemId") long itemId) {
        try {
            itensRecicladosService.deleteById(itemId);
            return new ResponseEntity<>("Item removido com sucesso.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao remover o item.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/usuario/{id}/amigos")
    public ResponseEntity<String> adicionarAmigo(@PathVariable("id") Long idUsuario, @RequestBody Map<String, Long> payload) {
        try {
            Long idAmigo = payload.get("idAmigo");
            if (idAmigo == null) {
                return new ResponseEntity<>("idAmigo é necessário", HttpStatus.BAD_REQUEST);
            }
            if (idUsuario.equals(idAmigo)) {
                return new ResponseEntity<>("Um usuário não pode adicionar a si mesmo como amigo.", HttpStatus.BAD_REQUEST);
            }
            amigosService.adicionarAmigo(idUsuario, idAmigo);
            return new ResponseEntity<>("Amigo adicionado com sucesso.", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao adicionar amigo.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/usuario/{id}/amigos")
    public ResponseEntity<List<Amigos>> obterAmigos(@PathVariable("id") Long idUsuario) {
        try {
            List<Amigos> amigos = amigosService.obterAmigos(idUsuario);
            if (amigos.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(amigos, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @DeleteMapping("/usuario/{id}/amigos/{idAmigo}")
    public ResponseEntity<String> removerAmigo(@PathVariable("id") Long idUsuario, @PathVariable("idAmigo") Long idAmigo) {
        try {
            amigosService.removerAmigo(idUsuario, idAmigo);
            return new ResponseEntity<>("Amigo removido com sucesso.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao remover amigo.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/localizacoes")
    public ResponseEntity<Localizacao> criarLocalizacao(@RequestBody Localizacao localizacao) {
        try {
            Localizacao novaLocalizacao = localizacaoService.salvarLocalizacao(localizacao);
            return new ResponseEntity<>(novaLocalizacao, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/localizacoes")
    public ResponseEntity<Page<Localizacao>> obterTodasLocalizacoesPaginado(@RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Localizacao> localizacoesPage = localizacaoService.obterTodasLocalizacoesPaginado(PageRequest.of(page, size));
            if (localizacoesPage.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(localizacoesPage, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/localizacoes/{id}")
    public ResponseEntity<Localizacao> obterLocalizacaoPorId(@PathVariable("id") Long id) {
        Optional<Localizacao> localizacao = localizacaoService.obterLocalizacaoPorId(id);
        return localizacao.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}



