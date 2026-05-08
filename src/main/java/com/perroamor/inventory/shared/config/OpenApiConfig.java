package com.perroamor.inventory.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Perro Amor — Inventory API")
                        .description("Backend del sistema de inventario y POS de Perro Amor.")
                        .version("v1")
                        .contact(new Contact().name("Perro Amor"))
                        .license(new License().name("Proprietary")));
    }
}
