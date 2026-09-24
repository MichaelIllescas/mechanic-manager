package com.mechanicmanager.shared.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    enum Kind { A, B }

    record Payload(Integer amount) {
    }

    @RestController
    static class ProbeController {

        @PostMapping("/probe/body")
        String body(@RequestBody Payload payload) {
            return "ok";
        }

        @GetMapping("/probe/enum")
        String enumParam(@RequestParam Kind kind) {
            return "ok";
        }

        @GetMapping("/probe/conflict")
        String conflict() {
            throw new DataIntegrityViolationException(
                    "could not execute statement", new RuntimeException("Duplicate entry for uq_secret_table"));
        }

        @GetMapping("/probe/boom")
        String boom() {
            throw new IllegalStateException("detalle interno que no debe salir");
        }

        @GetMapping("/probe/app")
        String app() {
            throw new ResourceNotFoundException("No existe");
        }

        @GetMapping("/probe/auth")
        String auth() {
            throw new BadCredentialsException("x");
        }
    }

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ProbeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void malformedJsonIsABadRequestWithTheCommonFormat() throws Exception {
        mvc.perform(post("/probe/body").contentType(MediaType.APPLICATION_JSON).content("{ no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
    }

    @Test
    void wrongTypeInsideTheJsonIsABadRequest() throws Exception {
        mvc.perform(post("/probe/body").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": \"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
    }

    @Test
    void invalidEnumParameterIsABadRequestNamingTheParameter() throws Exception {
        mvc.perform(get("/probe/enum").param("kind", "XYZ"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.message").value("El valor del parámetro 'kind' no es válido"));
    }

    @Test
    void missingRequiredParameterIsABadRequest() throws Exception {
        mvc.perform(get("/probe/enum"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    void databaseConstraintViolationIsAConflictWithoutInternalDetails() throws Exception {
        mvc.perform(get("/probe/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DATA_CONFLICT"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("uq_secret_table"))));
    }

    @Test
    void unexpectedErrorIsAGenericInternalErrorWithoutLeakingTheCause() throws Exception {
        mvc.perform(get("/probe/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("detalle interno"))));
    }

    @Test
    void frameworkErrorsKeepTheirOwnStatus() throws Exception {
        mvc.perform(post("/probe/enum"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("REQUEST_ERROR"));
    }

    @Test
    void unsupportedContentTypeKeepsItsStatus() throws Exception {
        mvc.perform(post("/probe/body").contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("REQUEST_ERROR"));
    }

    @Test
    void applicationExceptionsStillUseTheirOwnCodeAndStatus() throws Exception {
        mvc.perform(get("/probe/app"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void authenticationExceptionIsAnUnauthorizedNotAServerError() throws Exception {
        mvc.perform(get("/probe/auth"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }
}
