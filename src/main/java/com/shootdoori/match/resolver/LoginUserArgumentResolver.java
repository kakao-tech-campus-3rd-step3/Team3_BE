package com.shootdoori.match.resolver;

import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.UnauthorizedException;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final ProfileRepository profileRepository;
    private final JwtUtil jwtUtil;

    public LoginUserArgumentResolver(final ProfileRepository profileRepository, final JwtUtil jwtUtil) {
        this.profileRepository = profileRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginUser.class) &&
            parameter.getParameterType().equals(User.class);
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String token = jwtUtil.extractToken(request);

        if (token == null || !jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("인증 토큰이 존재하지 않습니다.");
        }

        Claims claims = jwtUtil.getClaims(token);
        Long userId = Long.valueOf(claims.getSubject());

        return profileRepository.findById(userId)
            .orElseThrow(() -> new UnauthorizedException("토큰에 해당하는 사용자를 찾을 수 없습니다."));
    }
}
