package com.pser.auction.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;


@OpenAPIDefinition(info = @Info(title = "Auction API 명세서", description = "Auction API 명세서", version = "v1"))
@Configuration
public class SwaggerConfig {
    @Value("${gateway.url}")
    private String gatewayUrl;

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = HttpHeaders.AUTHORIZATION;
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(
                        jwtSchemeName, new SecurityScheme()
                                .name(jwtSchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("Bearer")
                                .bearerFormat("JWT")
                );

        Server gatewayServer = new Server();
        gatewayServer.setUrl(gatewayUrl);

        OpenAPI openAPI = new OpenAPI()
                .addSecurityItem(securityRequirement)
                .components(components);
        openAPI.setServers(List.of(gatewayServer));

        return openAPI;
    }
}