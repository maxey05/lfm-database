package org.lfm.database.person;

import org.lfm.database.satellite.ChurchSatellite;
import org.lfm.database.satellite.ChurchSatelliteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

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

    private static final String[] LEADERS = {
            "Kuya Jed", "Ate Mimi", "Kuya Paolo", "Ate Grace"
    };

    private final PersonRepository personRepository;
    private final ChurchSatelliteRepository satelliteRepository;

    public DevDataSeeder(PersonRepository personRepository, ChurchSatelliteRepository satelliteRepository)
    {
        this.personRepository = personRepository;
        this.satelliteRepository = satelliteRepository;
    }

    @Override
    public void run(String... args)
    {
        if(personRepository.count() > 0)
        {
            return;
        }

        List<ChurchSatellite> satellites = satelliteRepository.findByActiveTrueOrderByNameAsc();
        Gender[] genders = Gender.values();
        CivilStatus[] civilStatuses = CivilStatus.values();

        for(int i = 0; i < FIRST_NAMES.length; i++)
        {
            Person person = new Person();
            person.setFirstName(FIRST_NAMES[i]);
            person.setLastName(LAST_NAMES[i]);
            person.setNickname(FIRST_NAMES[i].substring(0, 3));
            person.setEmail((FIRST_NAMES[i] + "." + LAST_NAMES[i].replace(" ", "") + i + "@example.com").toLowerCase());
            person.setContactNumber(String.format("+63917%07d", i));
            person.setFacebookName(FIRST_NAMES[i] + " " + LAST_NAMES[i]);
            person.setDateOfBirth(LocalDate.of(1990 + (i % 20), 1 + (i % 12), 1 + (i % 28)));
            person.setGender(genders[i % genders.length]);
            person.setCivilStatus(civilStatuses[i % civilStatuses.length]);

            if(!satellites.isEmpty())
            {
                person.setChurchSatellite(satellites.get(i % satellites.size()));
            }

            boolean inDgroup = i % 3 != 0;
            person.setInDgroup(inDgroup);

            if(inDgroup)
            {
                person.setDgroupLeaderName(LEADERS[i % LEADERS.length]);
                person.setDgroupLeaderContact(String.format("+63918%07d", i % LEADERS.length));
                person.setLfmGroupLeaderName(LEADERS[(i + 1) % LEADERS.length]);
            }

            person.setArchived(i % 12 == 11);
            personRepository.save(person);
        }
    }
}
