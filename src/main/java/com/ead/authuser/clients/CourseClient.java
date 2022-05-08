package com.ead.authuser.clients;

import com.ead.authuser.dtos.CourseDto;
import com.ead.authuser.dtos.ResponsePageDto;
import com.ead.authuser.services.UtilsService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Log4j2
@Component
public class CourseClient {

    private final RestTemplate restTemplate;
    private final UtilsService utilsService;

    @Value("${ead.api.url.course}")
    private String REQUEST_URL_COURSE;

    public CourseClient(RestTemplate restTemplate, UtilsService utilsService) {
        this.restTemplate = restTemplate;
        this.utilsService = utilsService;
    }

//    @Retry(name = "retryInstance", fallbackMethod = "retryfallback")
    @CircuitBreaker(name = "circuitbreakerInstance")
    public Page<CourseDto> getAllCoursesByUser(UUID userId, Pageable pageable) {
        List<CourseDto> fetchResult = null;
        ResponseEntity<ResponsePageDto<CourseDto>> result = null;
        String url = REQUEST_URL_COURSE + utilsService.createUrl(userId, pageable);

        log.debug("Request URL: {}", url);
        log.info("Request URL: {}", url);
        try {
            ParameterizedTypeReference<ResponsePageDto<CourseDto>> responseType = new ParameterizedTypeReference<>() {
            };
            result = restTemplate.exchange(url, HttpMethod.GET, null, responseType);
            fetchResult = result.getBody().getContent();
            log.debug("Response Number of elements: {}", fetchResult.size());
        } catch (HttpStatusCodeException e) {
            log.error("Error request /api/v1/courses {}", e);
        }
        log.info("Ending request /api/v1/courses userId {}", userId);
        return result.getBody();
    }

    public Page<CourseDto> retryfallback(UUID userId, Pageable pageable, Throwable t) {
        log.error("Inside retryfallback, cause - {}", t.toString());
        List<CourseDto> searchResult = new ArrayList<>();
        return new PageImpl<>(searchResult);
    }

    public Page<CourseDto> circuitrbreakerfallback(UUID userId, Pageable pageable, Throwable t) {
        log.error("Inside circuitrbreakerfallback, cause - {}", t.toString());
        List<CourseDto> searchResult = new ArrayList<>();
        return new PageImpl<>(searchResult);
    }
}
