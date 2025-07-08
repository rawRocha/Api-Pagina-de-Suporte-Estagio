package com.projeto.suporteAoCliente.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.projeto.suporteAoCliente.dto.request.EmailRequestDTO;
import com.projeto.suporteAoCliente.dto.request.UserRequestDTO;
import com.projeto.suporteAoCliente.dto.response.UserResponseDTO;
import com.projeto.suporteAoCliente.enums.Status;
import com.projeto.suporteAoCliente.enums.UserRole;
import com.projeto.suporteAoCliente.model.PendingUser;
import com.projeto.suporteAoCliente.model.User;
import com.projeto.suporteAoCliente.repository.PendingUserRepository;
import com.projeto.suporteAoCliente.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PendingUserRepository pendingUserRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public ResponseEntity<String> register(UserRequestDTO dto) {
        // 1. Validação de campos obrigatórios
        if (dto.getName() == null || dto.getName().isBlank() ||
                dto.getEmail() == null || dto.getEmail().isBlank() ||
                dto.getPassword() == null || dto.getPassword().isBlank() ||
                dto.getUserRole() == null || dto.getUserRole().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Preencha todos os campos.");
        }

        String email = dto.getEmail();

        // 2. Verifica se já existe usuário definitivo
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("O email já existe.");
        }

        // 3. Verifica se já existe usuário pendente com esse e-mail
        Optional<PendingUser> optional = pendingUserRepository.findByEmail(email);

        if (optional.isPresent()) {
            PendingUser existing = optional.get();

            if (existing.getStatus() == Status.PENDENTE) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("O email já existe, está aguardando aprovação.");
            }

            if (existing.getStatus() == Status.RECUSADO) {
                if (existing.getRegistrationAttempts() >= 3) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Este e-mail atingiu o limite de tentativas de cadastro, por favor, tente novamente com outro e-mail.");
                }

                // Permitir nova tentativa
                existing.setName(dto.getName());
                existing.setPassword(passwordEncoder.encode(dto.getPassword()));
                existing.setRole(UserRole.valueOf(dto.getUserRole().toUpperCase()));
                existing.setRegistrationAttempts(existing.getRegistrationAttempts() + 1);
                existing.setStatus(Status.PENDENTE);

                pendingUserRepository.save(existing);

                return ResponseEntity.ok("Nova tentativa de cadastro realizada com sucesso. Aguardando nova análise.");
            }
        }

        // 4. Novo cadastro
        PendingUser newUser = new PendingUser();
        newUser.setName(dto.getName());
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        newUser.setRole(UserRole.valueOf(dto.getUserRole().toUpperCase()));
        newUser.setStatus(Status.PENDENTE);
        newUser.setRegistrationAttempts(1);

        pendingUserRepository.save(newUser);

        return ResponseEntity.ok("Cadastro realizado com sucesso. Aguardando análise.");
    }

    public UserResponseDTO toDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        return dto;
    }

    public UserResponseDTO toDTO(PendingUser user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        return dto;
    }

    public List<UserResponseDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserResponseDTO> findAllPendingOrRejected() {
        List<Status> statuses = List.of(Status.PENDENTE, Status.RECUSADO);

        return pendingUserRepository.findAllByStatusIn(statuses)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO findById(Long id) {
        return userRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
    }

    public UserResponseDTO update(Long id, UserRequestDTO dto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));

        existingUser.setName(dto.getName());
        existingUser.setEmail(dto.getEmail());

        try {
            existingUser.setRole(UserRole.valueOf(dto.getUserRole().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Valor inválido para 'userRole': " + dto.getUserRole());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return toDTO(userRepository.save(existingUser));
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NoSuchElementException("Usuário não encontrado");
        }
        userRepository.deleteById(id);
    }

    public ResponseEntity<String> approveUser(EmailRequestDTO dto) {
        String email = dto.getEmail();

        PendingUser pendingUser = pendingUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Nenhum cadastro encontrado com este email"));

        User newUser = new User();
        newUser.setName(pendingUser.getName());
        newUser.setEmail(pendingUser.getEmail());
        newUser.setPassword(pendingUser.getPassword());
        newUser.setRole(pendingUser.getRole());

        userRepository.save(newUser);
        pendingUserRepository.delete(pendingUser);

        return ResponseEntity.ok("Usuário aprovado com sucesso!");
    }

    public ResponseEntity<String> refuseUser(EmailRequestDTO dto) {
        String email = dto.getEmail();

        PendingUser pendingUser = pendingUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Nenhum cadastro encontrado com este email"));

        int attempts = pendingUser.getRegistrationAttempts();

        if (attempts >= 3) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Número máximo de tentativas de cadastro atingido.");
        }

        pendingUser.setStatus(Status.RECUSADO);
        pendingUser.setRegistrationAttempts(attempts + 1);
        pendingUserRepository.save(pendingUser); // Salvar as alterações

        int tentativasRestantes = 4 - (attempts + 1);

        return ResponseEntity.ok("Usuário recusado com sucesso. Tentativas restantes: " + tentativasRestantes);
    }
}
