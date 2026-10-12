package com.michaelhope.api.exception;

public record ApiError(int status, String message, String requestId) {
}
