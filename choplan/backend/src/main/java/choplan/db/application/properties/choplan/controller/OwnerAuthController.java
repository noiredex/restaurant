package choplan.db.application.properties.choplan.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import choplan.db.application.properties.choplan.dto.AuthResponse;
import choplan.db.application.properties.choplan.dto.SignupRequestOwner;
import choplan.db.application.properties.choplan.entity.Users;
import choplan.db.application.properties.choplan.service.OwnerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/owner")
@RequiredArgsConstructor
public class OwnerAuthController {

    private final OwnerService ownerService;

    /**
     * OWNER 회원가입
     * - 사업자등록증 파일 업로드 포함
     * - @ModelAttribute로 DTO와 파일을 함께 받음
     */
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> signupOwner(
            @ModelAttribute SignupRequestOwner request,
            @RequestParam("businessRegistrationDoc") MultipartFile businessRegistrationDoc) {

        try {
            // ✅ 파일을 그대로 전달 — OwnerService 내부에서 S3 업로드 처리함
            Users savedOwner = ownerService.registerOwner(request, businessRegistrationDoc);

            return ResponseEntity.ok(
                    new AuthResponse(
                            200,
                            "OWNER 회원가입 성공 (관리자 승인 대기중)",
                            java.util.Map.of(
                                    "userId", savedOwner.getUserId(),
                                    "email", savedOwner.getEmail(),
                                    "role", savedOwner.getRole().name(),
                                    "ownerStatus", savedOwner.getOwnerStatus().name(),
                                    "businessDocUrl", savedOwner.getBusinessRegistrationDoc()
                            )
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(400, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(500, "서버 오류: " + e.getMessage(), null));
        }
    }

    /**
     * OWNER 승인 (관리자 전용)
     */
    @PatchMapping("/approve/{ownerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> approveOwner(@PathVariable Long ownerId) {
        try {
            Users approvedOwner = ownerService.approveOwner(ownerId);

            return ResponseEntity.ok(
                    new AuthResponse(
                            200,
                            "OWNER 승인 완료",
                            java.util.Map.of(
                                    "userId", approvedOwner.getUserId(),
                                    "email", approvedOwner.getEmail(),
                                    "role", approvedOwner.getRole().name(),
                                    "ownerStatus", approvedOwner.getOwnerStatus().name()
                            )
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(400, e.getMessage(), null));
        }
    }
}
