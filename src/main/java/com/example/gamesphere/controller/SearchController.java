package com.example.gamesphere.controller;

import com.example.gamesphere.dto.response.ApiResponse;
import com.example.gamesphere.dto.response.GlobalSearchResponse;
import com.example.gamesphere.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/search")
@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<ApiResponse<GlobalSearchResponse>> search (
            @RequestParam String q,
            @RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(ApiResponse.success("Search completed", searchService.search(q, limit)));
    }
}
