package choplan.db.application.properties.choplan.controller;

import choplan.db.application.properties.choplan.dto.ReservationRequest;
import choplan.db.application.properties.choplan.dto.VisitStatusRequest;
import choplan.db.application.properties.choplan.entity.Reservation;
import choplan.db.application.properties.choplan.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 테스트용 예약 컨트롤러 - 데모 종료 시 제거 예정
 */
@RestController
@RequestMapping("/api/demo/reservations")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody ReservationRequest request) {
        try {
            Reservation reservation = reservationService.createReservation(request);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Reservation>> getUserReservations(@PathVariable Long userId) {
        return ResponseEntity.ok(reservationService.getUserReservations(userId));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<Reservation>> getRestaurantReservations(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(reservationService.getRestaurantReservations(restaurantId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Reservation>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReservationById(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.getReservationById(id);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveReservation(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.approveReservation(id);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectReservation(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String reason = body.getOrDefault("reason", "");
            Reservation reservation = reservationService.rejectReservation(id, reason);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateReservationStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String status = body.getOrDefault("status", "");
            String reason = body.getOrDefault("reason", null);

            if (status == null || status.isEmpty()) {
                return ResponseEntity.badRequest().body("예약 상태를 입력해주세요.");
            }

            Reservation reservation = reservationService.updateReservationStatus(id, status, reason);
            return ResponseEntity.ok(reservation);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.status(404).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("예약 상태 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.cancelReservation(id);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel/approve")
    public ResponseEntity<?> approveCancellation(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.approveCancellation(id);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel/reject")
    public ResponseEntity<?> rejectCancellation(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String reason = body.getOrDefault("reason", "");
            Reservation reservation = reservationService.rejectCancellation(id, reason);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/visit-status")
    public ResponseEntity<?> updateVisitStatus(@RequestBody VisitStatusRequest request) {
        try {
            Reservation reservation = reservationService.updateVisitStatus(request);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/visit-status/{status}")
    public ResponseEntity<List<Reservation>> getReservationsByVisitStatus(@PathVariable String status) {
        try {
            Reservation.VisitStatus visitStatus = Reservation.VisitStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(reservationService.getReservationsByVisitStatus(visitStatus));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/blacklisted")
    public ResponseEntity<List<Reservation>> getBlacklistedReservations() {
        return ResponseEntity.ok(reservationService.getBlacklistedReservations());
    }

    @PostMapping("/{id}/appeal")
    public ResponseEntity<?> submitAppeal(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String appealType = body.getOrDefault("appealType", "");
            String appealContent = body.getOrDefault("appealContent", "");
            String blacklistReason = body.getOrDefault("blacklistReason", "");

            // 이의 제기 처리 로직 (실제 구현에서는 별도 테이블에 저장)
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "이의 제기가 접수되었습니다.");
            response.put("appealId", System.currentTimeMillis()); // 임시 ID

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
