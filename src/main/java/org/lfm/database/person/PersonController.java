package org.lfm.database.person;

import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PersonController
{
    private final PersonRepository personRepository;

    public PersonController(PersonRepository personRepository)
    {
        this.personRepository = personRepository;
    }

    @GetMapping("/people")
    @PreAuthorize("hasRole('VIEWER')")
    public String list(Model model)
    {
        model.addAttribute("people", personRepository.findAll(Sort.by("lastName", "firstName")));
        return "people/list";
    }
}
