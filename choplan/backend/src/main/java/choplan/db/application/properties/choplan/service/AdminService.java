package choplan.db.application.properties.choplan.service;

import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import choplan.db.application.properties.choplan.dto.AuthResponse;
import choplan.db.application.properties.choplan.dto.LoginRequest;
import choplan.db.application.properties.choplan.dto.SignupRequestAdmin;
import choplan.db.application.properties.choplan.entity.CustomerStatus;
import choplan.db.application.properties.choplan.entity.OwnerStatus;
import choplan.db.application.properties.choplan.entity.UserRole;
import choplan.db.application.properties.choplan.entity.Users;
import choplan.db.application.properties.choplan.repository.UserRepository;
import choplan.db.application.properties.choplan.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // ADMIN 회원가입
    public Users registerAdmin(SignupRequestAdmin request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Users admin = Users.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .realName(request.getRealName())
                .phone(request.getPhone())
                .role(UserRole.ADMIN)
                .build();

        return userRepository.save(admin);
    }

    // ADMIN 로그인
    public AuthResponse login(LoginRequest request) {
        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(
                200,
                "로그인 성공",
                Map.of(
                        "token", token,
                        "email", user.getEmail(),
                        "role", user.getRole().name()
                )
        );
    }

    // OWNER 상태 변경 (승인/거절/정지/탈퇴)
    public Users updateOwnerStatus(Long ownerId, OwnerStatus status) {
        Users owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 OWNER 사용자를 찾을 수 없습니다."));

        if (owner.getRole() != UserRole.OWNER) {
            throw new IllegalArgumentException("해당 사용자는 OWNER 권한이 아닙니다.");
        }

        owner.setOwnerStatus(status);
        return userRepository.save(owner);
    }

    // CUSTOMER 상태 변경 (활성/정지/탈퇴)
    public Users updateCustomerStatus(Long customerId, CustomerStatus status) {
        Users customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 CUSTOMER 사용자를 찾을 수 없습니다."));

        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new IllegalArgumentException("해당 사용자는 CUSTOMER 권한이 아닙니다.");
        }

        customer.setCustomerStatus(status);
        return userRepository.save(customer);
    }

    public List<Users> findCustomers(CustomerStatus status) {
        if (status == null) {
            return userRepository.findAllByRole(UserRole.CUSTOMER);
        }
        return userRepository.findAllByRoleAndCustomerStatus(UserRole.CUSTOMER, status);
    }

    public List<Users> findOwners(OwnerStatus status) {
        if (status == null) {
            return userRepository.findAllByRole(UserRole.OWNER);
        }
        return userRepository.findAllByRoleAndOwnerStatus(UserRole.OWNER, status);
    }
}
