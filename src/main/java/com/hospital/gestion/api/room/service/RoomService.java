package com.hospital.gestion.api.room.service;

import com.hospital.gestion.api.common.enums.BedStatus;
import com.hospital.gestion.api.common.enums.RoomStatus;
import com.hospital.gestion.api.common.enums.RoomType;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.exception.ResourceNotFoundException;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;



import com.hospital.gestion.api.room.dto.*;
import com.hospital.gestion.api.room.entity.Room;
import com.hospital.gestion.api.room.mapper.RoomMapper;
import com.hospital.gestion.api.room.repository.RoomRepository;
import com.hospital.gestion.api.ward.entity.Ward;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;





import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

import static com.hospital.gestion.api.room.specification.RoomSpecification.belongsToDepartment;
import static com.hospital.gestion.api.room.specification.RoomSpecification.belongsToWard;
import static com.hospital.gestion.api.room.specification.RoomSpecification.hasFloor;
import static com.hospital.gestion.api.room.specification.RoomSpecification.hasRoomType;
import static com.hospital.gestion.api.room.specification.RoomSpecification.hasStatus;
import static com.hospital.gestion.api.room.specification.RoomSpecification.numberContains;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;


    private final RoomMapper roomMapper;
    private final HospitalEntityHelper helper;


    private static final Set<String>
            ALLOWED_SORT_PROPERTIES = Set.of(
            "id",
            "number",
            "floor",
            "roomType",
            "status",
            "capacity",
            "ward.id",
            "ward.department.id",
            "createdAt",
            "updatedAt"
    );

    //================
    //create
    //==================

    @Transactional
    public RoomResponseDTO createRoom(RoomRequestDTO request) {

        log.info("Creating room: {} in ward: {}", request.number(), request.wardId());
        helper.validateRoomNumber(request.number());


        String normalizedNumber =
                request.number().trim();

        if (roomRepository.existsByNumberIgnoreCase(normalizedNumber)) {
            throw new ConflictException("Room already exists: " +normalizedNumber);

        }

        Ward ward = helper.findWardById(request.wardId());
        Room room = roomMapper.toEntity(request,ward);

        room.setNumber(normalizedNumber);
        room.setNotes(helper.normalizeNullableText(request.notes()));

        Room savedRoom = roomRepository.save(room);
        log.info(
                "Room created successfully with id: {}",
                savedRoom.getId()
        );

        return roomMapper.toResponseDTO(savedRoom);
    }



    //========
    //GET ALL
    //=============
    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getAllRooms(){
        log.info("Getting all rooms");

        return roomMapper.toResponseDTOList(roomRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Page<RoomResponseDTO> getAllRooms(
            Pageable pageable
    ) {
        log.info(
                "Fetching all rooms with pagination: {}",
                pageable
        );

        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return roomRepository.findAll(pageable)
                .map(roomMapper::toResponseDTO);
    }

    //========
    //GET BY ID
    //=============0

    @Transactional(readOnly = true)
    public RoomResponseDTO getRoomById(Long id) {

        log.info("Fetching room by id: {}", id);

       return roomMapper.toResponseDTO(helper.findRoomById(id));

    }

    //=====
    //Get room by number
    //===========0


    @Transactional(readOnly = true)
    public RoomResponseDTO getRoomByNumber(String number) {
        log.info("Fetching room by number: {}", number);

        Room room= roomRepository.findByNumberIgnoreCase(number.trim()).orElseThrow(
                () -> new ResourceNotFoundException("Room number: " + number.trim())
        );

        return roomMapper.toResponseDTO(room);
    }


    // ==============
    // GET BY WARD
    // ===========

@Transactional(readOnly = true)
public List<RoomResponseDTO> getRoomsByWardId(Long wardId) {
        log.info("Fetching rooms by wardId: {}", wardId);

        return roomMapper.toResponseDTOList(roomRepository.findByWard_Id(wardId));
}


    @Transactional(readOnly = true)
    public Page<RoomResponseDTO> getRoomsByWard(
            Long wardId,
            Pageable pageable
    ) {
        log.info(
                "Fetching rooms by ward: {} with pagination",
                wardId
        );

        helper.validateWardExists(wardId);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return roomRepository
                .findByWard_Id(wardId, pageable)
                .map(roomMapper::toResponseDTO);
    }


    //======
    //GET BY STATUS
    //============0

    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getRoomsByStatus(RoomStatus status) {
        log.info("Fetching rooms by status: {}", status);
         helper.validateRoomStatus(status);

         return roomMapper.toResponseDTOList(roomRepository.findByStatus(status));
    }


    @Transactional(readOnly = true)
    public Page<RoomResponseDTO> getRoomsByStatus(
            RoomStatus status,
            Pageable pageable
    ) {
        log.info(
                "Fetching rooms by status: {} with pagination",
                status
        );

        helper.validateRoomStatus(status);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return roomRepository
                .findByStatus(status, pageable)
                .map(roomMapper::toResponseDTO);
    }


    //===
    //GET BY TYPE
    //===========
    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getRoomsByType(RoomType type) {
        log.info("Fetching rooms by type: {}", type);
        helper.validateRoomType(type);
        return roomMapper.toResponseDTOList(roomRepository.findByRoomType(type));
    }


    @Transactional(readOnly = true)
    public Page<RoomResponseDTO> getRoomsByType(
            RoomType roomType,
            Pageable pageable
    ) {
        log.info(
                "Fetching rooms by type: {} with pagination",
                roomType
        );

        helper.validateRoomType(roomType);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return roomRepository
                .findByRoomType(roomType, pageable)
                .map(roomMapper::toResponseDTO);
    }


    //========
    //GET BY FLOOR
    //============
    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getRoomsByFloor(Integer floor) {
        log.info("Fetching rooms by floor: {}", floor);
        helper.validateFloor(floor);
        return roomMapper.toResponseDTOList(roomRepository.findByFloor(floor));
    }


    @Transactional(readOnly = true)
    public Page<RoomResponseDTO> getRoomsByFloor(
            Integer floor,
            Pageable pageable
    ) {
        log.info(
                "Fetching rooms by floor: {} with pagination",
                floor
        );

        helper.validateFloor(floor);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return roomRepository
                .findByFloor(floor, pageable)
                .map(roomMapper::toResponseDTO);
    }


    // ==========================
    // GET BY WARD AND STATUS
    // ========================

    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getRoomsByWardAndStatus(Long wardId, RoomStatus status){
        log.info(
                "Fetching rooms by ward: {} and status: {}",
                wardId,
                status
        );

        helper.validateWardExists(wardId);
        helper.validateRoomStatus(status);

        return roomMapper.toResponseDTOList(roomRepository.findByWard_IdAndStatus(wardId, status));
    }


    @Transactional(readOnly = true)
    public Page<RoomResponseDTO> getRoomsByWardAndStatus(
            Long wardId,
            RoomStatus status,
            Pageable pageable
    ) {
        log.info(
                "Fetching rooms by ward: {}, status: {} "
                        + "with pagination",
                wardId,
                status
        );

        helper.validateWardExists(wardId);
        helper.validateRoomStatus(status);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return roomRepository
                .findByWard_IdAndStatus(
                        wardId,
                        status,
                        pageable
                )
                .map(roomMapper::toResponseDTO);
    }


    // ==================
    // GET BY DEPARTMENT
    // ==================

    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getRoomsByDepartment(Long departmentId){

        log.info("Fetching rooms by department: {}", departmentId);
        helper.validateDepartmentExist(departmentId);

        return roomMapper.toResponseDTOList(roomRepository.findByWard_Department_Id(departmentId));
    }

    @Transactional(readOnly = true)
    public Page<RoomResponseDTO> getRoomsByDepartment(
            Long departmentId,
            Pageable pageable
    ) {
        log.info(
                "Fetching rooms by department: {} with pagination",
                departmentId
        );

        helper.validateDepartmentExist(departmentId);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return roomRepository
                .findByWard_Department_Id(
                        departmentId,
                        pageable
                )
                .map(roomMapper::toResponseDTO);
    }


    //================
    //GET BY DEPARTMENT AND STATUS
    //==============================

    @Transactional(readOnly = true)
    public List<RoomResponseDTO>getRoomsByDepartmentAndStatus(Long departmentId, RoomStatus status){
        log.info("Fetching rooms by department: {} and status: {}", departmentId, status);

        helper.validateDepartmentExist(departmentId);
        helper.validateRoomStatus(status);

        return roomMapper.toResponseDTOList(roomRepository.findByWard_Department_IdAndStatus(departmentId, status));
    }

    @Transactional(readOnly = true)
    public Page<RoomResponseDTO>
    getRoomsByDepartmentAndStatus(
            Long departmentId,
            RoomStatus status,
            Pageable pageable
    ) {
        log.info(
                "Fetching rooms by department: {}, status: {} "
                        + "with pagination",
                departmentId,
                status
        );

        helper.validateDepartmentExist(departmentId);
        helper.validateRoomStatus(status);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return roomRepository
                .findByWard_Department_IdAndStatus(
                        departmentId,
                        status,
                        pageable
                )
                .map(roomMapper::toResponseDTO);
    }


    //=======
    //update room
    //===============
    @Transactional
    public RoomResponseDTO updateRoomById(Long id, RoomUpdateDTO request) {
        log.info("Updating room by id {}", id);


        Room room = helper.findRoomById(id);

        helper.validateRoomNumber(request.number());

        String normalizedNumber =
                request.number().trim();

        helper.validateRoomNumberForUpdate(
                room,
                normalizedNumber
        );

        helper.validateCapacityForUpdate(
                room,
                request.capacity()
        );

        roomMapper.updateEntity(room, request);

        room.setNumber(normalizedNumber);
        room.setNotes(
                helper.normalizeNullableText(request.notes())
        );

        Room updatedRoom = roomRepository.saveAndFlush(room);

        log.info(
                "Room updated successfully with id: {}",
                updatedRoom.getId()
        );

        return roomMapper.toResponseDTO(updatedRoom);

    }



    //=========
    //Update Status
    //==============

    @Transactional
    public RoomResponseDTO updateRoomStatus(Long id,RoomStatusUpdateDTO request){
        log.info(
                "Updating status of room: {} to: {}", id, request.status());

        Room room = helper.findRoomById(id);
        helper.validateRoomStatus(request.status());
        if (room.getStatus() == request.status()) {
            throw new ConflictException(
                    "Room already has status: "
                            + request.status()
            );
        }

        room.setStatus(request.status());

        if (request.notes() != null) {
            room.setNotes(
                    helper.normalizeNullableText(request.notes())
            );
        }

        Room updatedRoom = roomRepository.saveAndFlush(room);

        log.info(
                "Room status updated successfully for id: {}",
                id
        );

        return roomMapper.toResponseDTO(updatedRoom);
    }


    //===
    //Delete
    //=========0

    @Transactional
    public void deleteRoom(Long id) {
        log.info("Deleting room by id: {}", id);

        Room room = helper.findRoomById(id);

        if (room.getBeds() != null
                && !room.getBeds().isEmpty()) {
            throw new ConflictException(
                    "Room cannot be deleted because it contains beds"
            );
        }

        roomRepository.delete(room);

        log.info(
                "Room deleted successfully with id: {}",
                id
        );
    }


    // ========================================
    // EXISTS
    // ========================================

    @Transactional(readOnly = true)
    public boolean existsByNumber(
            String number
    ) {
        helper.validateRoomNumber(number);

        return roomRepository.existsByNumberIgnoreCase(
                number.trim()
        );
    }






    // ========================================
    // COUNT
    // ========================================

    @Transactional(readOnly = true)
    public long countAllRooms() {
        return roomRepository.count();
    }

    @Transactional(readOnly = true)
    public long countRoomsByStatus(
            RoomStatus status
    ) {
        helper.validateRoomStatus(status);

        return roomRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countRoomsByType(
            RoomType roomType
    ) {
        helper.validateRoomType(roomType);

        return roomRepository.countByRoomType(roomType);
    }

    @Transactional(readOnly = true)
    public long countRoomsByFloor(
            Integer floor
    ) {
        helper.validateFloor(floor);

        return roomRepository.countByFloor(floor);
    }

    @Transactional(readOnly = true)
    public long countRoomsByWard(
            Long wardId
    ) {
        helper.validateWardExists(wardId);

        return roomRepository.countByWard_Id(wardId);
    }

    @Transactional(readOnly = true)
    public long countRoomsByWardAndStatus(
            Long wardId,
            RoomStatus status
    ) {
        helper.validateWardExists(wardId);
        helper.validateRoomStatus(status);

        return roomRepository.countByWard_IdAndStatus(
                wardId,
                status
        );
    }

    @Transactional(readOnly = true)
    public long countRoomsByDepartment(
            Long departmentId
    ) {
        helper.validateDepartmentExist(departmentId);

        return roomRepository.countByWard_Department_Id(
                departmentId
        );
    }

    @Transactional(readOnly = true)
    public long countRoomsByDepartmentAndStatus(
            Long departmentId,
            RoomStatus status
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validateRoomStatus(status);

        return roomRepository
                .countByWard_Department_IdAndStatus(
                        departmentId,
                        status
                );
    }


    // ========================================
    // STATISTICS
    // ========================================

    @Transactional(readOnly = true)
    public RoomStatsResponse getRoomStats() {
        log.info("Fetching room statistics");

        return new RoomStatsResponse(
                roomRepository.countByStatus(
                        RoomStatus.AVAILABLE
                ),
                roomRepository.countByStatus(
                        RoomStatus.OCCUPIED
                ),
                roomRepository.countByStatus(
                        RoomStatus.MAINTENANCE
                ),
                roomRepository.countByStatus(
                        RoomStatus.CLEANING
                ),
                roomRepository.countByStatus(
                        RoomStatus.RESERVED
                ),
                roomRepository.countBedsByStatus(
                        BedStatus.AVAILABLE
                )
        );
    }



    @Transactional(readOnly = true)
    public Page<RoomResponseDTO> getRooms(
            String number,
            Integer floor,
            RoomType roomType,
            RoomStatus status,
            Long wardId,
            Long departmentId,
            Pageable pageable
    ) {
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        if (floor != null) {
            helper.validateFloor(floor);
        }

        if (roomType != null) {
            helper.validateRoomType(roomType);
        }

        if (status != null) {
            helper.validateRoomStatus(status);
        }

        if (wardId != null) {
            helper.validateWardExists(wardId);
        }

        if (departmentId != null) {
            helper.validateDepartmentExist(departmentId);
        }

        String normalizedNumber =
                normalizeOptionalFilter(number);

        log.info(
                "Fetching rooms with filters: "
                        + "number={}, floor={}, roomType={}, "
                        + "status={}, wardId={}, departmentId={}",
                normalizedNumber,
                floor,
                roomType,
                status,
                wardId,
                departmentId
        );

        Specification<Room> specification =
                numberContains(normalizedNumber)
                        .and(hasFloor(floor))
                        .and(hasRoomType(roomType))
                        .and(hasStatus(status))
                        .and(belongsToWard(wardId))
                        .and(belongsToDepartment(
                                departmentId
                        ));

        return roomRepository
                .findAll(specification, pageable)
                .map(roomMapper::toResponseDTO);
    }

    private String normalizeOptionalFilter(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }



}
