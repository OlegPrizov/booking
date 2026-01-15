package com.example.hotel.service;

import com.example.hotel.entity.Hotel;
import com.example.hotel.entity.Room;
import com.example.hotel.entity.RoomAvailabilityLock;
import com.example.hotel.repository.HotelRepository;
import com.example.hotel.repository.RoomAvailabilityLockRepository;
import com.example.hotel.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final RoomAvailabilityLockRepository lockRepository;

    public HotelService(
            HotelRepository hotelRepository,
            RoomRepository roomRepository,
            RoomAvailabilityLockRepository lockRepository
    ) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.lockRepository = lockRepository;
    }

    public List<Hotel> listHotels() {
        return hotelRepository.findAll();
    }

    public Optional<Hotel> getHotel(Long id) {
        return hotelRepository.findById(id);
    }

    public Hotel saveHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    public void deleteHotel(Long id) {
        hotelRepository.deleteById(id);
    }

    public List<Room> listRooms() {
        return roomRepository.findAll();
    }

    public Optional<Room> getRoom(Long id) {
        return roomRepository.findById(id);
    }

    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    @Transactional
    public RoomAvailabilityLock holdRoom(String requestId, Long roomId, LocalDate startDate, LocalDate endDate) {
        Optional<RoomAvailabilityLock> existing = lockRepository.findByRequestId(requestId);
        if (existing.isPresent()) {
            return existing.get();
        }

        if (startDate == null || endDate == null || !startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("Некорректный диапазон дат");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("Room not found"));

        if (!room.isAvailable()) {
            throw new IllegalStateException("Номер недоступен (available=false)");
        }

        boolean hasOverlap = lockRepository.existsOverlap(
                roomId,
                startDate,
                endDate,
                Set.of(RoomAvailabilityLock.Status.HELD, RoomAvailabilityLock.Status.CONFIRMED)
        );

        if (hasOverlap) {
            throw new IllegalStateException("Номер недоступен на указанные даты");
        }

        RoomAvailabilityLock lock = RoomAvailabilityLock.builder()
                .requestId(requestId)
                .roomId(roomId)
                .startDate(startDate)
                .endDate(endDate)
                .status(RoomAvailabilityLock.Status.HELD)
                .build();

        return lockRepository.save(lock);
    }

    @Transactional
    public RoomAvailabilityLock confirmHold(String requestId) {
        RoomAvailabilityLock lock = lockRepository.findByRequestId(requestId)
                .orElseThrow(() -> new IllegalStateException("Hold not found"));

        if (lock.getStatus() == RoomAvailabilityLock.Status.CONFIRMED) {
            return lock;
        }
        if (lock.getStatus() == RoomAvailabilityLock.Status.RELEASED) {
            throw new IllegalStateException("Удержание уже снято");
        }

        lock.setStatus(RoomAvailabilityLock.Status.CONFIRMED);
        lockRepository.save(lock);

        roomRepository.findById(lock.getRoomId()).ifPresent(room -> {
            room.setTimesBooked(room.getTimesBooked() + 1);
            roomRepository.save(room);
        });

        return lock;
    }

    @Transactional
    public RoomAvailabilityLock releaseHold(String requestId) {
        RoomAvailabilityLock lock = lockRepository.findByRequestId(requestId)
                .orElseThrow(() -> new IllegalStateException("Hold not found"));

        if (lock.getStatus() == RoomAvailabilityLock.Status.RELEASED) {
            return lock;
        }
        if (lock.getStatus() == RoomAvailabilityLock.Status.CONFIRMED) {
            return lock;
        }

        lock.setStatus(RoomAvailabilityLock.Status.RELEASED);
        return lockRepository.save(lock);
    }
}