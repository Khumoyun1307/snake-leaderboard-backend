package com.snakeleaderboard.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SubmitScoreRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void validRequest_hasNoViolations() {
        SubmitScoreRequest request = new SubmitScoreRequest(
                "player1",
                42,
                2,
                "MAP_SELECT",
                "NORMAL",
                26000L,
                "1.0.0"
        );

        Set<ConstraintViolation<SubmitScoreRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void blankPlayerName_isRejected() {
        SubmitScoreRequest request = new SubmitScoreRequest(
                "",
                42,
                2,
                "MAP_SELECT",
                "NORMAL",
                26000L,
                "1.0.0"
        );

        Set<ConstraintViolation<SubmitScoreRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("playerName"));
    }

    @Test
    void invalidPlayerNameCharacters_areRejected() {
        SubmitScoreRequest request = new SubmitScoreRequest(
                "player!@#",
                42,
                2,
                "MAP_SELECT",
                "NORMAL",
                26000L,
                "1.0.0"
        );

        Set<ConstraintViolation<SubmitScoreRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("playerName"));
    }

    @Test
    void scoreAboveMax_isRejected() {
        SubmitScoreRequest request = new SubmitScoreRequest(
                "player1",
                2_000_001,
                2,
                "MAP_SELECT",
                "NORMAL",
                26000L,
                "1.0.0"
        );

        Set<ConstraintViolation<SubmitScoreRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("score"));
    }

    @Test
    void blankDifficulty_isRejected() {
        SubmitScoreRequest request = new SubmitScoreRequest(
                "player1",
                42,
                2,
                "MAP_SELECT",
                "",
                26000L,
                "1.0.0"
        );

        Set<ConstraintViolation<SubmitScoreRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("difficulty"));
    }

    @Test
    void negativeMapId_isRejected() {
        SubmitScoreRequest request = new SubmitScoreRequest(
                "player1",
                42,
                -1,
                "MAP_SELECT",
                "NORMAL",
                26000L,
                "1.0.0"
        );

        Set<ConstraintViolation<SubmitScoreRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("mapId"));
    }

    @Test
    void blankMode_isRejected() {
        SubmitScoreRequest request = new SubmitScoreRequest(
                "player1",
                42,
                2,
                "",
                "NORMAL",
                26000L,
                "1.0.0"
        );

        Set<ConstraintViolation<SubmitScoreRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("mode"));
    }

    @Test
    void timeSurvivedTooHigh_isRejected() {
        SubmitScoreRequest request = new SubmitScoreRequest(
                "player1",
                42,
                2,
                "MAP_SELECT",
                "NORMAL",
                86_400_001L,
                "1.0.0"
        );

        Set<ConstraintViolation<SubmitScoreRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("timeSurvivedMs"));
    }

    @Test
    void playerNameTooLong_isRejected() {
        SubmitScoreRequest request = new SubmitScoreRequest(
                "player_name_with_25_chars",
                42,
                2,
                "MAP_SELECT",
                "NORMAL",
                26000L,
                "1.0.0"
        );

        Set<ConstraintViolation<SubmitScoreRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("playerName"));
    }

    @Test
    void modeTooLong_isRejected() {
        SubmitScoreRequest request = new SubmitScoreRequest(
                "player1",
                42,
                2,
                "M".repeat(33),
                "NORMAL",
                26000L,
                "1.0.0"
        );

        Set<ConstraintViolation<SubmitScoreRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("mode"));
    }

    @Test
    void difficultyTooLong_isRejected() {
        SubmitScoreRequest request = new SubmitScoreRequest(
                "player1",
                42,
                2,
                "MAP_SELECT",
                "D".repeat(33),
                26000L,
                "1.0.0"
        );

        Set<ConstraintViolation<SubmitScoreRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("difficulty"));
    }

    @Test
    void gameVersionTooLong_isRejected() {
        SubmitScoreRequest request = new SubmitScoreRequest(
                "player1",
                42,
                2,
                "MAP_SELECT",
                "NORMAL",
                26000L,
                "1".repeat(33)
        );

        Set<ConstraintViolation<SubmitScoreRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("gameVersion"));
    }
}
