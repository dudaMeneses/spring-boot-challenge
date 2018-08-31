package com.n26.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NO_CONTENT, reason = "transaction older than 60 seconds.")
public class OldMessageException extends RuntimeException {
}
