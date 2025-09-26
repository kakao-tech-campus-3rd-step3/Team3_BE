package com.shootdoori.match.resolver;

import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.UnauthorizedException;
import com.shootdoori.match.repository.ProfileRepository;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final ProfileRepository profileRepository;

    public LoginUserArgumentResolver(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
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
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            throw new UnauthorizedException("인증 정보가 유효하지 않습니다.");
        }

        Long userId = (Long) authentication.getPrincipal();

        return profileRepository.findById(userId)
            .orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));
    }
}