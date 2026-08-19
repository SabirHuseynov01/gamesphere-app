package com.example.gamesphere.controller;

import com.example.gamesphere.dto.response.ApiResponse;
import com.example.gamesphere.dto.response.GiftResponse;
import com.example.gamesphere.service.GiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gifts")
@RequiredArgsConstructor
public class GiftController {

    private final GiftService giftService;

    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<List<GiftResponse>>> getSentGifts() {
        return ResponseEntity.ok(ApiResponse.success("Sent gifts retrieved", giftService.getSentGifts()));
    }

    @GetMapping("/received")
    public ResponseEntity<ApiResponse<List<GiftResponse>>> getReceivedGifts() {
        return ResponseEntity.ok(ApiResponse.success("Received gifts retrieved", giftService.getReceivedGifts()));
    }

    @PostMapping("/claim/{token}")
    public ResponseEntity<ApiResponse<GiftResponse>> claimGift(@PathVariable String token) {
        return ResponseEntity.ok(ApiResponse.success("Gift claimed", giftService.claimGift(token)));
    }
}
