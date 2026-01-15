package com.example.hotel.web;

import com.example.hotel.entity.Room;
import com.example.hotel.repository.RoomRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final RoomRepository roomRepository;

    public StatsController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Самые популярные номера (по убыванию timesBooked).
     * Аналог примера /stats/rooms/popular
     */
    @GetMapping("/rooms/popular")
    public List<Room> popularRooms() {
        return roomRepository.findAll().stream()
                .sorted(Comparator
                        .comparingLong(Room::getTimesBooked)
                        .reversed()
                        .thenComparing(Room::getId))
                .toList();
    }
}