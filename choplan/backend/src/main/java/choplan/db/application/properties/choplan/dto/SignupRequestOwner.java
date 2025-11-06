package choplan.db.application.properties.choplan.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestOwner {

    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 10, message = "비밀번호는 최소 10자리 이상이어야 합니다.")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9]).*$",
        message = "비밀번호에는 숫자와 대문자가 최소 1개 이상 포함되어야 합니다."
    )
    private String password;

    @NotBlank(message = "사장님 이름은 필수 입력값입니다.")
    @Pattern(regexp = "^[가-힣]*$", message = "이름은 한글만 입력 가능합니다.")
    private String realName;

    @NotBlank(message = "개인 전화번호는 필수 입력값입니다.")
    @Pattern(regexp = "^[0-9]*$", message = "전화번호는 숫자만 입력 가능합니다.")
    private String phone;

    @NotBlank(message = "가게 이름은 필수 입력값입니다.")
    private String storeName;

    @NotBlank(message = "가게 전화번호는 필수 입력값입니다.")
    @Pattern(regexp = "^[0-9]*$", message = "전화번호는 숫자만 입력 가능합니다.")
    private String storePhone;

    @NotBlank(message = "가게 주소는 필수 입력값입니다.")
    private String roadAddress;

    @NotBlank(message = "가게 상세주소는 필수 입력값입니다.")
    private String detailAddress;   // 새로 추가된 필드

    @NotBlank(message = "사업자 등록번호는 필수 입력값입니다.")
    private String businessNumber;

    // 사업자 등록증 파일은 MultipartFile로 따로 받음 (컨트롤러에서 처리)
}
