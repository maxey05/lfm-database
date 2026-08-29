package org.lfm.database.person;

import org.junit.jupiter.api.Test;
import org.lfm.database.config.JpaAuditingConfig;
import org.lfm.database.satellite.ChurchSatellite;
import org.lfm.database.satellite.ChurchSatelliteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=validate")
@Import(JpaAuditingConfig.class)
@Testcontainers
class PersonRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ChurchSatelliteRepository satelliteRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Person samplePerson(String email) {
        Person person = new Person();
        person.setFirstName("Maria");
        person.setMiddleName("Reyes");
        person.setLastName("Santos");
        person.setNickname("Mia");
        person.setEmail(email);
        person.setContactNumber("+639171234567");
        person.setFacebookName("Maria Santos");
        person.setDateOfBirth(LocalDate.of(2001, 5, 14));
        person.setGender(Gender.FEMALE);
        person.setCivilStatus(CivilStatus.SINGLE);
        return person;
    }

    @Test
    void flywayCreatedTheSchemaAndSeededSatellites() {
        List<ChurchSatellite> satellites = satelliteRepository.findByActiveTrueOrderByNameAsc();

        assertThat(satellites).isNotEmpty();
        assertThat(satellites).extracting(ChurchSatellite::getName).doesNotHaveDuplicates();
    }

    @Test
    void savesAndReloadsEveryField() {
        Person saved = personRepository.save(samplePerson("maria.santos@example.com"));
        entityManager.flush();
        entityManager.clear();

        Person loaded = personRepository.findById(saved.getId()).orElseThrow();

        assertThat(loaded.getFirstName()).isEqualTo("Maria");
        assertThat(loaded.getMiddleName()).isEqualTo("Reyes");
        assertThat(loaded.getLastName()).isEqualTo("Santos");
        assertThat(loaded.getCompleteName()).isEqualTo("Maria Reyes Santos");
        assertThat(loaded.getDateOfBirth()).isEqualTo(LocalDate.of(2001, 5, 14));
        assertThat(loaded.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(loaded.getCivilStatus()).isEqualTo(CivilStatus.SINGLE);
    }

    @Test
    void appliesDefaultsAndAuditTimestamps() {
        Person saved = personRepository.save(samplePerson("defaults@example.com"));
        entityManager.flush();
        entityManager.clear();

        Person loaded = personRepository.findById(saved.getId()).orElseThrow();

        assertThat(loaded.isInDgroup()).isFalse();
        assertThat(loaded.isArchived()).isFalse();
        assertThat(loaded.getSource()).isEqualTo(PersonSource.MANUAL);
        assertThat(loaded.getCreatedAt()).isNotNull();
        assertThat(loaded.getUpdatedAt()).isNotNull();
        assertThat(loaded.getCreatedBy()).isNull();
    }

    @Test
    void rejectsADuplicateEmail() {
        personRepository.saveAndFlush(samplePerson("duplicate@example.com"));

        Person second = samplePerson("duplicate@example.com");
        second.setFirstName("Ana");

        assertThatThrownBy(() -> personRepository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsAnEmailThatIsNotLowercase() {
        Person person = samplePerson("Maria.Santos@Example.com");

        assertThatThrownBy(() -> personRepository.saveAndFlush(person))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void linksAPersonToASatelliteAndToALeader() {
        ChurchSatellite satellite = satelliteRepository.findByActiveTrueOrderByNameAsc().get(0);

        Person leader = samplePerson("leader@example.com");
        leader.setFirstName("Jed");
        leader.setLastName("Cruz");
        personRepository.saveAndFlush(leader);

        Person member = samplePerson("member@example.com");
        member.setChurchSatellite(satellite);
        member.setInDgroup(true);
        member.setDgroupLeaderName("Kuya Jed");
        member.setDgroupLeader(leader);
        Person saved = personRepository.saveAndFlush(member);

        entityManager.clear();

        Optional<Person> reloaded = personRepository.findByEmail("member@example.com");

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getId()).isEqualTo(saved.getId());
        assertThat(reloaded.get().isInDgroup()).isTrue();
        assertThat(reloaded.get().getDgroupLeaderName()).isEqualTo("Kuya Jed");
        assertThat(reloaded.get().getDgroupLeader().getId()).isEqualTo(leader.getId());
        assertThat(reloaded.get().getChurchSatellite().getName()).isEqualTo(satellite.getName());
    }
}