package cz.psgs.SpringRestDemo.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "User API",
        version = "Version 0.5",
        contact = @Contact(),
        license = @License(),
        description = "Spring boot RESTful app demo"
    )
)
public class SwaggerConfig {
    
}
