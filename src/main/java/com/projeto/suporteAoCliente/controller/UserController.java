package com.projeto.suporteAoCliente.controller;

import com.projeto.suporteAoCliente.dto.request.EmailRequestDTO;
import com.projeto.suporteAoCliente.dto.request.UserRequestDTO;
import com.projeto.suporteAoCliente.dto.response.UserResponseDTO;
import com.projeto.suporteAoCliente.service.UserService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Cadastrar usuário
    @PostMapping("/create-user")
    public ResponseEntity<String> createUser(@RequestBody UserRequestDTO dto) {
        return userService.register(dto);
    }

    // Listar todos os usuários
    @GetMapping("/all-user")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/all-user-pending")
    public ResponseEntity<List<UserResponseDTO>> getPendingOrRejectedUsers() {
        return ResponseEntity.ok(userService.findAllPendingOrRejected());
    }

    // Buscar usuário por ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    // Atualizar usuário
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody @Valid UserRequestDTO dto) {
        UserResponseDTO updatedUser = userService.update(id, dto);
        return ResponseEntity.ok(updatedUser);
    }

    // Deletar usuário
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/approve-user")
    public ResponseEntity<String> approveUser(@RequestBody EmailRequestDTO dto) {
        return userService.approveUser(dto);
    }

}
