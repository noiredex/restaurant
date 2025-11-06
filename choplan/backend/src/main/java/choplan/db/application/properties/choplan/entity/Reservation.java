package choplan.db.application.properties.choplan.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 테스트용 예약 엔티티 - 데모 종료 시 제거 예정
 */
@Entity
@Table(name = "demo_reservations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;
    
    @Column(name = "restaurant_name")
    private String restaurantName;
    
    @Column(name = "user_name", nullable = false)
    private String userName;
    
    @Column(name = "user_phone", nullable = false)
    private String userPhone;
    
    @Column(name = "user_email")
    private String userEmail;
    
    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;
    
    @Column(name = "reservation_time", nullable = false)
    private LocalTime reservationTime;
    
    @Column(nullable = false)
    private Integer guests;
    
    @Column(name = "special_requests", length = 1000)
    private String specialRequests;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;
    
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = ReservationStatus.PENDING;
        }
        if (visitStatus == null) {
            visitStatus = VisitStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @Column(name = "visit_status")
    @Enumerated(EnumType.STRING)
    private VisitStatus visitStatus;
    
    @Column(name = "visit_confirmed_at")
    private LocalDateTime visitConfirmedAt;
    
    @Column(name = "no_show_reason", length = 500)
    private String noShowReason;
    
    @Column(name = "blacklist_reason", length = 500)
    private String blacklistReason;
    
    @Column(name = "is_blacklisted")
    @Builder.Default
    private Boolean isBlacklisted = false;
    
    public enum ReservationStatus {
        PENDING,           // 대기중
        APPROVED,          // 승인됨
        REJECTED,          // 거절됨
        CANCELLED,         // 취소됨
        CANCELLED_PENDING, // 취소 요청 대기중
        COMPLETED          // 완료됨
    }
    
    public enum VisitStatus {
        PENDING,    // 방문 대기
        VISITED,    // 방문함
        NO_SHOW,    // 노쇼
        BLACKLISTED // 블랙리스트
    }
}
