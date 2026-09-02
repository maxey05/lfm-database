package org.lfm.database.person;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfm.database.satellite.ChurchSatellite;
import org.lfm.database.satellite.ChurchSatelliteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PersonDetailTest
{
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ChurchSatelliteRepository satelliteRepository;

    private Long personId;

    @BeforeEach
    void seedOnePerson()
    {
        personRepository.deleteAll();

        ChurchSatellite ortigas = satelliteRepository.findByName("Ortigas").orElseThrow();

        Person person = new Person();
        person.setFirstName("Maria");
        person.setMiddleName("Lopez");
        person.setLastName("Santos");
        person.setNickname("Mars");
        person.setEmail("maria.santos@example.com");
        person.setContactNumber("+639170000001");
        person.setFacebookName("Maria L Santos");
        person.setDateOfBirth(LocalDate.of(1995, 4, 12));
        person.setGender(Gender.FEMALE);
        person.setCivilStatus(CivilStatus.SINGLE);
        person.setChurchSatellite(ortigas);
        person.setInDgroup(true);
        person.setDgroupLeaderName("Kuya Jed");
        person.setDgroupLeaderContact("+639180000001");
        person.setLfmGroupLeaderName("Ate Mimi");

        personId = personRepository.save(person).getId();
    }

    @Test
    void theModalShowsEveryFieldOfTheRecord() throws Exception
    {
        mockMvc.perform(get("/people/{id}/modal", personId).with(user("v").roles("VIEWER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Maria Lopez Santos")))
                .andExpect(content().string(containsString("Mars")))
                .andExpect(content().string(containsString("maria.santos@example.com")))
                .andExpect(content().string(containsString("+639170000001")))
                .andExpect(content().string(containsString("Maria L Santos")))
                .andExpect(content().string(containsString("12 Apr 1995")))
                .andExpect(content().string(containsString("Female")))
                .andExpect(content().string(containsString("Single")))
                .andExpect(content().string(containsString("Ortigas")))
                .andExpect(content().string(containsString("Kuya Jed")))
                .andExpect(content().string(containsString("+639180000001")))
                .andExpect(content().string(containsString("Ate Mimi")))
                .andExpect(content().string(containsString("Manual")));
    }

    @Test
    void theModalIsAFragmentNotAWholePage() throws Exception
    {
        mockMvc.perform(get("/people/{id}/modal", personId).with(user("v").roles("VIEWER")))
                .andExpect(content().string(not(containsString("<html"))));
    }

    @Test
    void theRecordHasAShareablePermalinkPage() throws Exception
    {
        mockMvc.perform(get("/people/{id}", personId).with(user("v").roles("VIEWER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<html")))
                .andExpect(content().string(containsString("Maria Lopez Santos")))
                .andExpect(content().string(containsString("Ortigas")));
    }

    @Test
    void missingValuesRenderAsADashRatherThanBlank() throws Exception
    {
        Person sparse = new Person();
        sparse.setFirstName("Juan");
        sparse.setLastName("Dela Cruz");
        Long sparseId = personRepository.save(sparse).getId();

        mockMvc.perform(get("/people/{id}/modal", sparseId).with(user("v").roles("VIEWER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Juan Dela Cruz")))
                .andExpect(content().string(containsString("—")));
    }

    @Test
    void anUnknownIdReturnsNotFound() throws Exception
    {
        mockMvc.perform(get("/people/{id}/modal", 999999L).with(user("v").roles("VIEWER")))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/people/{id}", 999999L).with(user("v").roles("VIEWER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void anUnauthenticatedRequestForTheModalIsRedirectedToLogin() throws Exception
    {
        mockMvc.perform(get("/people/{id}/modal", personId))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void theTableRowsLinkToTheModal() throws Exception
    {
        mockMvc.perform(get("/people/table").with(user("v").roles("VIEWER")))
                .andExpect(content().string(containsString("/people/" + personId + "/modal")))
                .andExpect(content().string(containsString("id=\"person-" + personId + "\"")));
    }
}
