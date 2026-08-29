package org.lfm.database.satellite;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "church_satellite")
@Getter
@Setter
@NoArgsConstructor
public class ChurchSatellite 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public ChurchSatellite(String name)
    {
        this.name = name;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o)
            return true;
        if(!(o instanceof ChurchSatellite other))
            return false;

        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }
}
