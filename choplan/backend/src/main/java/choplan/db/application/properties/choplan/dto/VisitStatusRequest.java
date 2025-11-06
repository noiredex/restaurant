package choplan.db.application.properties.choplan.dto;


import lombok.Data;

/**
 * 방문 상태 변경 요청 DTO
 */
@Data
public class VisitStatusRequest {
    private Long reservationId;
    private String visitStatus; // VISITED, NO_SHOW, BLACKLISTED
    private String reason; // 노쇼 사유 또는 블랙리스트 사유
    private Long createdBy; // 관리자 ID
}

