package org.lfm.database.person;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public final class PersonSpecifications
{
    public static final String PRESET_BIRTHDAYS_THIS_MONTH = "birthdaysThisMonth";
    public static final String PRESET_NOT_IN_DGROUP = "notInDgroup";
    public static final String PRESET_NO_LEADER = "noLeader";
    public static final String PRESET_ADDED_THIS_WEEK = "addedThisWeek";

    private static final String[] SEARCHABLE = {
            "firstName", "middleName", "lastName", "nickname", "email", "contactNumber", "facebookName"
    };

    private PersonSpecifications()
    {
    }

    public static Specification<Person> from(PersonSearchCriteria criteria)
    {
        List<Specification<Person>> parts = new ArrayList<>();

        if(!criteria.isIncludeArchived())
        {
            parts.add(notArchived());
        }

        if(PersonSearchCriteria.hasText(criteria.getQ()))
        {
            parts.add(matchesText(criteria.getQ().trim()));
        }

        if(criteria.getSatellite() != null)
        {
            parts.add(inSatellite(criteria.getSatellite()));
        }

        if(criteria.getGender() != null)
        {
            parts.add(hasGender(criteria.getGender()));
        }

        if(criteria.getCivilStatus() != null)
        {
            parts.add(hasCivilStatus(criteria.getCivilStatus()));
        }

        if(criteria.getInDgroup() != null)
        {
            parts.add(inDgroup(criteria.getInDgroup()));
        }

        Specification<Person> preset = preset(criteria.getPreset());

        if(preset != null)
        {
            parts.add(preset);
        }

        return combine(parts);
    }

    public static Specification<Person> preset(String name)
    {
        if(!PersonSearchCriteria.hasText(name))
        {
            return null;
        }

        switch(name.trim())
        {
            case PRESET_BIRTHDAYS_THIS_MONTH:
                return birthdayInMonth(LocalDate.now().getMonthValue());
            case PRESET_NOT_IN_DGROUP:
                return inDgroup(false);
            case PRESET_NO_LEADER:
                return noLeaderRecorded();
            case PRESET_ADDED_THIS_WEEK:
                return createdSince(Instant.now().minus(7, ChronoUnit.DAYS));
            default:
                return null;
        }
    }

    public static Specification<Person> notArchived()
    {
        return (root, query, cb) -> cb.isFalse(root.<Boolean>get("archived"));
    }

    public static Specification<Person> matchesText(String text)
    {
        String pattern = "%" + escapeLike(text.toLowerCase()) + "%";

        return (root, query, cb) ->
        {
            List<Predicate> matches = new ArrayList<>();

            for(String field : SEARCHABLE)
            {
                Path<String> path = root.<String>get(field);
                matches.add(cb.like(cb.lower(path), pattern, '\\'));
            }

            return cb.or(matches.toArray(new Predicate[0]));
        };
    }

    public static Specification<Person> inSatellite(Long satelliteId)
    {
        return (root, query, cb) -> cb.equal(root.get("churchSatellite").get("id"), satelliteId);
    }

    public static Specification<Person> hasGender(Gender gender)
    {
        return (root, query, cb) -> cb.equal(root.get("gender"), gender);
    }

    public static Specification<Person> hasCivilStatus(CivilStatus civilStatus)
    {
        return (root, query, cb) -> cb.equal(root.get("civilStatus"), civilStatus);
    }

    public static Specification<Person> inDgroup(boolean value)
    {
        return (root, query, cb) -> cb.equal(root.get("inDgroup"), value);
    }

    public static Specification<Person> birthdayInMonth(int month)
    {
        return (root, query, cb) -> cb.and(cb.isNotNull(root.get("dateOfBirth")),
                cb.equal(root.get("birthMonth"), month));
    }

    public static Specification<Person> createdSince(Instant moment)
    {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.<Instant>get("createdAt"), moment);
    }

    public static Specification<Person> noLeaderRecorded()
    {
        return (root, query, cb) -> cb.and(blank(cb, root.<String>get("dgroupLeaderName")),
                blank(cb, root.<String>get("lfmGroupLeaderName")));
    }

    private static Predicate blank(jakarta.persistence.criteria.CriteriaBuilder cb, Path<String> path)
    {
        return cb.or(cb.isNull(path), cb.equal(cb.trim(path), ""));
    }

    private static Specification<Person> combine(List<Specification<Person>> parts)
    {
        Specification<Person> combined = (root, query, cb) -> cb.conjunction();

        for(Specification<Person> part : parts)
        {
            combined = combined.and(part);
        }

        return combined;
    }

    private static String escapeLike(String value)
    {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
