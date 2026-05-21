package com.bookmyshow.booking.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

@Slf4j
@Component
public class BearerTokenForwardingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        if (request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION) == null) {
            String inboundAuth = currentRequestAuthHeader();
            if (inboundAuth != null) {
                request.getHeaders().add(HttpHeaders.AUTHORIZATION, inboundAuth);
            } else {
                log.debug("No Authorization header on inbound request; outbound call to {} {} will be unauthenticated",
                        request.getMethod(), request.getURI());
            }
        }
        return execution.execute(request, body);
    }

    private static String currentRequestAuthHeader() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }
}
