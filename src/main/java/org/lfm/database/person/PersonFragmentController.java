package org.lfm.database.person;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PersonFragmentController
{
    private final PersonRepository personRepository;

    public PersonFragmentController(PersonRepository personRepository)
    {
        this.personRepository = personRepository;
    }

    @GetMapping("/people/table")
    @PreAuthorize("hasRole('VIEWER')")
    public String table(@RequestParam(defaultValue = "0") int page, Model model)
    {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PersonController.PAGE_SIZE, PersonController.DEFAULT_SORT);
        model.addAttribute("peoplePage", personRepository.findAll(pageable));
        return "people/fragments/table :: table";
    }
}
