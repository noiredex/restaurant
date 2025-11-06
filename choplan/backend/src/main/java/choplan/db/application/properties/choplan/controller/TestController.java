package choplan.db.application.properties.choplan.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    // CUSTOMER 전용
    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String customerHello() {
        return "Hello, CUSTOMER!";
    }

    // OWNER 전용
    @GetMapping("/owner")
    @PreAuthorize("hasRole('OWNER')")
    public String ownerHello() {
        return "Hello, OWNER!";
    }

    // ADMIN 전용
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminHello() {
        return "Hello, ADMIN!";
    }
}
