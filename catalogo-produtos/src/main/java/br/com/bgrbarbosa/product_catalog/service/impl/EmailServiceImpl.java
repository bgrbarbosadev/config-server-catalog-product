package br.com.bgrbarbosa.product_catalog.service.impl;

import br.com.bgrbarbosa.product_catalog.model.Product;
import br.com.bgrbarbosa.product_catalog.repository.ProductRepository;
import br.com.bgrbarbosa.product_catalog.service.EmailService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final ProductRepository productRepository;

    private final JavaMailSender mailSender;

    @Override
    public void sendingProductListByEmail(String destination) {
        try {
            // 1. Obtem os produtos
            List<Product> produtos = productRepository.findAll();

            // 2. Gera o PDF na memória
            ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, pdfStream);
            document.open();

            // Adiciona um título
            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(0, 102, 204));
            Paragraph title = new Paragraph("Relatório de Produtos", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Adiciona uma tabela para os produtos
            PdfPTable table = new PdfPTable(2); // 2 colunas: Nome e Preço
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Cabeçalho da tabela
            table.addCell("Nome do Produto");
            table.addCell("Preço");

            // Popula a tabela com os dados dos produtos
            for (Product produto : produtos) {
                table.addCell(produto.getNameProduct());
                table.addCell("R$" + produto.getPriceProduct());
            }

            document.add(table);
            document.close();

            // 3. Cria a mensagem de e-mail com anexo
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true); // true para multi-part (anexo)

            helper.setFrom("${HOST_USERNAME}"); // email da aplicação.
            helper.setTo(destination);
            helper.setSubject("Relatório de Produtos - Anexo PDF");
            helper.setText("Prezado(a),\n\nSegue em anexo o relatório completo com a lista de todos os produtos.\n\nAtenciosamente,\nBGRBARBOSA.INFO");

            // Anexa o PDF
            helper.addAttachment("relatorio-produtos.pdf", new ByteArrayResource(pdfStream.toByteArray()));

            // 4. Envia o e-mail
            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao gerar PDF ou enviar e-mail: " + e.getMessage());
        }
    }



}
