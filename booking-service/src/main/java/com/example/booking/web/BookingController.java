package com.example.booking.web;

import com.example.booking.entity.Booking;
import com.example.booking.repository.BookingRepository;
import com.example.booking.service.BookingService;
import com.example.booking.service.BookingService.RoomView;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearer-jwt")
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    public BookingController(BookingService bookingService, BookingRepository bookingRepository) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
    }

    @PostMapping
    public Booking create(@AuthenticationPrincipal Jwt jwt, @RequestBody Map<String, String> request) {
        final Long userId = Long.parseLong(jwt.getSubject());
        final Long roomId = Long.valueOf(request.get("roomId"));
        final LocalDate start = LocalDate.parse(request.get("startDate"));
        final LocalDate end = LocalDate.parse(request.get("endDate"));
        final String requestId = request.get("requestId");

        return bookingService.createBooking(userId, roomId, start, end, requestId);
    }

    @GetMapping
    public List<Booking> myBookings(@AuthenticationPrincipal Jwt jwt) {
        final Long userId = Long.parseLong(jwt.getSubject());
        return bookingRepository.findByUserId(userId);
    }

    @GetMapping("/suggestions")
    public Mono<List<RoomView>> suggestions() {
        return bookingService.getRoomSuggestions();
    }

    @GetMapping("/all")
    public ResponseEntity<List<Booking>> all(@AuthenticationPrincipal Jwt jwt) {
        final String scope = jwt.getClaimAsString("scope");
        if ("ADMIN".equals(scope)) {
            return ResponseEntity.ok(bookingRepository.findAll());
        }
        return ResponseEntity.status(403).build();
    }
}