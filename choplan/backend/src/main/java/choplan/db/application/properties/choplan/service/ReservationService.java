package choplan.db.application.properties.choplan.service;

import choplan.db.application.properties.choplan.dto.ReservationRequest;
import choplan.db.application.properties.choplan.dto.VisitStatusRequest;
import choplan.db.application.properties.choplan.entity.Reservation;
import choplan.db.application.properties.choplan.entity.Users;
import choplan.db.application.properties.choplan.repository.ReservationRepository;
import choplan.db.application.properties.choplan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 테스트용 예약 서비스 - 데모 종료 시 제거 예정
 */
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Reservation createReservation(ReservationRequest request) {
        Reservation reservation = Reservation.builder()
                .userId(request.getUserId())
                .restaurantId(request.getRestaurantId())
                .restaurantName(request.getRestaurantName())
                .userName(request.getUserName())
                .userPhone(request.getUserPhone())
                .userEmail(request.getUserEmail())
                .reservationDate(request.getReservationDate())
                .reservationTime(request.getReservationTime())
                .guests(request.getGuests())
                .specialRequests(request.getSpecialRequests())
                .status(Reservation.ReservationStatus.PENDING)
                .build();

        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getUserReservations(Long userId) {
        System.out.println("getUserReservations 호출됨 - userId: " + userId);

        List<Reservation> reservations = reservationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        System.out.println("기본 메서드로 조회된 예약 수: " + reservations.size());

        if (reservations.isEmpty()) {
            System.out.println("기본 메서드 결과가 없어서 직접 쿼리 시도");
            reservations = reservationRepository.findUserReservationsWithQuery(userId);
            System.out.println("직접 쿼리로 조회된 예약 수: " + reservations.size());
        }

        for (Reservation r : reservations) {
            System.out.println("예약 ID: " + r.getId() + ", 상태: " + r.getStatus() + ", 방문상태: " + r.getVisitStatus());
        }
        return reservations;
    }

    @Transactional(readOnly = true)
    public List<Reservation> getRestaurantReservations(Long restaurantId) {
        List<Reservation> reservations = reservationRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
        return enrichReservationsWithUserNickname(reservations);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getOwnerReservations(Long userId, Long restaurantId) {
        List<Reservation> reservations = reservationRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
        return enrichReservationsWithUserNickname(reservations);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return enrichReservationsWithUserNickname(reservations);
    }

    @Transactional(readOnly = true)
    public Reservation getReservationById(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));
        try {
            Users user = userRepository.findById(reservation.getUserId()).orElse(null);
            if (user != null && user.getName() != null) {
                reservation.setUserName(user.getName());
            }
        } catch (Exception ignored) {}
        return reservation;
    }

    @Transactional
    public Reservation approveReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));
        reservation.setStatus(Reservation.ReservationStatus.APPROVED);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation rejectReservation(Long reservationId, String reason) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));
        reservation.setStatus(Reservation.ReservationStatus.REJECTED);
        reservation.setRejectionReason(reason);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation updateReservationStatus(Long reservationId, String status, String reason) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        Reservation.ReservationStatus reservationStatus;
        try {
            reservationStatus = Reservation.ReservationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("유효하지 않은 예약 상태입니다: " + status);
        }

        reservation.setStatus(reservationStatus);

        if (reservationStatus == Reservation.ReservationStatus.REJECTED && reason != null && !reason.isEmpty()) {
            reservation.setRejectionReason(reason);
        }

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        if (reservation.getStatus() == Reservation.ReservationStatus.PENDING) {
            reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        } else if (reservation.getStatus() == Reservation.ReservationStatus.APPROVED) {
            reservation.setStatus(Reservation.ReservationStatus.CANCELLED_PENDING);
        } else {
            throw new RuntimeException("취소할 수 없는 예약 상태입니다.");
        }

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation approveCancellation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        if (reservation.getStatus() != Reservation.ReservationStatus.CANCELLED_PENDING) {
            throw new RuntimeException("취소 요청 대기 상태가 아닙니다.");
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation rejectCancellation(Long reservationId, String reason) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        if (reservation.getStatus() != Reservation.ReservationStatus.CANCELLED_PENDING) {
            throw new RuntimeException("취소 요청 대기 상태가 아닙니다.");
        }

        reservation.setStatus(Reservation.ReservationStatus.APPROVED);
        reservation.setRejectionReason(reason);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation updateVisitStatus(VisitStatusRequest request) {
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        Reservation.VisitStatus visitStatus =
                Reservation.VisitStatus.valueOf(request.getVisitStatus().toUpperCase());
        reservation.setVisitStatus(visitStatus);

        if (visitStatus == Reservation.VisitStatus.VISITED) {
            reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
            reservation.setVisitConfirmedAt(java.time.LocalDateTime.now());
        }

        if (visitStatus == Reservation.VisitStatus.NO_SHOW
                && request.getReason() != null && !request.getReason().isEmpty()) {
            reservation.setNoShowReason(request.getReason());
        }

        if (visitStatus == Reservation.VisitStatus.BLACKLISTED) {
            reservation.setIsBlacklisted(true);
            if (request.getReason() != null && !request.getReason().isEmpty()) {
                reservation.setBlacklistReason(request.getReason());
            }
        }

        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByVisitStatus(Reservation.VisitStatus visitStatus) {
        List<Reservation> reservations =
                reservationRepository.findByVisitStatusOrderByCreatedAtDesc(visitStatus);
        return enrichReservationsWithUserNickname(reservations);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getBlacklistedReservations() {
        List<Reservation> reservations =
                reservationRepository.findByIsBlacklistedTrueOrderByCreatedAtDesc();
        return enrichReservationsWithUserNickname(reservations);
    }

    /**
     * 예약 리스트에 사용자 닉네임 정보를 추가합니다
     */
    private List<Reservation> enrichReservationsWithUserNickname(List<Reservation> reservations) {
        return reservations.stream().map(reservation -> {
            try {
                Users user = userRepository.findById(reservation.getUserId()).orElse(null);
                if (user != null && user.getName() != null) {
                    reservation.setUserName(user.getName());
                }
            } catch (Exception ignored) {}
            return reservation;
        }).collect(Collectors.toList());
    }
}
