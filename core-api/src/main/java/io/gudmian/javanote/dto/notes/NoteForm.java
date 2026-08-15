package io.gudmian.javanote.dto.notes;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * @author d.guba
 */
@NoArgsConstructor
@Getter
@Setter
public class NoteForm {
    private UUID id;
    @NotBlank
    private String title;
    private String content;
    private String tags;
}
