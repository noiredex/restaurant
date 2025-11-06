package choplan.db.application.properties.choplan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import choplan.db.application.properties.choplan.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화
            .csrf(csrf -> csrf.disable())

            // 요청별 권한 설정
            .authorizeHttpRequests(auth -> auth
                // 회원가입 / 로그인은 모두 접근 허용
                .requestMatchers(
                    "/auth/admin/signup", "/auth/admin/login",
                    "/auth/customer/signup", "/auth/customer/login",
                    "/auth/owner/signup", "/auth/owner/login"
                ).permitAll()

                // 관리자(Admin) 전용
                .requestMatchers("/admin/**", "/auth/admin/**").hasRole("ADMIN")

                // 고객(Customer) 전용
                .requestMatchers("/customer/**", "/auth/customer/**").hasRole("CUSTOMER")

                // 점주(Owner) 전용
                .requestMatchers("/owner/**", "/auth/owner/**").hasRole("OWNER")

                // 나머지는 인증 필요
                .anyRequest().authenticated()
            )

            // 예외 처리 핸들러 등록
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            )

            // JWT 필터 등록
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            // 기본 로그인 폼 비활성화
            .formLogin(form -> form.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"인증이 필요합니다.\"}");
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"접근 권한이 없습니다.\"}");
        };
    }
}
