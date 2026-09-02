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
class PersonSearchTest
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

    private Long ortigasId;

    @BeforeEach
    void seed()
    {
        personRepository.deleteAll();

        ChurchSatellite ortigas = satelliteRepository.findByName("Ortigas").orElseThrow();
        ChurchSatellite makati = satelliteRepository.findByName("Makati").orElseThrow();
        ortigasId = ortigas.getId();

        personRepository.save(person("Maria", "Santos", "maria.santos@example.com", "+639170000001",
                Gender.FEMALE, CivilStatus.SINGLE, ortigas, true, "Kuya Jed",
                LocalDate.now().withYear(1995), false));

        personRepository.save(person("Jose", "Reyes", "jose.reyes@example.com", "+639170000002",
                Gender.MALE, CivilStatus.MARRIED, makati, false, null,
                LocalDate.of(1990, 2, 14), false));

        personRepository.save(person("Ana", "Cruz", "ana.cruz@sample.org", "+639170000003",
                Gender.FEMALE, CivilStatus.SINGLE, ortigas, false, null,
                LocalDate.of(1988, 7, 3), false));

        personRepository.save(person("Zeno", "Archivado", "zeno.archivado@example.com", "+639170000004",
                Gender.MALE, CivilStatus.SINGLE, makati, true, "Ate Mimi",
                LocalDate.of(1992, 3, 9), true));
    }

    private Person person(String first, String last, String email, String contact, Gender gender,
                          CivilStatus civilStatus, ChurchSatellite satellite, boolean inDgroup,
                          String leader, LocalDate dob, boolean archived)
    {
        Person person = new Person();
        person.setFirstName(first);
        person.setLastName(last);
        person.setEmail(email);
        person.setContactNumber(contact);
        person.setGender(gender);
        person.setCivilStatus(civilStatus);
        person.setChurchSatellite(satellite);
        person.setInDgroup(inDgroup);
        person.setDgroupLeaderName(leader);
        person.setDateOfBirth(dob);
        person.setArchived(archived);
        return person;
    }

    @Test
    void theSearchBoxMatchesOnName() throws Exception
    {
        mockMvc.perform(get("/people/table").param("q", "sant").with(user("v").roles("VIEWER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Maria Santos")))
                .andExpect(content().string(not(containsString("Jose Reyes"))));
    }

    @Test
    void theSearchBoxMatchesOnEmailAndContactNumber() throws Exception
    {
        mockMvc.perform(get("/people/table").param("q", "sample.org").with(user("v").roles("VIEWER")))
                .andExpect(content().string(containsString("Ana Cruz")))
                .andExpect(content().string(not(containsString("Maria Santos"))));

        mockMvc.perform(get("/people/table").param("q", "0000002").with(user("v").roles("VIEWER")))
                .andExpect(content().string(containsString("Jose Reyes")))
                .andExpect(content().string(not(containsString("Ana Cruz"))));
    }

    @Test
    void searchIsCaseInsensitive() throws Exception
    {
        mockMvc.perform(get("/people/table").param("q", "MARIA").with(user("v").roles("VIEWER")))
                .andExpect(content().string(containsString("Maria Santos")));
    }

    @Test
    void aWildcardInTheSearchTermIsTreatedAsLiteralText() throws Exception
    {
        mockMvc.perform(get("/people/table").param("q", "%").with(user("v").roles("VIEWER")))
                .andExpect(content().string(containsString("No records match.")));
    }

    @Test
    void filteringByGenderNarrowsTheList() throws Exception
    {
        mockMvc.perform(get("/people/table").param("gender", "MALE").with(user("v").roles("VIEWER")))
                .andExpect(content().string(containsString("Jose Reyes")))
                .andExpect(content().string(not(containsString("Maria Santos"))));
    }

    @Test
    void filteringBySatelliteNarrowsTheList() throws Exception
    {
        mockMvc.perform(get("/people/table").param("satellite", ortigasId.toString())
                        .with(user("v").roles("VIEWER")))
                .andExpect(content().string(containsString("Maria Santos")))
                .andExpect(content().string(containsString("Ana Cruz")))
                .andExpect(content().string(not(containsString("Jose Reyes"))));
    }

    @Test
    void filteringByDgroupMembershipNarrowsTheList() throws Exception
    {
        mockMvc.perform(get("/people/table").param("inDgroup", "true").with(user("v").roles("VIEWER")))
                .andExpect(content().string(containsString("Maria Santos")))
                .andExpect(content().string(not(containsString("Ana Cruz"))));
    }

    @Test
    void archivedRecordsAreHiddenUnlessAskedFor() throws Exception
    {
        mockMvc.perform(get("/people/table").with(user("v").roles("VIEWER")))
                .andExpect(content().string(not(containsString("Zeno Archivado"))));

        mockMvc.perform(get("/people/table").param("includeArchived", "true").with(user("v").roles("VIEWER")))
                .andExpect(content().string(containsString("Zeno Archivado")));
    }

    @Test
    void sortingDescendingReversesTheOrder() throws Exception
    {
        String ascending = mockMvc.perform(get("/people/table").param("sort", "lastName,asc")
                        .with(user("v").roles("VIEWER")))
                .andReturn().getResponse().getContentAsString();

        String descending = mockMvc.perform(get("/people/table").param("sort", "lastName,desc")
                        .with(user("v").roles("VIEWER")))
                .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(ascending.indexOf("Ana Cruz"))
                .isLessThan(ascending.indexOf("Maria Santos"));
        org.assertj.core.api.Assertions.assertThat(descending.indexOf("Ana Cruz"))
                .isGreaterThan(descending.indexOf("Maria Santos"));
    }

    @Test
    void sortSurvivesFilteringAndFilteringSurvivesSorting() throws Exception
    {
        String body = mockMvc.perform(get("/people")
                        .param("satellite", ortigasId.toString())
                        .param("sort", "lastName,desc")
                        .with(user("v").roles("VIEWER")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Jose Reyes"))))
                .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(body.indexOf("Maria Santos"))
                .isLessThan(body.indexOf("Ana Cruz"));
        org.assertj.core.api.Assertions.assertThat(body).contains("lastName,desc");
    }

    @Test
    void aSortFieldOutsideTheWhitelistIsIgnoredRatherThanExploding() throws Exception
    {
        mockMvc.perform(get("/people").param("sort", "passwordHash,asc")
                        .with(user("v").roles("VIEWER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("lastName,asc")))
                .andExpect(content().string(not(containsString("passwordHash"))));
    }

    @Test
    void theNotInDgroupPresetWorks() throws Exception
    {
        mockMvc.perform(get("/people/table").param("preset", "notInDgroup").with(user("v").roles("VIEWER")))
                .andExpect(content().string(containsString("Ana Cruz")))
                .andExpect(content().string(not(containsString("Maria Santos"))));
    }

    @Test
    void theNoLeaderPresetWorks() throws Exception
    {
        mockMvc.perform(get("/people/table").param("preset", "noLeader").with(user("v").roles("VIEWER")))
                .andExpect(content().string(containsString("Ana Cruz")))
                .andExpect(content().string(containsString("Jose Reyes")))
                .andExpect(content().string(not(containsString("Maria Santos"))));
    }

    @Test
    void theBirthdaysThisMonthPresetWorks() throws Exception
    {
        mockMvc.perform(get("/people/table").param("preset", "birthdaysThisMonth")
                        .with(user("v").roles("VIEWER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Maria Santos")));
    }

    @Test
    void theAddedThisWeekPresetIncludesEverythingJustSeeded() throws Exception
    {
        mockMvc.perform(get("/people/table").param("preset", "addedThisWeek")
                        .with(user("v").roles("VIEWER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Maria Santos")))
                .andExpect(content().string(containsString("Ana Cruz")));
    }

    @Test
    void anUnknownPresetIsIgnored() throws Exception
    {
        mockMvc.perform(get("/people/table").param("preset", "nonsense").with(user("v").roles("VIEWER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Maria Santos")));
    }

    @Test
    void theFullPageRendersTheFilterControls() throws Exception
    {
        mockMvc.perform(get("/people").with(user("v").roles("VIEWER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"filters\"")))
                .andExpect(content().string(containsString("Birthdays this month")))
                .andExpect(content().string(containsString("Not in a Dgroup")))
                .andExpect(content().string(containsString("No leader assigned")))
                .andExpect(content().string(containsString("Added this week")));
    }
}
