package org.lfm.database.person.dto;

public record PersonDetail(
        Long id,
        String completeName,
        String nickname,
        String email,
        String contactNumber,
        String facebookName,
        String dateOfBirth,
        String gender,
        String civilStatus,
        String satelliteName,
        String inDgroup,
        String dgroupLeaderName,
        String dgroupLeaderContact,
        String lfmGroupLeaderName,
        String source,
        boolean archived,
        String createdAt,
        String updatedAt)
{
}
