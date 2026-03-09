package vn.com.nws.cms.common.security;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.HtmlUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class XssRequestWrapper extends HttpServletRequestWrapper {

    private byte[] body;

    public XssRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        // For JSON/Body sanitization
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            String originalBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            String sanitizedBody = sanitize(originalBody);
            this.body = sanitizedBody.getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (body == null) {
            return super.getInputStream();
        }
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }
        };
    }

    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values == null) {
            return null;
        }
        return Arrays.stream(values)
                .map(this::sanitize)
                .toArray(String[]::new);
    }

    @Override
    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        return sanitize(value);
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return sanitize(value);
    }

    private String sanitize(String value) {
        if (value == null) {
            return null;
        }
        // Basic XSS prevention by escaping HTML characters
        return HtmlUtils.htmlEscape(value);
    }
}
