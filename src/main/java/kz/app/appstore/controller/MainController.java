package kz.app.appstore.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.app.appstore.exception.NotFoundException;
import kz.app.appstore.exception.ShopServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/")
@ControllerAdvice
public class MainController {
    private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

    @GetMapping("/")
    public RedirectView rootToSwaggerUiRedirect() {
        return new RedirectView("/swagger-ui.html");
    }

    @PostMapping("/")
    public RedirectView rootToSwaggerUiRedirectPost() {
        return new RedirectView("/swagger-ui.html");
    }

    @ExceptionHandler({ShopServiceException.class, NotFoundException.class})
    public void handleNotFoundRequestExceptions(HttpServletResponse response,
                                                ShopServiceException ex,
                                                WebRequest webRequest) throws IOException {
        handleException(
                response,
                ex,
                HttpStatus.CONFLICT,
                webRequest,
                null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public void handleValidationException(HttpServletResponse response,
                                          MethodArgumentNotValidException ex,
                                          WebRequest webRequest) throws IOException {
        BindingResult bindingResult = ex.getBindingResult();
        List<FieldError> errors = bindingResult.getFieldErrors();
        Map<String, FieldErrorMessage> fields = new HashMap<>();
        if (!errors.isEmpty()) {
            for (FieldError error : errors) {
                fields.put(error.getField(), new FieldErrorMessage(
                        error.getDefaultMessage(),
                        error.getDefaultMessage()));
            }
        }
        handleException(
                response,
                ex,
                HttpStatus.BAD_REQUEST,
                webRequest,
                fields);
    }

    @ExceptionHandler(RuntimeException.class)
    public void handleRuntimeException(HttpServletResponse response,
                                       RuntimeException ex,
                                       WebRequest webRequest) throws IOException {
        LOG.error("EXP", ex);
        handleException(
                response,
                ex,
                HttpStatus.INTERNAL_SERVER_ERROR,
                webRequest,
                null);
    }

    @ExceptionHandler
    public void handleAllException(HttpServletResponse response,
                                   Exception ex,
                                   WebRequest webRequest) throws IOException {
        LOG.error("EXP", ex);
        handleException(
                response,
                ex,
                HttpStatus.INTERNAL_SERVER_ERROR,
                webRequest,
                null);
    }

    private void handleException(HttpServletResponse response,
                                 Exception ex,
                                 HttpStatus httpStatus,
                                 WebRequest webRequest,
                                 Map<String, FieldErrorMessage> fields) throws IOException {

        HandledExceptionResponse exceptionResponse = new HandledExceptionResponse();
        exceptionResponse.setTimestamp(new Date());
        exceptionResponse.setUrl(webRequest.toString());
        exceptionResponse.setErrorCode(String.valueOf(httpStatus.value()));
        exceptionResponse.setHttpStatusCode(httpStatus.value());
        exceptionResponse.setHttpStatusDesc(httpStatus.getReasonPhrase());

        Map<String, String> messageLang = new HashMap<>();
        if (fields != null && !fields.isEmpty()) {
            exceptionResponse.setFields(fields);
            messageLang.put("ru", "Валидация не успешна");
            messageLang.put("kk", "Валидация сәтсіз жүзеге асты");
        } else {
            messageLang.put("ru", ex.getMessage());
            messageLang.put("kk", ex.getMessage());
        }
        exceptionResponse.setMessageLang(messageLang);

        LOG.error(ex.getMessage());
        response.setStatus(httpStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(exceptionResponse));
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private class HandledExceptionResponse {
        private Date timestamp;
        private String url;
        private String errorCode;
        private Map<String, String> messageLang;
        private int httpStatusCode;
        private String httpStatusDesc;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Map<String, FieldErrorMessage> fields;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @AllArgsConstructor
    private class FieldErrorMessage {
        private String ru;
        private String kk;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @SuppressWarnings("unused")
    public ResponseEntity<Object> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return ResponseEntity
                .status(HttpStatus.EXPECTATION_FAILED)
                .body("One or more files are too large!");
    }
}
