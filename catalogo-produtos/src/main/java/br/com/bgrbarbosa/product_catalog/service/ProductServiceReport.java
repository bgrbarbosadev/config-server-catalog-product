package br.com.bgrbarbosa.product_catalog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductServiceReport {

    private final ResourceLoader resourceLoader;

    private Map<String, Object> params = new HashMap<>();

    private static final String JASPER_DIRETORIO = "classpath:reports/";

    public void addParams(String key, Object value) {
        this.params.put("IMAGEM_DIRETORIO", JASPER_DIRETORIO);
        this.params.put("REPORT_LOCALE", new Locale("pt", "BR"));
        this.params.put(key, value);
    }

    /**
     * Gera um relatório PDF a partir de uma lista de objetos.
     * @param dataList A lista de objetos (entidades) que servirá como fonte de dados.
     * @return O array de bytes do relatório PDF gerado.
     */
    public byte[] gerarPdf(List<?> dataList) {
        byte[] bytes = null;
        try {
            // 1. Crie o JRDataSource a partir da lista de objetos
            JRDataSource dataSource = new JRBeanCollectionDataSource(dataList);

            // 2. Carregue o template do relatório
            Resource resource = resourceLoader.getResource(JASPER_DIRETORIO.concat("Produto.jasper"));
            InputStream stream = resource.getInputStream();

            // 3. Preencha o relatório com o JRDataSource
            JasperPrint print = JasperFillManager.fillReport(stream, params, dataSource);

            // 4. Exporte o relatório para PDF
            bytes = JasperExportManager.exportReportToPdf(print);

        } catch (IOException | JRException e) {
            log.error("Jasper Reports ::: ", e.getCause());
            throw new RuntimeException("Erro ao gerar o relatório PDF.", e);
        }
        return bytes;
    }
}
