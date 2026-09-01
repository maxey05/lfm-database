package org.lfm.database.user;

import org.junit.jupiter.api.Test;
import org.lfm.database.config.JpaAuditingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=validate")
@Import(JpaAuditingConfig.class)
@Testcontainers
class AppUserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void flywaySeedsAnAdminWhosePasswordHashIsUsable() {
        Optional<AppUser> admin = appUserRepository.findByUsername("admin");

        assertThat(admin).isPresent();
        assertThat(admin.get().getRole()).isEqualTo(Role.ADMIN);
        assertThat(admin.get().isEnabled()).isTrue();
        assertThat(admin.get().getCreatedAt()).isNotNull();
        assertThat(new BCryptPasswordEncoder().matches("ChangeMe123!", admin.get().getPasswordHash())).isTrue();
    }

    @Test
    void rejectsADuplicateUsername() {
        AppUser duplicate = new AppUser();
        duplicate.setUsername("admin");
        duplicate.setPasswordHash("irrelevant");
        duplicate.setFullName("Impostor");
        duplicate.setRole(Role.VIEWER);

        assertThatThrownBy(() -> appUserRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void storesRolesAsStringsNotOrdinals() {
        AppUser staff = new AppUser();
        staff.setUsername("booth1");
        staff.setPasswordHash("irrelevant");
        staff.setFullName("Booth Volunteer");
        staff.setRole(Role.STAFF);
        appUserRepository.saveAndFlush(staff);

        Object storedRole = entityManager.getEntityManager()
                .createNativeQuery("SELECT role FROM app_user WHERE username = 'booth1'")
                .getSingleResult();

        assertThat(storedRole).isEqualTo("STAFF");
        assertThat(appUserRepository.findByUsername("booth1"))
                .get()
                .extracting(AppUser::getRole)
                .isEqualTo(Role.STAFF);
    }

    @Test
    void rejectsARoleOutsideTheEnum() {
        assertThatThrownBy(() -> entityManager.getEntityManager()
                .createNativeQuery("""
                        INSERT INTO app_user (username, password_hash, full_name, role, enabled)
                        VALUES ('rogue', 'irrelevant', 'Rogue', 'SUPERUSER', true)
                        """)
                .executeUpdate())
                .isInstanceOf(Exception.class);
    }
}
