package org.lfm.database.person;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PersonController
{
    static final int PAGE_SIZE = 20;
    static final Sort DEFAULT_SORT = Sort.by("lastName", "firstName");

    private final PersonRepository personRepository;

    public PersonController(PersonRepository personRepository)
    {
        this.personRepository = personRepository;
    }

    @GetMapping("/people")
    @PreAuthorize("hasRole('VIEWER')")
    public String list(@RequestParam(defaultValue = "0") int page, Model model)
    {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, DEFAULT_SORT);
        model.addAttribute("peoplePage", personRepository.findAll(pageable));
        return "people/list";
    }
}
