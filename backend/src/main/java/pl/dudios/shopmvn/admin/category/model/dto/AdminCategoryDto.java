package pl.dudios.shopmvn.admin.category.model.dto;

import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;

@Getter
public class AdminCategoryDto {

    @NotBlank
    @Length(min = 3, max = 255)
    private String name;
    private String description;
    // Wersja angielska — opcjonalna.
    @Length(max = 255)
    private String nameEn;
    private String descriptionEn;
    @NotBlank
    @Length(min = 3, max = 255)
    private String slug;

}
