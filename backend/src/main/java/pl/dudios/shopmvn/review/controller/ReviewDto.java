package pl.dudios.shopmvn.review.controller;

import org.hibernate.validator.constraints.Length;

public record ReviewDto(
        @Length(min = 2, max = 60)
        String authorName,
        @Length(min = 2, max = 600)
        String content,
        Long productId) {
}

