package org.lfm.database.person;

import org.lfm.database.common.SortWhitelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/people")
public class PersonController
{
    static final int PAGE_SIZE = PersonService.PAGE_SIZE;
    static final String REFRESH_EVENT = "refresh-people";

    private final PersonService personService;

    public PersonController(PersonService personService)
    {
        this.personService = personService;
    }

    @GetMapping
    public String list(@ModelAttribute("criteria") PersonSearchCriteria criteria,
                       @RequestParam(defaultValue = "0") int page,
                       Sort sort,
                       Model model)
    {
        Sort effective = PersonService.SORT.sanitize(sort);
        Page<Person> peoplePage = personService.search(criteria, page, sort);

        model.addAttribute("peoplePage", peoplePage);
        model.addAttribute("sortValue", SortWhitelist.toParam(effective));

        return "people/list";
    }

    @GetMapping("/{id}")
    public String detailPage(@PathVariable Long id, Model model)
    {
        model.addAttribute("person", personService.detail(id));
        return "people/detail";
    }
}
