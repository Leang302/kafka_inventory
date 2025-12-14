package com.leang.orderservice.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leang.orderservice.exception.BadRequestException;
import com.leang.orderservice.exception.ExceptionDetail;
import com.leang.orderservice.exception.NotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder errorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        ExceptionDetail detail = null;

        if (response.body() != null) {
            try (InputStream bodyIs = response.body().asInputStream()) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false
                );
                detail = objectMapper.readValue(bodyIs, ExceptionDetail.class);
            } catch (IOException e) {
                return new RuntimeException("Failed to parse error response body", e);
            }
        }

        return switch (response.status()) {
            case 400 -> new BadRequestException(
                    detail != null && detail.getDetail() != null
                            ? detail.getDetail()
                            : "Bad Request"
            );
            case 404 -> new NotFoundException(
                    detail != null && detail.getDetail() != null
                            ? detail.getDetail()
                            : "Not Found"
            );
            default -> errorDecoder.decode(methodKey, response);
        };
    }
}
