package org.lfm.database.person.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.lfm.database.person.CivilStatus;
import org.lfm.database.person.Gender;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class PersonForm
{
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @Size(max = 60)
    private String nickname;

    @Email(message = "That does not look like an email address")
    @Size(max = 255)
    private String email;

    @Size(max = 25)
    private String contactNumber;

    @Size(max = 150)
    private String facebookName;

    @Past(message = "Date of birth must be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    private Gender gender;

    private CivilStatus civilStatus;

    private Long satelliteId;

    private boolean inDgroup;

    @Size(max = 150)
    private String dgroupLeaderName;

    @Size(max = 25)
    private String dgroupLeaderContact;

    @Size(max = 150)
    private String lfmGroupLeaderName;
}
