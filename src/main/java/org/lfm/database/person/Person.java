package org.lfm.database.person;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfm.database.satellite.ChurchSatellite;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "person")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Person 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "nickname", length = 60)
    private String nickname;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "contact_number", length = 25)
    private String contactNumber;

    @Column(name = "facebook_name", length = 150)
    private String facebookName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "civil_status", length = 20)
    private CivilStatus civilStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_satellite_id")
    private ChurchSatellite churchSatellite;

    @Column(name = "in_dgroup", nullable = false)
    private boolean inDgroup = false;

    @Column(name = "dgroup_leader_name", length = 150)
    private String dgroupLeaderName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dgroup_leader_id")
    private Person dgroupLeader;

    @Column(name = "dgroup_leader_contact", length = 25)
    private String dgroupLeaderContact;

    @Column(name = "lfm_group_leader_name", length = 150)
    private String lfmGroupLeaderName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lfm_group_leader_id")
    private Person lfmGroupLeader;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private PersonSource source = PersonSource.MANUAL;

    @Column(name = "archived", nullable = false)
    private boolean archived = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    public String getCompleteName() {
        StringBuilder sb = new StringBuilder(firstName);
        if (middleName != null && !middleName.isBlank()) {
            sb.append(' ').append(middleName);
        }
        return sb.append(' ').append(lastName).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Person other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
