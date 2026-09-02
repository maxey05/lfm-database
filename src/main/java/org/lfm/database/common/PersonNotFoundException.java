package org.lfm.database.common;

public class PersonNotFoundException extends RuntimeException
{
    public PersonNotFoundException(Long id)
    {
        super("No person with id " + id);
    }
}
