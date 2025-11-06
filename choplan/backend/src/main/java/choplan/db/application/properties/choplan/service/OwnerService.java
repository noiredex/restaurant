package choplan.db.application.properties.choplan.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import choplan.db.application.properties.choplan.dto.AuthResponse;
import choplan.db.application.properties.choplan.dto.LoginRequest;
import choplan.db.application.properties.choplan.dto.SignupRequestOwner;
import choplan.db.application.properties.choplan.entity.OwnerStatus;
import choplan.db.application.properties.choplan.entity.StoreAddress;
import choplan.db.application.properties.choplan.entity.UserRole;
import choplan.db.application.properties.choplan.entity.Users;
import choplan.db.application.properties.choplan.repository.UserRepository;
import choplan.db.application.properties.choplan.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // .env(application.yml)에서 주입받는 AWS 설정
    @Value("${AWS_ACCESS_KEY_ID}")
    private String awsAccessKey;

    @Value("${AWS_SECRET_ACCESS_KEY}")
    private String awsSecretKey;

    @Value("${AWS_REGION}")
    private String awsRegion;

    @Value("${AWS_S3_BUCKET}")
    private String awsBucketName;

    /**
     * OWNER 회원가입 (S3 업로드 포함)
     */
    public Users registerOwner(SignupRequestOwner request, MultipartFile businessDoc) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 주소 구성
        StoreAddress storeAddress = new StoreAddress();
        storeAddress.setRoadAddress(request.getRoadAddress());
        storeAddress.setDetailAddress(request.getDetailAddress());

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // S3 업로드 실행
        String uploadedUrl = uploadToS3(businessDoc);

        // 사용자 엔티티 생성
        Users user = Users.builder()
                .email(request.getEmail())
                .passwordHash(encodedPassword)
                .realName(request.getRealName())
                .phone(request.getPhone())
                .storeName(request.getStoreName())
                .storePhone(request.getStorePhone())
                .storeAddress(storeAddress)
                .businessRegistrationDoc(uploadedUrl) // S3 업로드 URL 저장
                .role(UserRole.OWNER)
                .ownerStatus(OwnerStatus.PENDING)
                .build();

        return userRepository.save(user);
    }

    /**
     * AWS S3 업로드 로직
     */
    private String uploadToS3(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("사업자등록증 파일이 필요합니다.");
        }

        try {
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null || !originalFileName.toLowerCase().endsWith(".pdf")) {
                throw new IllegalArgumentException("PDF 형식의 파일만 업로드 가능합니다.");
            }

            // S3 키명 생성
            String key = "business-docs/" + UUID.randomUUID() + "-" + originalFileName;

            // S3 클라이언트 생성
            S3Client s3Client = S3Client.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(awsAccessKey, awsSecretKey)
                            )
                    )
                    .build();

            // 임시 파일 생성 후 업로드
            Path tempFile = Files.createTempFile("upload-", ".pdf");
            file.transferTo(tempFile.toFile());

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(awsBucketName)
                    .key(key)
                    .contentType("application/pdf")
                    .build();

            s3Client.putObject(request, tempFile);
            Files.deleteIfExists(tempFile);

            return "https://" + awsBucketName + ".s3." + awsRegion + ".amazonaws.com/" + key;

        } catch (IOException | S3Exception e) {
            throw new RuntimeException("S3 업로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * OWNER 로그인
     */
    public AuthResponse login(LoginRequest request) {
        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        if (user.getOwnerStatus() != OwnerStatus.APPROVED) {
            throw new IllegalArgumentException("관리자 승인 후 로그인 가능합니다. (현재 상태: "
                    + user.getOwnerStatus().name() + ")");
        }

        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(
                200,
                "로그인 성공",
                Map.of(
                        "token", token,
                        "email", user.getEmail(),
                        "role", user.getRole().name(),
                        "ownerStatus", user.getOwnerStatus().name()
                )
        );
    }

    /**
     * OWNER 승인 (관리자 전용)
     */
    public Users approveOwner(Long ownerId) {
        Users owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 OWNER 사용자를 찾을 수 없습니다."));

        if (owner.getRole() != UserRole.OWNER) {
            throw new IllegalArgumentException("해당 사용자는 OWNER 권한이 아닙니다.");
        }

        owner.setOwnerStatus(OwnerStatus.APPROVED);
        return userRepository.save(owner);
    }

    /**
     * OWNER 상태 변경 (관리자 전용: 거절/정지/탈퇴 등)
     */
    public Users updateOwnerStatus(Long ownerId, OwnerStatus status) {
        Users owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 OWNER 사용자를 찾을 수 없습니다."));

        if (owner.getRole() != UserRole.OWNER) {
            throw new IllegalArgumentException("해당 사용자는 OWNER 권한이 아닙니다.");
        }

        owner.setOwnerStatus(status);
        return userRepository.save(owner);
    }
}
