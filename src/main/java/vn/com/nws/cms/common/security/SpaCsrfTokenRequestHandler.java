package vn.com.nws.cms.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

public class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
        /*
         * Always use CsrfTokenRequestAttributeHandler to provide CSRF token as a request attribute.
         * This is necessary for Spring Security to populate the token in the session/cookie.
         */
        super.handle(request, response, csrfToken);
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        /*
         * If the request contains a Bearer token, we consider it safe from CSRF.
         * This makes CSRF "optional" when a Bearer token is used.
         */
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return csrfToken.getToken();
        }

        /*
         * Otherwise, fall back to the default behavior (checking headers or parameters).
         */
        return super.resolveCsrfTokenValue(request, csrfToken);
    }
}
