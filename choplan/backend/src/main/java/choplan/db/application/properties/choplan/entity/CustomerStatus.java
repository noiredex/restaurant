package choplan.db.application.properties.choplan.entity;

public enum CustomerStatus {

    ACTIVE(0, "활성"),        // 이용 중
    SUSPENDED(1, "이용정지"), // admin 제제로 이용정지
    DELETED(2, "회원탈퇴");   // 회원 탈퇴

    private final int code;
    private final String description;

    // 생성자에서 파라미터 이름을 desc로 분리
    CustomerStatus(int code, String desc) {
        this.code = code;
        this.description = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
