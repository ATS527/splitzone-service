package com.example.splitzone.service.shared.exception

import com.example.splitzone.service.shared.exception.filter.RequestIdFilter
import com.example.splitzone.service.shared.exception.types.ResourceNotFoundException
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalExceptionHandlerTest.TestController::class)
class GlobalExceptionHandlerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should return structured response for domain exception`() {
        mockMvc.perform(get("/test/not-found/42").header(RequestIdFilter.REQUEST_ID_HEADER, "req-123"))
            .andExpect(status().isNotFound)
            .andExpect(header().string(RequestIdFilter.REQUEST_ID_HEADER, "req-123"))
            .andExpect(jsonPath("$.code", equalTo("RESOURCE_NOT_FOUND")))
            .andExpect(jsonPath("$.requestId", equalTo("req-123")))
            .andExpect(jsonPath("$.path", equalTo("/test/not-found/42")))
    }

    @Test
    fun `should return validation errors for invalid body`() {
        mockMvc.perform(
            post("/test/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":""}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(header().exists(RequestIdFilter.REQUEST_ID_HEADER))
            .andExpect(jsonPath("$.code", equalTo("VALIDATION_FAILED")))
            .andExpect(jsonPath("$.fieldErrors[0].field", equalTo("name")))
    }

    @Test
    fun `should return generic payload for unexpected exception`() {
        mockMvc.perform(get("/test/crash"))
            .andExpect(status().isInternalServerError)
            .andExpect(header().exists(RequestIdFilter.REQUEST_ID_HEADER))
            .andExpect(jsonPath("$.code", equalTo("INTERNAL_SERVER_ERROR")))
    }

    @RestController
    @Validated
    @RequestMapping("/test")
    class TestController {

        @GetMapping("/not-found/{id}")
        fun notFound(@PathVariable id: String): String {
            throw ResourceNotFoundException("Expense", id)
        }

        @PostMapping("/validate")
        fun validate(@Valid @RequestBody request: CreateRequest): String = request.name

        @GetMapping("/crash")
        fun crash(): String {
            error("boom")
        }
    }

    data class CreateRequest(
        @field:NotBlank(message = "name must not be blank")
        val name: String
    )
}
