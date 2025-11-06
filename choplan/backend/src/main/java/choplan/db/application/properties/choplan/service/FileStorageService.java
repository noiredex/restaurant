package choplan.db.application.properties.choplan.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class FileStorageService {

    private final String bucketName = "your-bucket-name"; // 실제 S3 버킷명
    private final String accessKey = "your-access-key";   // IAM 액세스 키
    private final String secretKey = "your-secret-key";   // IAM 시크릿 키
    private final Region region = Region.AP_NORTHEAST_2;  // 서울 리전

    private final S3Client s3Client;

    public FileStorageService() {
        this.s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .contentType(file.getContentType())
                        .build(),
                software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes())
        );

        // 업로드 후 접근 가능한 URL 반환
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + fileName;
    }
}
