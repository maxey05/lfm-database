package org.lfm.database.person;

import org.lfm.database.common.PersonNotFoundException;
import org.lfm.database.common.PhoneNumberNormalizer;
import org.lfm.database.common.SortWhitelist;
import org.lfm.database.person.dto.PersonDetail;
import org.lfm.database.person.dto.PersonForm;
import org.lfm.database.satellite.ChurchSatellite;
import org.lfm.database.satellite.ChurchSatelliteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Locale;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class PersonService
{
    public static final int PAGE_SIZE = 20;

    public static final SortWhitelist SORT = new SortWhitelist(
            Set.of("firstName", "middleName", "lastName", "nickname", "email", "contactNumber",
                    "facebookName", "dateOfBirth", "gender", "civilStatus", "inDgroup",
                    "dgroupLeaderName", "lfmGroupLeaderName", "createdAt", "updatedAt"),
            Sort.by("lastName", "firstName"));

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH);
    private static final ZoneId MANILA = ZoneId.of("Asia/Manila");
    private static final String DASH = "—";

    private final PersonRepository personRepository;
    private final ChurchSatelliteRepository satelliteRepository;

    public PersonService(PersonRepository personRepository, ChurchSatelliteRepository satelliteRepository)
    {
        this.personRepository = personRepository;
        this.satelliteRepository = satelliteRepository;
    }

    @PreAuthorize("hasRole('VIEWER')")
    public Page<Person> search(PersonSearchCriteria criteria, int page, Sort requestedSort)
    {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, SORT.sanitize(requestedSort));
        return personRepository.findAll(PersonSpecifications.from(criteria), pageable);
    }

    @PreAuthorize("hasRole('VIEWER')")
    public PersonDetail detail(Long id)
    {
        return toDetail(require(id));
    }

    @PreAuthorize("hasRole('STAFF')")
    public PersonForm editForm(Long id)
    {
        return toForm(require(id));
    }

    @PreAuthorize("hasRole('STAFF')")
    @Transactional
    public PersonDetail create(PersonForm form)
    {
        Person person = new Person();
        person.setSource(PersonSource.MANUAL);
        apply(form, person);
        return toDetail(personRepository.save(person));
    }

    @PreAuthorize("hasRole('STAFF')")
    @Transactional
    public PersonDetail update(Long id, PersonForm form)
    {
        Person person = require(id);
        apply(form, person);
        return toDetail(personRepository.save(person));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public PersonDetail setArchived(Long id, boolean archived)
    {
        Person person = require(id);
        person.setArchived(archived);
        return toDetail(personRepository.save(person));
    }

    @PreAuthorize("hasRole('VIEWER')")
    public boolean emailTaken(String email, Long excludeId)
    {
        String normalized = normalizeEmail(email);

        if(normalized == null)
        {
            return false;
        }

        return personRepository.findByEmail(normalized)
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .isPresent();
    }

    public PersonDetail toDetail(Person person)
    {
        ChurchSatellite satellite = person.getChurchSatellite();

        return new PersonDetail(
                person.getId(),
                person.getCompleteName(),
                dash(person.getNickname()),
                dash(person.getEmail()),
                dash(person.getContactNumber()),
                dash(person.getFacebookName()),
                person.getDateOfBirth() == null ? DASH : DATE.format(person.getDateOfBirth()),
                person.getGender() == null ? DASH : label(person.getGender().name()),
                person.getCivilStatus() == null ? DASH : label(person.getCivilStatus().name()),
                satellite == null ? DASH : satellite.getName(),
                person.isInDgroup() ? "Yes" : "No",
                dash(person.getDgroupLeaderName()),
                dash(person.getDgroupLeaderContact()),
                dash(person.getLfmGroupLeaderName()),
                label(person.getSource().name()),
                person.isArchived(),
                person.getCreatedAt() == null ? DASH : STAMP.format(person.getCreatedAt().atZone(MANILA)),
                person.getUpdatedAt() == null ? DASH : STAMP.format(person.getUpdatedAt().atZone(MANILA)));
    }

    public PersonForm toForm(Person person)
    {
        PersonForm form = new PersonForm();
        form.setId(person.getId());
        form.setFirstName(person.getFirstName());
        form.setMiddleName(person.getMiddleName());
        form.setLastName(person.getLastName());
        form.setNickname(person.getNickname());
        form.setEmail(person.getEmail());
        form.setContactNumber(person.getContactNumber());
        form.setFacebookName(person.getFacebookName());
        form.setDateOfBirth(person.getDateOfBirth());
        form.setGender(person.getGender());
        form.setCivilStatus(person.getCivilStatus());
        form.setSatelliteId(person.getChurchSatellite() == null ? null : person.getChurchSatellite().getId());
        form.setInDgroup(person.isInDgroup());
        form.setDgroupLeaderName(person.getDgroupLeaderName());
        form.setDgroupLeaderContact(person.getDgroupLeaderContact());
        form.setLfmGroupLeaderName(person.getLfmGroupLeaderName());
        return form;
    }

    private void apply(PersonForm form, Person person)
    {
        person.setFirstName(trimToNull(form.getFirstName()));
        person.setMiddleName(trimToNull(form.getMiddleName()));
        person.setLastName(trimToNull(form.getLastName()));
        person.setNickname(trimToNull(form.getNickname()));
        person.setEmail(normalizeEmail(form.getEmail()));
        person.setContactNumber(PhoneNumberNormalizer.normalize(form.getContactNumber()));
        person.setFacebookName(trimToNull(form.getFacebookName()));
        person.setDateOfBirth(form.getDateOfBirth());
        person.setGender(form.getGender());
        person.setCivilStatus(form.getCivilStatus());
        person.setInDgroup(form.isInDgroup());
        person.setDgroupLeaderName(trimToNull(form.getDgroupLeaderName()));
        person.setDgroupLeaderContact(PhoneNumberNormalizer.normalize(form.getDgroupLeaderContact()));
        person.setLfmGroupLeaderName(trimToNull(form.getLfmGroupLeaderName()));

        if(form.getSatelliteId() == null)
        {
            person.setChurchSatellite(null);
        }
        else
        {
            person.setChurchSatellite(satelliteRepository.findById(form.getSatelliteId()).orElse(null));
        }
    }

    private Person require(Long id)
    {
        return personRepository.findById(id).orElseThrow(() -> new PersonNotFoundException(id));
    }

    private static String normalizeEmail(String email)
    {
        String trimmed = trimToNull(email);
        return trimmed == null ? null : trimmed.toLowerCase(Locale.ENGLISH);
    }

    private static String trimToNull(String value)
    {
        if(value == null)
        {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String dash(String value)
    {
        String trimmed = trimToNull(value);
        return trimmed == null ? DASH : trimmed;
    }

    private static String label(String enumName)
    {
        String[] words = enumName.toLowerCase(Locale.ENGLISH).split("_");
        StringBuilder sb = new StringBuilder();

        for(String word : words)
        {
            if(sb.length() > 0)
            {
                sb.append(' ');
            }

            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }

        return sb.toString();
    }
}
