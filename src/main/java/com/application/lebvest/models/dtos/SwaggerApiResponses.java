package com.application.lebvest.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

public class SwaggerApiResponses {

    @Schema(
            name = "InvestorApplicationResponse",
            description = "Successful response for a new investor application",
            example = """
            {
              "statusCode": 201,
              "timestamp": "2026-04-11T16:45:00Z",
              "data": {
                "identityDocumentPresignedUrl": "https://s3.amazonaws.com/lebvest/temp/id_123...",
                "addressProofDocumentPresignedUrl": "https://s3.amazonaws.com/lebvest/temp/addr_123..."
              }
            }
            """
    )
    public static class InvestorApplicationResponse extends ApiResponseDto<InvestorApplicationResponseDto> {
    }

    @Schema(
            name = "InvestorApplicationConflictResponse",
            description = "Conflict response when an investor application with the same email already exists",
            example = """
            {
              "statusCode": 409,
              "timestamp": "2026-04-11T16:45:00Z",
              "error": {
                "message": "InvestorApplication already exists with email = investor@example.com"
              }
            }
            """
    )
    public static class InvestorApplicationConflictResponse extends ApiResponseDto<Void> {
    }

    @Schema(
            name = "InvestorApplicationDecisionResponse",
            description = "Successful response for approving or rejecting an investor application",
            example = """
            {
              "statusCode": 200,
              "timestamp": "2026-04-13T16:45:00Z",
              "data": {
                "applicationId": 1,
                "email": "investor@example.com",
                "applicationStatus": "ACCEPTED"
              }
            }
            """
    )
    public static class InvestorApplicationDecisionResponse extends ApiResponseDto<InvestorApplicationDecisionResponseDto> {
    }

    @Schema(
            name = "InvestorSetPasswordResponse",
            description = "Successful response for investor password setup",
            example = """
            {
              "statusCode": 200,
              "timestamp": "2026-04-13T16:45:00Z",
              "data": {
                "accountId": 1,
                "email": "investor@example.com"
              }
            }
            """
    )
    public static class InvestorSetPasswordResponse extends ApiResponseDto<InvestorSetPasswordResponseDto> {
    }

    @Schema(
            name = "InvestorLoginResponse",
            description = "Successful investor login with JWT payload",
            example = """
            {
              "statusCode": 200,
              "timestamp": "2026-04-13T16:45:00Z",
              "data": {
                "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                "tokenType": "Bearer",
                "expiresAt": "2026-04-13T17:00:00Z",
                "email": "investor@example.com",
                "role": "INVESTOR"
              }
            }
            """
    )
    public static class InvestorLoginResponse extends ApiResponseDto<InvestorAuthTokenPayloadDto> {
    }

    @Schema(
            name = "UnauthorizedResponse",
            description = "Unauthorized — invalid credentials or missing authentication",
            example = """
            {
              "statusCode": 401,
              "timestamp": "2026-04-13T16:45:00Z",
              "error": {
                "message": "Invalid email or password"
              }
            }
            """
    )
    public static class UnauthorizedResponse extends ApiResponseDto<Void> {
    }

    @Schema(
            name = "BadRequestResponse",
            description = "Bad request response",
            example = """
            {
              "statusCode": 400,
              "timestamp": "2026-04-13T16:45:00Z",
              "error": {
                "message": "Set password token is invalid"
              }
            }
            """
    )
    public static class BadRequestResponse extends ApiResponseDto<Void> {
    }

    @Schema(
            name = "NotFoundResponse",
            description = "Resource not found response",
            example = """
            {
              "statusCode": 404,
              "timestamp": "2026-04-13T16:45:00Z",
              "error": {
                "message": "InvestorApplication not found with id = 10"
              }
            }
            """
    )
    public static class NotFoundResponse extends ApiResponseDto<Void> {
    }
}