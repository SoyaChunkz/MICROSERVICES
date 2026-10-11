package com.message.message.dto;

/**
 *
 * @param accountNumber
 * @param type
 * @param status
 */
public record CommunicationStatusDto(Long accountNumber, String type, String status) {}
