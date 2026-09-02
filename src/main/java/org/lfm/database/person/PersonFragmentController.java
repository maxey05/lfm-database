package org.lfm.database.person;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.lfm.database.common.SortWhitelist;
import org.lfm.database.person.dto.PersonDetail;
import org.lfm.database.person.dto.PersonForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/people")
public class PersonFragmentController
{
    private static final String MODAL_VIEW = "people/fragments/detail :: modal";
    private static final String FORM_VIEW = "people/fragments/form :: form";

    private final PersonService personService;

    public PersonFragmentController(PersonService personService)
    {
        this.personService = personService;
    }

    @GetMapping("/table")
    public String table(@ModelAttribute("criteria") PersonSearchCriteria criteria,
                        @RequestParam(defaultValue = "0") int page,
                        Sort sort,
                        Model model)
    {
        Sort effective = PersonService.SORT.sanitize(sort);
        Page<Person> peoplePage = personService.search(criteria, page, sort);

        model.addAttribute("peoplePage", peoplePage);
        model.addAttribute("sortValue", SortWhitelist.toParam(effective));

        return "people/fragments/table :: table";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('STAFF')")
    public String newForm(Model model)
    {
        model.addAttribute("personForm", new PersonForm());
        model.addAttribute("mode", "create");
        return FORM_VIEW;
    }

    @GetMapping("/{id}/modal")
    public String detailModal(@PathVariable Long id, Model model)
    {
        model.addAttribute("person", personService.detail(id));
        return MODAL_VIEW;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model)
    {
        model.addAttribute("personForm", personService.editForm(id));
        model.addAttribute("mode", "edit");
        return FORM_VIEW;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("personForm") PersonForm form,
                         BindingResult errors,
                         Model model,
                         HttpServletResponse response)
    {
        rejectDuplicateEmail(form, errors, null);

        if(errors.hasErrors())
        {
            model.addAttribute("mode", "create");
            return FORM_VIEW;
        }

        PersonDetail saved = personService.create(form);
        model.addAttribute("person", saved);
        response.setHeader("HX-Trigger", PersonController.REFRESH_EVENT);

        return MODAL_VIEW;
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("personForm") PersonForm form,
                         BindingResult errors,
                         Model model,
                         HttpServletResponse response)
    {
        rejectDuplicateEmail(form, errors, id);

        if(errors.hasErrors())
        {
            form.setId(id);
            model.addAttribute("mode", "edit");
            return FORM_VIEW;
        }

        PersonDetail saved = personService.update(id, form);
        model.addAttribute("person", saved);
        response.setHeader("HX-Trigger", PersonController.REFRESH_EVENT);

        return MODAL_VIEW;
    }

    @PostMapping("/{id}/archive")
    public String archive(@PathVariable Long id, Model model, HttpServletResponse response)
    {
        model.addAttribute("person", personService.setArchived(id, true));
        response.setHeader("HX-Trigger", PersonController.REFRESH_EVENT);

        return MODAL_VIEW;
    }

    @PostMapping("/{id}/restore")
    public String restore(@PathVariable Long id, Model model, HttpServletResponse response)
    {
        model.addAttribute("person", personService.setArchived(id, false));
        response.setHeader("HX-Trigger", PersonController.REFRESH_EVENT);

        return MODAL_VIEW;
    }

    private void rejectDuplicateEmail(PersonForm form, BindingResult errors, Long excludeId)
    {
        if(form.getEmail() == null || form.getEmail().trim().isEmpty())
        {
            return;
        }

        if(personService.emailTaken(form.getEmail(), excludeId))
        {
            errors.rejectValue("email", "duplicate", "Someone already has that email address");
        }
    }
}
