package org.lfm.database.person;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PersonWriteTest
{
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;

    @BeforeEach
    void clear()
    {
        personRepository.deleteAll();
    }

    private Long existingPerson()
    {
        Person person = new Person();
        person.setFirstName("Maria");
        person.setLastName("Santos");
        person.setEmail("maria.santos@example.com");
        person.setContactNumber("+639170000001");
        return personRepository.save(person).getId();
    }

    @Test
    void theAddFormIsServedAsAFragment() throws Exception
    {
        mockMvc.perform(get("/people/new").with(user("staff").roles("STAFF")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Add Person")))
                .andExpect(content().string(containsString("First name")))
                .andExpect(content().string(not(containsString("<html"))));
    }

    @Test
    void aViewerCannotOpenTheAddForm() throws Exception
    {
        mockMvc.perform(get("/people/new").with(user("v").roles("VIEWER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void staffCanCreateAPerson() throws Exception
    {
        mockMvc.perform(post("/people").with(user("staff").roles("STAFF")).with(csrf())
                        .param("firstName", "Juan")
                        .param("lastName", "Dela Cruz")
                        .param("email", "  JUAN.DELACRUZ@Example.COM  ")
                        .param("contactNumber", "0917 123 4567")
                        .param("inDgroup", "true")
                        .param("dgroupLeaderName", "Kuya Jed"))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Trigger", "refresh-people"))
                .andExpect(content().string(containsString("Juan Dela Cruz")));

        Optional<Person> saved = personRepository.findByEmail("juan.delacruz@example.com");

        assertThat(saved).isPresent();
        assertThat(saved.get().getContactNumber()).isEqualTo("+639171234567");
        assertThat(saved.get().isInDgroup()).isTrue();
        assertThat(saved.get().getSource()).isEqualTo(PersonSource.MANUAL);
        assertThat(saved.get().getCreatedAt()).isNotNull();
    }

    @Test
    void aViewerCannotCreateAPerson() throws Exception
    {
        mockMvc.perform(post("/people").with(user("v").roles("VIEWER")).with(csrf())
                        .param("firstName", "Juan")
                        .param("lastName", "Dela Cruz"))
                .andExpect(status().isForbidden());

        assertThat(personRepository.count()).isZero();
    }

    @Test
    void aMissingNameIsRejectedAndNothingIsSaved() throws Exception
    {
        mockMvc.perform(post("/people").with(user("staff").roles("STAFF")).with(csrf())
                        .param("firstName", "")
                        .param("lastName", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("First name is required")))
                .andExpect(content().string(containsString("Last name is required")));

        assertThat(personRepository.count()).isZero();
    }

    @Test
    void aDuplicateEmailIsRejectedWithAReadableMessage() throws Exception
    {
        existingPerson();

        mockMvc.perform(post("/people").with(user("staff").roles("STAFF")).with(csrf())
                        .param("firstName", "Maria")
                        .param("lastName", "Santos")
                        .param("email", "MARIA.SANTOS@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Someone already has that email address")));

        assertThat(personRepository.count()).isEqualTo(1);
    }

    @Test
    void staffCanEditAPersonAndTheChangeIsPersisted() throws Exception
    {
        Long id = existingPerson();

        mockMvc.perform(get("/people/{id}/edit", id).with(user("staff").roles("STAFF")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Edit Person")))
                .andExpect(content().string(containsString("Maria")));

        mockMvc.perform(put("/people/{id}", id).with(user("staff").roles("STAFF")).with(csrf())
                        .param("firstName", "Maria")
                        .param("lastName", "Santos-Reyes")
                        .param("email", "maria.santos@example.com")
                        .param("nickname", "Mars"))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Trigger", "refresh-people"))
                .andExpect(content().string(containsString("Maria Santos-Reyes")));

        Person updated = personRepository.findById(id).orElseThrow();

        assertThat(updated.getLastName()).isEqualTo("Santos-Reyes");
        assertThat(updated.getNickname()).isEqualTo("Mars");
        assertThat(personRepository.count()).isEqualTo(1);
    }

    @Test
    void keepingYourOwnEmailOnAnEditIsNotADuplicate() throws Exception
    {
        Long id = existingPerson();

        mockMvc.perform(put("/people/{id}", id).with(user("staff").roles("STAFF")).with(csrf())
                        .param("firstName", "Maria")
                        .param("lastName", "Santos")
                        .param("email", "maria.santos@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Someone already has that email address"))));
    }

    @Test
    void anAdminCanArchiveAPersonAndTheRowLeavesTheDefaultList() throws Exception
    {
        Long id = existingPerson();

        mockMvc.perform(post("/people/{id}/archive", id).with(user("boss").roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Trigger", "refresh-people"))
                .andExpect(content().string(containsString("This record is archived.")));

        assertThat(personRepository.findById(id).orElseThrow().isArchived()).isTrue();

        mockMvc.perform(get("/people/table").with(user("v").roles("VIEWER")))
                .andExpect(content().string(not(containsString("Maria Santos"))));

        mockMvc.perform(post("/people/{id}/restore", id).with(user("boss").roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk());

        assertThat(personRepository.findById(id).orElseThrow().isArchived()).isFalse();
    }

    @Test
    void staffCannotArchive() throws Exception
    {
        Long id = existingPerson();

        mockMvc.perform(post("/people/{id}/archive", id).with(user("staff").roles("STAFF")).with(csrf()))
                .andExpect(status().isForbidden());

        assertThat(personRepository.findById(id).orElseThrow().isArchived()).isFalse();
    }

    @Test
    void aWriteWithoutACsrfTokenIsRejected() throws Exception
    {
        mockMvc.perform(post("/people").with(user("staff").roles("STAFF"))
                        .param("firstName", "Juan")
                        .param("lastName", "Dela Cruz"))
                .andExpect(status().isForbidden());

        assertThat(personRepository.count()).isZero();
    }

    @Test
    void editingSomethingThatDoesNotExistReturnsNotFound() throws Exception
    {
        mockMvc.perform(get("/people/{id}/edit", 999999L).with(user("staff").roles("STAFF")))
                .andExpect(status().isNotFound());
    }
}
