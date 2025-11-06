package choplan.db.application.properties.choplan.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 프로젝트 전역 예외 처리 클래스
 * - DTO 유효성 검사 (@Valid)
 * - IllegalArgumentException
 * - RuntimeException
 * - 기타 모든 예외
 * 
 * 모든 응답은 일관된 JSON 형식으로 반환됨:
 * {
 *   "status": 400,
 *   "message": "비밀번호는 최소 10자리 이상이어야 합니다.",
 *   "errors": { "password": "비밀번호는 최소 10자리 이상이어야 합니다." }
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * DTO 유효성 검사 실패 시 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        String firstErrorMessage = fieldErrors.values().stream().findFirst().orElse("요청값이 올바르지 않습니다.");

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", firstErrorMessage); // 첫 번째 에러 메시지를 상단 표시용으로
        body.put("errors", fieldErrors);       // 각 필드별 에러는 errors에 JSON으로 담기

        System.err.println("[VALIDATION ERROR] " + fieldErrors); // 디버깅용 로그

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * IllegalArgumentException 처리 (주로 잘못된 요청)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", ex.getMessage());
        body.put("errors", null);

        System.err.println("[BAD REQUEST] " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * RuntimeException 처리 (예상치 못한 로직 오류)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("message", "서버 실행 중 오류가 발생했습니다.");
        body.put("errors", Map.of("exception", ex.getMessage()));

        System.err.println("[RUNTIME ERROR] " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    /**
     * 그 외 모든 예외 처리 (예상치 못한 서버 에러)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("message", "서버에 알 수 없는 오류가 발생했습니다.");
        body.put("errors", Map.of("exception", ex.getMessage()));

        System.err.println("[GLOBAL EXCEPTION] " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
