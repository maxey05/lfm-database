package org.lfm.database.person;

import org.lfm.database.satellite.ChurchSatellite;
import org.lfm.database.satellite.ChurchSatelliteRepository;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice(assignableTypes = {PersonController.class, PersonFragmentController.class})
public class PersonLookupAdvice
{
    private final ChurchSatelliteRepository satelliteRepository;

    public PersonLookupAdvice(ChurchSatelliteRepository satelliteRepository)
    {
        this.satelliteRepository = satelliteRepository;
    }

    @InitBinder
    public void trimSubmittedStrings(WebDataBinder binder)
    {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @ModelAttribute("satellites")
    public List<ChurchSatellite> satellites()
    {
        return satelliteRepository.findByActiveTrueOrderByNameAsc();
    }

    @ModelAttribute("genders")
    public Gender[] genders()
    {
        return Gender.values();
    }

    @ModelAttribute("civilStatuses")
    public CivilStatus[] civilStatuses()
    {
        return CivilStatus.values();
    }
}
