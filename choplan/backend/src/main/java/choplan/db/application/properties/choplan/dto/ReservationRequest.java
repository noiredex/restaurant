package choplan.db.application.properties.choplan.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

/**
 * 테스트용 예약 요청 DTO - 데모 종료 시 제거 예정
 */
@Data
public class ReservationRequest {
    private Long userId;              // 예약자 ID
    private Long restaurantId;        // 매장 ID
    private String restaurantName;    // 매장 이름
    private String userName;          // 예약자 이름
    private String userPhone;         // 예약자 연락처
    private String userEmail;         // 예약자 이메일
    private LocalDate reservationDate; // 예약 날짜
    private LocalTime reservationTime; // 예약 시간
    private Integer guests;            // 인원 수
    private String specialRequests;    // 요청사항
}
