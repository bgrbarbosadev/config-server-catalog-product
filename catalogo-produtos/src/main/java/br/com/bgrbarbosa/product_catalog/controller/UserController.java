package br.com.bgrbarbosa.product_catalog.controller;

import br.com.bgrbarbosa.product_catalog.controller.mapper.UserMapper;
import br.com.bgrbarbosa.product_catalog.model.User;
import br.com.bgrbarbosa.product_catalog.model.dto.ProductDTO;
import br.com.bgrbarbosa.product_catalog.model.dto.UserDTO;
import br.com.bgrbarbosa.product_catalog.model.dto.UserRequestDTO;
import br.com.bgrbarbosa.product_catalog.model.dto.UserResponseDTO;
import br.com.bgrbarbosa.product_catalog.security.TokenService;
import br.com.bgrbarbosa.product_catalog.service.UserService;
import br.com.bgrbarbosa.product_catalog.service.exception.ResourceNotFoundException;
import br.com.bgrbarbosa.product_catalog.service.exception.UserException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping(value = "/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "Contém as operações para controle de cadastro de usuários. Obs: As roles são cadastradas manualmente no banco de dados, apenas usuários serão cadastrados na aplicação")
public class UserController {

	private final UserService service;
	private final UserMapper mapper;
	private final PasswordEncoder passwordEncoder;
	private final TokenService tokenService;

	@GetMapping
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
	@Operation(
			summary = "Listar todos os Usuarios",
			description = "Listar todos os usuarios cadastrados",
			responses = {
					@ApiResponse(responseCode = "200", description = "Lista todos os usuarios cadastradas",
							content = @Content(mediaType = "application/json"))
			})
	public ResponseEntity<Page<UserDTO>> findAll(
			@PageableDefault(page = 0, size = 10, sort = "uuid", direction = Sort.Direction.ASC) Pageable page){

		List<UserDTO> listDTO = mapper.parseToListDTO(service.findAll(page));
		Page<UserDTO> pageDTO = mapper.toPageDTO(listDTO, page);
		return ResponseEntity.ok(pageDTO);
	}

	@GetMapping(value = "/{uuid}")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
	@Operation(summary = "Recuperar um usuario pelo id", description = "Recuperar um usuario pelo id",
			responses = {
					@ApiResponse(responseCode = "200", description = "Usuario recuperado com sucesso",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
					@ApiResponse(responseCode = "404", description = "Usuario não encontrado",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceNotFoundException.class)))
			})
	public ResponseEntity<UserDTO> findById(@PathVariable UUID uuid) {
		UserDTO dto = mapper.parseToDto(service.findById(uuid));
		return ResponseEntity.ok().body(dto);
	}

	@PostMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Operation(summary = "Cadastra um novo usuario", description = "Recurso para cadastrar usuario",
			responses = {
					@ApiResponse(responseCode = "201", description = "Usuario cadastrado com sucesso",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)))
			})
	public ResponseEntity<UserDTO> insert(@RequestBody @Valid UserDTO dto) throws UserException {
		User result = service.insert(mapper.parseToEntity(dto));
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uuid}")
				.buildAndExpand(result.getUuid()).toUri();
		return ResponseEntity.created(uri).body(mapper.parseToDto(result));
	}

	@PutMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Operation(summary = "Atualizar usuario", description = "Atualizar registro de usuario",
			responses = {
					@ApiResponse(responseCode = "204", description = "Usuario atualizado com sucesso",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
					@ApiResponse(responseCode = "404", description = "Usuario não encontrado",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceNotFoundException.class)))
			})
	public ResponseEntity<UserDTO> update(@RequestBody @Valid UserDTO dto) {
		User result = service.update(mapper.parseToEntity(dto));
		return ResponseEntity.ok().body(mapper.parseToDto(result));
	}

	@DeleteMapping(value = "/{uuid}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Operation(summary = "Deleção de usuario", description = "Deletar um usuario pelo ID",
			responses = {
					@ApiResponse(responseCode = "202", description = "Usuario deletado com sucesso",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
					@ApiResponse(responseCode = "404", description = "Usuario não encontrado",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceNotFoundException.class)))
			})
	public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
		service.delete(uuid);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/login")
	@Operation(summary = "Obter acesso", description = "Recurso para obter um token de acesso",
			responses = {
					@ApiResponse(responseCode = "201", description = "Token obtido com sucesso",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)))
			})
	public ResponseEntity<Object> login(@RequestBody UserRequestDTO request){
		User user = service.loadUserByUsername(request.email());
		if(passwordEncoder.matches(request.password(), user.getPassword())) {
			String token = this.tokenService.generateToken(user);
			return ResponseEntity.ok(new UserResponseDTO(user.getFirstName(), token));
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(request);
	}
} 
