package br.com.bgrbarbosa.product_catalog.service.impl;


import br.com.bgrbarbosa.product_catalog.model.Category;
import br.com.bgrbarbosa.product_catalog.model.Product;
import br.com.bgrbarbosa.product_catalog.repository.ProductRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    private MimeMessage mimeMessage;

    private Product p1;
    private Product p2;
    private Category category;

    @BeforeEach
    void setUp() throws jakarta.mail.MessagingException {
        UUID uuidP1 = UUID.randomUUID();
        UUID uuidP2 = UUID.randomUUID();
        mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        p1 = new Product(uuidP1, "Cabo de Rede par trançado", "Cabo de rede par trançado categoria 5e Furukawa", 200.0, "http://upload123", LocalDate.of(2023, 10, 26), null, category);
        p2 = new Product(uuidP2, "Cabo de celular V8", "Cabo de celular V8", 20.0, "http://upload321", LocalDate.of(2023, 10, 26), null, category);
        category = new Category(UUID.randomUUID(), "Cabos", "Categoria de cabos", LocalDate.of(2023, 10, 26),LocalDate.of(2023, 10, 26), List.of());

    }

    @Test
    void sendingProductListByEmail_shouldSendEmailWithPdfAttachment() throws jakarta.mail.MessagingException {

        // Arrange
        String recipient = "test@example.com";
        List<Product> products = new ArrayList<>();
        products.add(p1);
        products.add(p2);

        when(productRepository.findAll()).thenReturn(products);

        // Argument captors to inspect the arguments passed to mailSender.send()
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);

        // Act
        assertDoesNotThrow(() -> emailService.sendingProductListByEmail(recipient));

        // Assert
        verify(productRepository, times(1)).findAll();
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(messageCaptor.capture());
        MimeMessage capturedMessage = messageCaptor.getValue();

    }

    @Test
    void sendingProductListByEmail_whenNoProducts_shouldSendEmailWithEmptyTable() throws jakarta.mail.MessagingException {
        // Arrange
        String recipient = "test@example.com";
        List<Product> products = new ArrayList<>(); // Empty list

        when(productRepository.findAll()).thenReturn(products);

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);

        // Act
        assertDoesNotThrow(() -> emailService.sendingProductListByEmail(recipient));

        // Assert
        verify(productRepository, times(1)).findAll();
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(messageCaptor.capture());
    }

}