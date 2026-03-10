package org.aibles.intellihireresume.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.BaseResponse;
import org.aibles.intellihireresume.dto.FieldError;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class HeaderAuthFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");

        if (userId == null || userId.isBlank()) {
            log.warn("Request rejected: missing X-User-Id header for path={}", request.getRequestURI());
            sendForbidden(response, "Missing required header: X-User-Id");
            return;
        }

        String userRole = request.getHeader("X-User-Role");
        if (userRole == null || userRole.isBlank()) {
            userRole = "ROLE_USER";
        }

        request.setAttribute("userId", userId);
        request.setAttribute("userRole", userRole);

        filterChain.doFilter(request, response);
    }

    private void sendForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        List<FieldError> errors = List.of(
                FieldError.builder()
                        .field("X-User-Id")
                        .message(message)
                        .rejectedValue(null)
                        .build()
        );
        BaseResponse<Void> body = BaseResponse.error("COM_004", errors);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
