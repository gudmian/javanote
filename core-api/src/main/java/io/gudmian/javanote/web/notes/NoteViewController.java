package io.gudmian.javanote.web.notes;

import io.gudmian.javanote.data.notes.NoteDocument;
import io.gudmian.javanote.dto.notes.NoteForm;
import io.gudmian.javanote.service.notes.NoteService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

/**
 * @author d.guba
 */
@Controller
@RequestMapping("/notes")
public class NoteViewController {

    private final NoteService noteService;

    public NoteViewController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public String list(Model model, Authentication authentication) {
        List<NoteDocument> notes = noteService.findAllForOwner(authentication.getName());
        model.addAttribute("notes", notes);
        return "notes/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("noteForm", new NoteForm());
        return "notes/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute NoteForm form,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "notes/form";
        }

        UUID ownerId = noteService.resolveUserId(authentication.getName());
        noteService.create(
                ownerId,
                form.getTitle(),
                form.getContent(),
                List.of(form.getTags().split(","))
        );
        redirectAttributes.addFlashAttribute("message", "Заметка создана");
        return "redirect:/notes";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model, Authentication authentication) {
        NoteDocument document = noteService.findByIdForOwner(id, authentication.getName());
        NoteForm editModel = new NoteForm();
        editModel.setId(document.id());
        editModel.setTitle(document.title());
        editModel.setContent(document.content());
        editModel.setTags(String.join(",", document.tags()));
        model.addAttribute(editModel);
        return "notes/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute NoteForm form,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "notes/form";
        }

        noteService.update(
                id,
                authentication.getName(),
                form.getTitle(),
                form.getContent(),
                List.of(form.getTags().split(","))
        );

        redirectAttributes.addFlashAttribute("message", "Заметка обновлена");
        return "redirect:/notes";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable UUID id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        noteService.delete(id, authentication.getName());
        redirectAttributes.addFlashAttribute("message", "Заметка удалена");
        return "redirect:/notes";
    }
}
