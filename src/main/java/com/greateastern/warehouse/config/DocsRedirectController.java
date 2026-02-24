package com.greateastern.warehouse.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DocsRedirectController {

  @GetMapping("/")
  public String redirectToSwaggerUi() {
    return "redirect:/swagger-ui/index.html";
  }
}
