package kz.app.appstore.dto.auth;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class ApiErrorDto {
    private int status;
    private String message;
    private List<String> errors;
}