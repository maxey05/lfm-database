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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PersonListTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;

    @BeforeEach
    void seedTwentyFivePeople() {
        personRepository.deleteAll();
        for (int i = 0; i < 25; i++) {
            Person person = new Person();
            person.setFirstName("First" + i);
            person.setLastName(String.format("Last%02d", i));
            person.setEmail("person" + i + "@example.com");
            person.setContactNumber(String.format("+63917%07d", i));
            person.setGender(Gender.values()[i % Gender.values().length]);
            personRepository.save(person);
        }
    }

    @Test
    void theListPageShowsTheFourBoothColumns() throws Exception {
        mockMvc.perform(get("/people").with(user("viewer").roles("VIEWER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Complete Name")))
                .andExpect(content().string(containsString("Email")))
                .andExpect(content().string(containsString("Contact Number")))
                .andExpect(content().string(containsString("Gender")))
                .andExpect(content().string(containsString("First0 Last00")));
    }

    @Test
    void theFirstPageShowsTwentyRowsAndANextLink() throws Exception {
        mockMvc.perform(get("/people").with(user("viewer").roles("VIEWER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Page 1 of 2")))
                .andExpect(content().string(containsString("Next")));
    }

    @Test
    void theSecondPageShowsTheRemainingRowsAndAPreviousLink() throws Exception {
        mockMvc.perform(get("/people").param("page", "1").with(user("viewer").roles("VIEWER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Page 2 of 2")))
                .andExpect(content().string(containsString("Previous")))
                .andExpect(content().string(containsString("First24 Last24")));
    }

    @Test
    void theFragmentEndpointReturnsOnlyTheTableNotTheFullPage() throws Exception {
        mockMvc.perform(get("/people/table").with(user("viewer").roles("VIEWER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Complete Name")))
                .andExpect(content().string(not(containsString("<html"))));
    }

    @Test
    void anUnauthenticatedRequestForTheFragmentIsRejected() throws Exception {
        mockMvc.perform(get("/people/table"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void anEmptyTableShowsThePlaceholderMessage() throws Exception {
        personRepository.deleteAll();

        mockMvc.perform(get("/people").with(user("viewer").roles("VIEWER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No records yet.")));
    }
}
