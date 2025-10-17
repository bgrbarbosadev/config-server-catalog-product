package br.com.bgrbarbosa.product_catalog.controller;

import br.com.bgrbarbosa.product_catalog.controller.mapper.CategoryMapper;
import br.com.bgrbarbosa.product_catalog.model.Category;
import br.com.bgrbarbosa.product_catalog.model.dto.CategoryDTO;
import br.com.bgrbarbosa.product_catalog.service.CategoryService;
import br.com.bgrbarbosa.product_catalog.service.exception.ResourceNotFoundException;
import br.com.bgrbarbosa.product_catalog.specification.filter.ProductFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/category")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Contém as operações para controle de cadastro de categorias.")
public class CategoryController {

	private final CategoryService service;
	private final CategoryMapper mapper;

	@GetMapping
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(
			summary = "Listar todas as Categorias",
			description = "Listar todas as categorias cadastradas",
			responses = {
					@ApiResponse(responseCode = "200", description = "Lista todas as categorias cadastradas",
							content = @Content(mediaType = "application/json"))
			})
	public ResponseEntity<Page<CategoryDTO>> findAll(
			@PageableDefault(page = 0, size = 10, sort = "uuid", direction = Sort.Direction.ASC) Pageable page){

		List<CategoryDTO> listDTO = mapper.parseToListDTO(service.findAll(page));
		Page<CategoryDTO> pageDTO = mapper.toPageDTO(listDTO, page);
		return ResponseEntity.ok(pageDTO);
	}

	@GetMapping(value = "/{uuid}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Recuperar uma categoria pelo id", description = "Recuperar uma categoria pelo id",
			responses = {
					@ApiResponse(responseCode = "200", description = "Categoria recuperada com sucesso",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDTO.class))),
					@ApiResponse(responseCode = "404", description = "Categoria não encontrada",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceNotFoundException.class)))
			})
	public ResponseEntity<CategoryDTO> findById(@PathVariable UUID uuid) {
		CategoryDTO dto = mapper.parseToDto(service.findById(uuid));
		return ResponseEntity.ok().body(dto);
	}

	@GetMapping("/report")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Gerar relatórios de categorias usando filtros", description = "Gerar relatórios de categorias usando filtros",
			responses = {
					@ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDTO.class)))
			})
	public void gerarRelatorio(
			HttpServletResponse response,
			ProductFilter filter,
			@RequestParam(name = "fileType", defaultValue = "pdf") String fileType
	) throws JRException, IOException {

		// Carregar e compilar o arquivo JRXML
		InputStream jasperStream = this.getClass().getResourceAsStream("/reports/Categoria.jrxml");
		JasperReport jasperReport = JasperCompileManager.compileReport(jasperStream);

		// Definir os dados para o relatório
		List<Category> dados = service.findAll();
		JRDataSource dataSource = new JRBeanCollectionDataSource(dados);

		// Adicionar parâmetros ao relatório (opcional)
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("titulo", "Relatório de Exemplo");

		// Preencher o relatório
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

		// --- Lógica de exportação dinâmica ---
		OutputStream outputStream = response.getOutputStream();

		if ("xlsx".equalsIgnoreCase(fileType)) {
			// Configurar para XLSX
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setHeader("Content-Disposition", "attachment; filename=\"relatorio.xlsx\"");

			// Exportar para XLSX
			JRXlsxExporter exporter = new JRXlsxExporter();
			exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
			exporter.exportReport();

		} else if ("csv".equalsIgnoreCase(fileType)) {
			// Configurar para CSV
			response.setContentType("text/csv");
			response.setHeader("Content-Disposition", "attachment; filename=\"relatorio.csv\"");

			// Exportar para CSV
			JRCsvExporter exporter = new JRCsvExporter();
			exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));
			exporter.exportReport();

		} else {
			// Padrão: exportar para PDF
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment; filename=\"relatorio.pdf\"");

			JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
		}
	}
	
	@PostMapping
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
	@Operation(summary = "Cadastrar uma nova categoria", description = "Recurso para cadastrar categorias",
			responses = {
					@ApiResponse(responseCode = "201", description = "Categoria cadastrada com sucesso",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDTO.class)))
			})
	public ResponseEntity<CategoryDTO> insert(@RequestBody @Valid CategoryDTO dto) {
		Category result = service.insert(mapper.parseToEntity(dto));
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uuid}")
				.buildAndExpand(result.getUuidCategory()).toUri();
		return ResponseEntity.created(uri).body(mapper.parseToDto(result));
	}

	@PutMapping
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
	@Operation(summary = "Atualizar categoria", description = "Atualizar registro de categoria",
			responses = {
					@ApiResponse(responseCode = "204", description = "Categoria atualizado com sucesso",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDTO.class))),
					@ApiResponse(responseCode = "404", description = "Categoria não encontrada",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceNotFoundException.class)))
			})
	public ResponseEntity<CategoryDTO> update(@RequestBody @Valid CategoryDTO dto) {
		Category result = service.update(mapper.parseToEntity(dto));
		return ResponseEntity.ok().body(mapper.parseToDto(result));
	}

	@DeleteMapping(value = "/{uuid}")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
	@Operation(summary = "Deleção de categoria", description = "Deletar uma categoria pelo ID",
			responses = {
					@ApiResponse(responseCode = "202", description = "Categoria deletada com sucesso",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDTO.class))),
					@ApiResponse(responseCode = "404", description = "Categoria não encontrada",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceNotFoundException.class)))
			})
	public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
		service.delete(uuid);
		return ResponseEntity.noContent().build();
	}
} 
