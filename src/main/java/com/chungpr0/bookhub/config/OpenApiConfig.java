package com.chungpr0.bookhub.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI bookHubOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BookHub API Specification")
                        .description("Tài liệu đặc tả và kiểm thử tương tác RESTful API hệ thống BookHub. " +
                                "Tuân thủ nghiêm ngặt quy chuẩn response contract ApiResponse<T>, xử lý lỗi tập trung, " +
                                "và hỗ trợ ổ khóa bảo mật JWT Bearer Authentication cho việc test trực tiếp trên UI.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BookHub Engineering Team")
                                .email("contact@bookhub.com"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Nhập mã Access Token (JWT) được cấp từ API /api/v1/auth/login. " +
                                        "Lưu ý: Không cần nhập tiền tố 'Bearer ', hệ thống sẽ tự động thêm vào HTTP header.")));
    }
}

