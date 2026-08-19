package pl.omnisport.api.exception;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ErrorResponse {
    private LocalDateTime time;
    private int status;
    private String error;
    private String message;

    public ErrorResponse(int status, String error, String message) {
        this.time = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
    }
}
