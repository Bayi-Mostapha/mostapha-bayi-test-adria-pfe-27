package com.bank.transactionservice.web.rest;

/**
 * Error body returned by the global handler for every failed request.
 */
public record ErrorResponse(String timestamp, int status, String error, String message) {
}
