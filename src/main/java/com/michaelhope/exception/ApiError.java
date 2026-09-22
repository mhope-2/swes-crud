package com.michaelhope.exception;

public record ApiError(int status, String message, String requestId) {
}
