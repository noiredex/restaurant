package choplan.db.application.properties.choplan.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import choplan.db.application.properties.choplan.dto.AuthResponse;
import choplan.db.application.properties.choplan.dto.LoginRequest;
import choplan.db.application.properties.choplan.dto.SignupRequestCustomer;
import choplan.db.application.properties.choplan.entity.CustomerStatus;
import choplan.db.application.properties.choplan.entity.UserRole;
import choplan.db.application.properties.choplan.entity.Users;
import choplan.db.application.properties.choplan.repository.UserRepository;
import choplan.db.application.properties.choplan.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // CUSTOMER 회원가입
    public Users registerCustomer(SignupRequestCustomer request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Users user = Users.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .realName(request.getRealName())
                .phone(request.getPhone())
                .nickname(request.getNickname())
                .role(UserRole.CUSTOMER)
                .customerStatus(CustomerStatus.ACTIVE) // 기본값 ACTIVE
                .build();

        return userRepository.save(user);
    }

    // CUSTOMER 로그인
    public AuthResponse login(LoginRequest request) {
        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        // 상태 체크 (활성 사용자만 로그인 가능)
        if (user.getCustomerStatus() != CustomerStatus.ACTIVE) {
            throw new IllegalArgumentException("현재 계정 상태로는 로그인할 수 없습니다. (상태: "
                                               + user.getCustomerStatus().name() + ")");
        }

        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(
                200,
                "로그인 성공",
                java.util.Map.of(
                        "token", token,
                        "email", user.getEmail(),
                        "role", user.getRole().name(),
                        "customerStatus", user.getCustomerStatus().name()
                )
        );
    }

    // CUSTOMER 상태 변경 (관리자 전용)
    public Users updateCustomerStatus(Long customerId, CustomerStatus status) {
        Users customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 CUSTOMER를 찾을 수 없습니다."));

        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new IllegalArgumentException("해당 사용자는 CUSTOMER 권한이 아닙니다.");
        }

        customer.setCustomerStatus(status);
        return userRepository.save(customer);
    }
}
