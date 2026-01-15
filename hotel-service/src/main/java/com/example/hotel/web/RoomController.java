package com.example.hotel.web;

import com.example.hotel.entity.Room;
import com.example.hotel.entity.RoomAvailabilityLock;
import com.example.hotel.service.HotelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final HotelService hotelService;

    public RoomController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> get(@PathVariable("id") Long id) {
        return hotelService.getRoom(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Room> create(@RequestBody Map<String, Object> req) {
        Long hotelId = ((Number) req.get("hotelId")).longValue();

        var hotel = hotelService.getHotel(hotelId)
                .orElse(null);
        if (hotel == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Room room = new Room();
        room.setNumber((String) req.get("number"));

        Object capacityObj = req.get("capacity");
        room.setCapacity(capacityObj == null ? 0 : ((Number) capacityObj).intValue());

        Object availableObj = req.get("available");
        room.setAvailable(availableObj == null || (Boolean) availableObj);

        room.setTimesBooked(0);
        room.setHotel(hotel);

        return ResponseEntity.status(HttpStatus.CREATED).body(hotelService.saveRoom(room));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> update(@PathVariable Long id, @RequestBody Room r) {
        return hotelService.getRoom(id)
                .map(existing -> {
                    // hotel не меняем здесь
                    existing.setNumber(r.getNumber());
                    existing.setCapacity(r.getCapacity());
                    existing.setAvailable(r.isAvailable());
                    return ResponseEntity.ok(hotelService.saveRoom(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hotelService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    // ===== availability (упрощённо как в примере) =====

    @PostMapping("/{id}/hold")
    public ResponseEntity<RoomAvailabilityLock> hold(@PathVariable Long id, @RequestBody Map<String, String> req) {
        String requestId = req.get("requestId");
        LocalDate start = LocalDate.parse(req.get("startDate"));
        LocalDate end = LocalDate.parse(req.get("endDate"));
        try {
            RoomAvailabilityLock lock = hotelService.holdRoom(requestId, id, start, end);
            return ResponseEntity.ok(lock);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<RoomAvailabilityLock> confirm(@PathVariable Long id, @RequestBody Map<String, String> req) {
        String requestId = req.get("requestId");
        try {
            return ResponseEntity.ok(hotelService.confirmHold(requestId));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<RoomAvailabilityLock> release(@PathVariable Long id, @RequestBody Map<String, String> req) {
        String requestId = req.get("requestId");
        try {
            return ResponseEntity.ok(hotelService.releaseHold(requestId));
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }
}