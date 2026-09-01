package org.lfm.database.person;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner
{
    private static final String[] FIRST_NAMES = {
            "Maria", "Jose", "Ana", "Juan", "Carmen", "Pedro", "Rosa", "Miguel", "Luz", "Antonio",
            "Elena", "Ramon", "Teresa", "Carlos", "Isabel", "Fernando", "Josefina", "Ricardo",
            "Cristina", "Manuel", "Beatriz", "Rafael", "Gloria", "Alberto", "Victoria"
    };

    private static final String[] LAST_NAMES = {
            "Santos", "Reyes", "Cruz", "Bautista", "Garcia", "Torres", "Flores", "Ramos", "Mendoza",
            "Castillo", "Gonzales", "Aquino", "Rivera", "Villanueva", "Fernandez", "Dela Cruz",
            "Salazar", "Pascual", "Navarro", "Domingo", "Aguilar", "Marquez", "Ocampo", "Padilla",
            "Ignacio"
    };

    private final PersonRepository personRepository;

    public DevDataSeeder(PersonRepository personRepository)
    {
        this.personRepository = personRepository;
    }

    @Override
    public void run(String... args)
    {
        if (personRepository.count() > 0) {
            return;
        }

        Gender[] genders = Gender.values();
        CivilStatus[] civilStatuses = CivilStatus.values();

        for (int i = 0; i < FIRST_NAMES.length; i++) {
            Person person = new Person();
            person.setFirstName(FIRST_NAMES[i]);
            person.setLastName(LAST_NAMES[i]);
            person.setEmail((FIRST_NAMES[i] + "." + LAST_NAMES[i] + i + "@example.com").toLowerCase());
            person.setContactNumber(String.format("+63917%07d", i));
            person.setDateOfBirth(LocalDate.of(1990 + (i % 20), 1 + (i % 12), 1 + (i % 28)));
            person.setGender(genders[i % genders.length]);
            person.setCivilStatus(civilStatuses[i % civilStatuses.length]);
            personRepository.save(person);
        }
    }
}
