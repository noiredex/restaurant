package choplan.db.application.properties.choplan.entity;

public enum OwnerStatus {
    PENDING(0), // 승인 대기
    APPROVED(1), // 승인 완료, 운영중
    REJECTED(2), // 승인 거절
    SUSPENDED(3), // 계정 정지
    DELETE(4); // 회원 탈퇴, 폐업

    private final int code;

    OwnerStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static OwnerStatus fromCode(int code) {
        for (OwnerStatus status : OwnerStatus.values()) {
            if (status.code == code) return status;
            }
            throw new IllegalArgumentException("Invalid OwnerStatus code: " + code);
        }
    }
