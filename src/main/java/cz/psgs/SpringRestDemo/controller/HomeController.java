package cz.psgs.SpringRestDemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String demo(){
        return "Hello World...";
    }

  /*   @GetMapping("/test")
    @Tag(name = "Test", description = "Test API")
    @SecurityRequirement(name = "psgs-demo-api")
    public String test(){
        return "test_api";
    } */
    
}
