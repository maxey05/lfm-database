package org.lfm.database.person;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonSearchCriteria
{
    private String q;
    private Long satellite;
    private Gender gender;
    private CivilStatus civilStatus;
    private Boolean inDgroup;
    private boolean includeArchived;
    private String preset;

    public boolean isEmpty()
    {
        return !hasText(q)
                && satellite == null
                && gender == null
                && civilStatus == null
                && inDgroup == null
                && !includeArchived
                && !hasText(preset);
    }

    static boolean hasText(String value)
    {
        return value != null && !value.trim().isEmpty();
    }
}
