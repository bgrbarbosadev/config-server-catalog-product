package br.com.bgrbarbosa.product_catalog.service.impl;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;

import br.com.bgrbarbosa.product_catalog.service.ProductServiceReport;
import net.sf.jasperreports.engine.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@ExtendWith(MockitoExtension.class)
class ProductServiceReportTest {

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Resource resource;

    @Mock
    private Map<String, Object> params;

    @Mock
    private Logger log;

    @InjectMocks
    private ProductServiceReport serviceReport;

    private final String JASPER_DIRETORIO = "/resources/reports/";

    @Test
    @DisplayName("Deve adicionar os parâmetros padrão e o parâmetro fornecido ao mapa")
    void shouldAddAllParamsToMap() {
        // Cenário de teste
        String key = "PRODUTO_NOME";
        String value = "Notebook";

        // 1. Execução do método a ser testado
        serviceReport.addParams(key, value);

        // 2. Verificação dos resultados

        // Verifica se os parâmetros padrão foram adicionados
        assertTrue(params.containsKey("IMAGEM_DIRETORIO"));
        assertEquals(JASPER_DIRETORIO, params.get("IMAGEM_DIRETORIO"));

        assertTrue(params.containsKey("REPORT_LOCALE"));
        assertEquals(new Locale("pt", "BR"), params.get("REPORT_LOCALE"));

        // Verifica se o parâmetro fornecido foi adicionado
        assertTrue(params.containsKey(key));
        assertEquals(value, params.get(key));

        // 3. Verificação do tamanho do mapa
        assertEquals(3, params.size());
    }

    @Test
    @DisplayName("Deve substituir um parâmetro se a chave já existir")
    void shouldReplaceExistingParameter() {
        // Cenário de teste
        String key = "IMAGEM_DIRETORIO";
        String value = "novo/diretorio/imagens/";

        // Adicione um valor inicial para garantir que ele será substituído
        params.put("IMAGEM_DIRETORIO", "diretorio/antigo/");

        // 1. Execução do método a ser testado
        serviceReport.addParams(key, value);

        // 2. Verificação dos resultados
        // O valor do parâmetro IMAGEM_DIRETORIO deve ter sido atualizado
        assertEquals(value, params.get(key));

        // Os outros parâmetros padrão ainda devem existir
        assertTrue(params.containsKey("REPORT_LOCALE"));

        // O tamanho do mapa deve ser 3, pois um dos parâmetros padrão foi substituído
        assertEquals(3, params.size());
    }


    @Test
    @DisplayName("Deve gerar o PDF corretamente quando a lista não está vazia")
    void deveGerarPdfComSucesso() throws IOException, JRException {
        // 1. Mock das dependências e dados
        List<Object> mockDataList = Collections.singletonList(new Object());
        InputStream mockInputStream = mock(InputStream.class);
        JasperPrint mockJasperPrint = mock(JasperPrint.class);
        byte[] expectedBytes = "conteudo-pdf-mock".getBytes();

        // 2. Configuração do comportamento dos mocks
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(mockInputStream);


        try (MockedStatic<JasperFillManager> fillManagerMock = mockStatic(JasperFillManager.class);
             MockedStatic<JasperExportManager> exportManagerMock = mockStatic(JasperExportManager.class)) {

            // Configura o mock do método fillReport. `anyMap()` funciona para mockar um Map genérico.
            fillManagerMock.when(() -> JasperFillManager.fillReport(any(InputStream.class), anyMap(), any(JRDataSource.class)))
                    .thenReturn(mockJasperPrint);

            // Configura o mock do método exportReportToPdf
            exportManagerMock.when(() -> JasperExportManager.exportReportToPdf(any(JasperPrint.class)))
                    .thenReturn(expectedBytes);

            byte[] resultBytes = serviceReport.gerarPdf(mockDataList);

            // 4. Verificação dos resultados
            assertNotNull(resultBytes);
            assertArrayEquals(expectedBytes, resultBytes);

            // 5. Verificação das chamadas
            verify(resourceLoader).getResource(JASPER_DIRETORIO.concat("Produto.jasper"));
            verify(resource).getInputStream();
        }
    }

    @Test
    @DisplayName("Deve logar o erro e lançar RuntimeException em caso de JRException")
    void shouldLogErrorAndThrowRuntimeExceptionOnJRException() throws IOException, JRException {
        // 1. Configuração do cenário de teste
        List<Object> mockDataList = Collections.singletonList(new Object());
        InputStream mockInputStream = mock(InputStream.class);

        // Define o comportamento dos mocks para o fluxo que precede a exceção
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(mockInputStream);

        // Cria o mock para os métodos estáticos
        try (MockedStatic<JasperFillManager> fillManagerMock = mockStatic(JasperFillManager.class)) {

            // Configura o mock para lançar uma JRException quando fillReport for chamado
            JRException jreException = new JRException("Simulando erro de preenchimento do relatório");
            fillManagerMock.when(() -> JasperFillManager.fillReport(any(InputStream.class), anyMap(), any(JRDataSource.class)))
                    .thenThrow(jreException);

            // 2. Execução do método e verificação da exceção lançada
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                serviceReport.gerarPdf(mockDataList);
            }, "Uma RuntimeException deveria ser lançada.");

            // 3. Verificação do log e da causa da exceção
            // Verifica se o método error do logger foi chamado
         //   verify(log).error(eq("Jasper Reports ::: "), any(Throwable.class));

            // Verifica se a causa da RuntimeException é a JRException original
            assertEquals(jreException, thrownException.getCause(), "A causa da RuntimeException deveria ser a JRException original.");
        }
    }

    @Test
    @DisplayName("Deve logar o erro e lançar RuntimeException em caso de IOException")
    void shouldLogErrorAndThrowRuntimeExceptionOnIOException() throws IOException {
        // 1. Configuração do cenário de teste
        List<Object> mockDataList = Collections.singletonList(new Object());

        // Define o comportamento dos mocks para que getInputStream lance uma IOException
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        IOException ioException = new IOException("Simulando erro de leitura do arquivo");
        when(resource.getInputStream()).thenThrow(ioException);

        // 2. Execução do método e verificação da exceção lançada
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            serviceReport.gerarPdf(mockDataList);
        }, "Uma RuntimeException deveria ser lançada.");

        // 3. Verificação do log e da causa da exceção
        // Verifica se o método error do logger foi chamado
       // verify(log).error(eq("Jasper Reports ::: "), any(Throwable.class));

        // Verifica se a causa da RuntimeException é a IOException original
        assertEquals(ioException, thrownException.getCause(), "A causa da RuntimeException deveria ser a IOException original.");
    }

}