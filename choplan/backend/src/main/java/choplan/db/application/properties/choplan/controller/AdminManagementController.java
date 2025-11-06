package choplan.db.application.properties.choplan.controller;

import choplan.db.application.properties.choplan.entity.Users;
import choplan.db.application.properties.choplan.entity.CustomerStatus;
import choplan.db.application.properties.choplan.entity.OwnerStatus;
import choplan.db.application.properties.choplan.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminManagementController {

    private final AdminService adminService;

    // CUSTOMER 목록 조회 (상태 필터 가능)
    @GetMapping("/customers")
    public ResponseEntity<List<Map<String, Object>>> getCustomers(
            @RequestParam(required = false) CustomerStatus status) {

        List<Users> customers = adminService.findCustomers(status);
        List<Map<String, Object>> result = customers.stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getId());
                    map.put("email", user.getEmail());
                    map.put("realName", user.getRealName());
                    map.put("phone", user.getPhone());
                    map.put("status", user.getCustomerStatus() != null ? user.getCustomerStatus().name() : null);
                    map.put("createdAt", user.getCreatedAt());
                    map.put("updatedAt", user.getUpdatedAt());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // OWNER 목록 조회 (상태 필터 가능)
    @GetMapping("/owners")
    public ResponseEntity<List<Map<String, Object>>> getOwners(
            @RequestParam(required = false) OwnerStatus status) {

        List<Users> owners = adminService.findOwners(status);
        List<Map<String, Object>> result = owners.stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getId());
                    map.put("email", user.getEmail());
                    map.put("realName", user.getRealName());
                    map.put("phone", user.getPhone());
                    map.put("status", user.getOwnerStatus() != null ? user.getOwnerStatus().name() : null);
                    map.put("createdAt", user.getCreatedAt());
                    map.put("updatedAt", user.getUpdatedAt());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // OWNER 상태 변경
    @PutMapping("/owners/{ownerId}/status")
    public ResponseEntity<String> updateOwnerStatus(
            @PathVariable Long ownerId,
            @RequestParam OwnerStatus status) {
        adminService.updateOwnerStatus(ownerId, status);
        return ResponseEntity.ok("OWNER 상태가 " + status + "(으)로 변경되었습니다.");
    }

    // CUSTOMER 상태 변경
    @PutMapping("/customers/{customerId}/status")
    public ResponseEntity<String> updateCustomerStatus(
            @PathVariable Long customerId,
            @RequestParam CustomerStatus status) {
        adminService.updateCustomerStatus(customerId, status);
        return ResponseEntity.ok("CUSTOMER 상태가 " + status + "(으)로 변경되었습니다.");
    }
}
