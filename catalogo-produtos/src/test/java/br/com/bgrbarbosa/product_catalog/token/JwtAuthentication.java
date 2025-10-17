package br.com.bgrbarbosa.product_catalog.token;

import br.com.bgrbarbosa.product_catalog.model.dto.UserRequestDTO;
import br.com.bgrbarbosa.product_catalog.model.dto.UserResponseDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.function.Consumer;

public class JwtAuthentication {

    public static Consumer<HttpHeaders> getHeaderAuthorization(WebTestClient client, String username, String password) {
        String token = client
                .post()
                .uri("/user/login")
                .bodyValue(new UserRequestDTO(username, password))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDTO.class)
                .returnResult().getResponseBody().token();
        return headers -> headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }
}
