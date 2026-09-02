package org.lfm.database.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SecurityConfigTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anUnauthenticatedRequestForPeopleRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/people"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void theLoginPageIsPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"password\"")));
    }

    @Test
    void theHealthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void theSeededAdminCanSignIn() throws Exception {
        mockMvc.perform(formLogin("/login").user("admin").password("ChangeMe123!"))
                .andExpect(authenticated().withRoles("ADMIN"))
                .andExpect(redirectedUrl("/people"));
    }

    @Test
    void aRealLoginPutsAnAppUserPrincipalInTheContext() throws Exception {
        mockMvc.perform(formLogin("/login").user("admin").password("ChangeMe123!"))
                .andExpect(authenticated().withAuthentication(authentication -> {
                    AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
                    org.assertj.core.api.Assertions.assertThat(principal.getId()).isNotNull();
                    org.assertj.core.api.Assertions.assertThat(principal.getFullName())
                            .isEqualTo("LFM Administrator");
                    org.assertj.core.api.Assertions.assertThat(principal.getRole()).isEqualTo(Role.ADMIN);
                }));
    }

    @Test
    void aWrongPasswordIsRejected() throws Exception {
        mockMvc.perform(formLogin("/login").user("admin").password("wrong"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void anUnknownUserIsRejected() throws Exception {
        mockMvc.perform(formLogin("/login").user("nobody").password("ChangeMe123!"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void anAuthenticatedViewerCanOpenThePeoplePage() throws Exception {
        mockMvc.perform(get("/people").with(user("viewer").roles("VIEWER")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("People")));
    }

    @Test
    void theRootPathRedirectsToPeople() throws Exception {
        mockMvc.perform(get("/").with(user("viewer").roles("VIEWER")))
                .andExpect(redirectedUrl("/people"));
    }

    @Test
    void aViewerIsRefusedTheAdminArea() throws Exception {
        mockMvc.perform(get("/admin/users").with(user("viewer").roles("VIEWER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void anAdminInheritsViewerAccessThroughTheRoleHierarchy() throws Exception {
        mockMvc.perform(get("/people").with(user("boss").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void signingOutClearsTheSession() throws Exception {
        mockMvc.perform(logout("/logout"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    void aStateChangingPostWithoutACsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/people").with(user("staff").roles("STAFF")))
                .andExpect(status().isForbidden());
    }

    @Test
    void theWebhookPathIsExemptFromCsrfAndAuthentication() throws Exception {
        mockMvc.perform(post("/api/forms/webhook").contentType("application/json").content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void aValidCsrfTokenLetsTheSameRequestThrough() throws Exception {
        mockMvc.perform(post("/people").with(user("staff").roles("STAFF")).with(csrf())
                        .param("firstName", "")
                        .param("lastName", ""))
                .andExpect(status().isOk());
    }
}
