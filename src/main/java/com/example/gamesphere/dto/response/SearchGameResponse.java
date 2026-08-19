package com.example.gamesphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchGameResponse {

    private Long id;
    private String title;
    private String slug;
    private String coverImageUrl;
    private String developer;
    private String publisher;
    private LocalDate releaseDate;
}
