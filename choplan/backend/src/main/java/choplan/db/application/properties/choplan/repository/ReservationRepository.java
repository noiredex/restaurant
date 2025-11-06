package choplan.db.application.properties.choplan.repository;

import choplan.db.application.properties.choplan.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 사용자 ID로 예약 조회
    List<Reservation> findByUserId(Long userId);

    // 매장 ID로 예약 조회
    List<Reservation> findByRestaurantId(Long restaurantId);

    // 예약 상태별 조회 (예: 예약완료, 취소, 대기)
    List<Reservation> findByStatus(Reservation.ReservationStatus status);

    // 사용자 예약 내역 최신순 정렬
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 매장 예약 내역 최신순 정렬
    List<Reservation> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    // 방문 상태별 조회 (예: 방문완료, 노쇼 등)
    List<Reservation> findByVisitStatus(Reservation.VisitStatus visitStatus);

    // 방문 상태별 최신순 정렬
    List<Reservation> findByVisitStatusOrderByCreatedAtDesc(Reservation.VisitStatus visitStatus);

    // 블랙리스트 처리된 예약 조회
    List<Reservation> findByIsBlacklistedTrue();

    // 블랙리스트 예약 최신순 조회
    List<Reservation> findByIsBlacklistedTrueOrderByCreatedAtDesc();

    // 사용자 ID로 예약 내역 직접 쿼리 조회
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId ORDER BY r.createdAt DESC")
    List<Reservation> findUserReservationsWithQuery(@Param("userId") Long userId);
}
