package choplan.db.application.properties.choplan.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestAdmin {

    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 10, message = "비밀번호는 최소 10자리 이상이어야 합니다.")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9]).*$",
             message = "비밀번호에는 숫자와 대문자가 최소 1개 이상 포함되어야 합니다.")
    private String password;

    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Pattern(regexp = "^[가-힣]*$", message = "이름은 한글만 입력 가능합니다.")
    private String realName;

    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    @Pattern(regexp = "^[0-9]*$", message = "전화번호는 숫자만 입력 가능합니다.")
    private String phone;
}
