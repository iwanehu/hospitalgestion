package com.hospital.gestion.api.bed.service;

import com.hospital.gestion.api.bed.dto.BedRequestDTO;
import com.hospital.gestion.api.bed.dto.BedResponseDTO;
import com.hospital.gestion.api.bed.dto.BedUpdateDTO;
import com.hospital.gestion.api.bed.entity.Bed;
import com.hospital.gestion.api.bed.mapper.BedMapper;
import com.hospital.gestion.api.bed.repository.BedRepository;
import com.hospital.gestion.api.common.enums.BedStatus;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.exception.ResourceNotFoundException;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;
import com.hospital.gestion.api.room.entity.Room;
import com.hospital.gestion.api.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

import static com.hospital.gestion.api.bed.specification.BedSpecification.belongsToDepartment;
import static com.hospital.gestion.api.bed.specification.BedSpecification.belongsToRoom;
import static com.hospital.gestion.api.bed.specification.BedSpecification.belongsToWard;
import static com.hospital.gestion.api.bed.specification.BedSpecification.hasStatus;
import static com.hospital.gestion.api.bed.specification.BedSpecification.numberContains;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BedService {








    private final BedRepository bedRepository;
    private final RoomRepository roomRepository;

    private final BedMapper bedMapper;
    private final HospitalEntityHelper helper;


    private static final Set<String>
            ALLOWED_SORT_PROPERTIES = Set.of(
            "id",
            "bedNumber",
            "status",
            "room.id",
            "room.number",
            "room.ward.id",
            "room.ward.department.id",
            "createdAt",
            "updatedAt"
    );

    // ========================================
    // CREATE
    // ========================================

    @Transactional
    public BedResponseDTO createBed(
            BedRequestDTO request
    ) {
        log.info(
                "Creating bed: {} in room: {}",
                request.bedNumber(),
                request.roomId()
        );

        helper.validateBedNumber(request.bedNumber());

        String normalizedNumber =
                request.bedNumber().trim();

        Room room = helper.findRoomByIdForUpdate(
                request.roomId()
        );

        if (bedRepository
                .existsByBedNumberIgnoreCaseAndRoom_Id(
                        normalizedNumber,
                        room.getId()
                )) {
            throw new ConflictException(
                    "Bed number already exists in room: "
                            + normalizedNumber
            );
        }

        long currentBeds =
                bedRepository.countByRoom_Id(room.getId());

        if (room.getCapacity() == null
                || currentBeds >= room.getCapacity()) {
            throw new ConflictException(
                    "Room has reached its maximum bed capacity"
            );
        }

        Bed bed = bedMapper.toEntity(request, room);

        bed.setBedNumber(normalizedNumber);
        bed.setNotes(
                helper.normalizeNullableText(request.notes())
        );

        Bed savedBed = bedRepository.save(bed);

        log.info(
                "Bed created successfully with id: {}",
                savedBed.getId()
        );

        return bedMapper.toResponseDTO(savedBed);
    }

    // ========================================
    // GET ALL
    // ========================================

    @Transactional(readOnly = true)
    public List<BedResponseDTO> getAllBeds() {
        log.info("Fetching all beds");

        return bedMapper.toResponseDTOList(
                bedRepository.findAll()
        );
    }

    @Transactional(readOnly = true)
    public Page<BedResponseDTO> getAllBeds(
            Pageable pageable
    ) {

        helper.validatePageable(pageable,ALLOWED_SORT_PROPERTIES);

        return bedRepository.findAll(pageable)
                .map(bedMapper::toResponseDTO);
    }

    // ========================================
    // GET BY ID
    // ========================================

    @Transactional(readOnly = true)
    public BedResponseDTO getBedById(Long id) {
        log.info("Fetching bed by id: {}", id);

        return bedMapper.toResponseDTO(
                helper.findBedById(id)
        );
    }

    // ========================================
    // GET BY EXACT NUMBER AND ROOM
    // ========================================

    @Transactional(readOnly = true)
    public BedResponseDTO getBedByNumberAndRoom(
            String bedNumber,
            Long roomId
    ) {
        helper.validateBedNumber(bedNumber);
        helper.validateRoomExists(roomId);

        String normalizedNumber =
                bedNumber.trim();

        Bed bed = bedRepository
                .findByBedNumberIgnoreCaseAndRoom_Id(
                        normalizedNumber,
                        roomId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bed not found with number: "
                                        + normalizedNumber
                                        + " in room: "
                                        + roomId
                        )
                );

        return bedMapper.toResponseDTO(bed);
    }

    // ========================================
    // SEARCH BY NUMBER
    // ========================================

    @Transactional(readOnly = true)
    public List<BedResponseDTO> searchBedsByNumber(
            String bedNumber
    ) {
        helper.validateBedNumber(bedNumber);

        return bedMapper.toResponseDTOList(
                bedRepository
                        .findByBedNumberContainingIgnoreCase(
                                bedNumber.trim()
                        )
        );
    }

    @Transactional(readOnly = true)
    public Page<BedResponseDTO> searchBedsByNumber(
            String bedNumber,
            Pageable pageable
    ) {
        helper.validateBedNumber(bedNumber);
        helper.validatePageable(pageable,ALLOWED_SORT_PROPERTIES);


        return bedRepository
                .findByBedNumberContainingIgnoreCase(
                        bedNumber.trim(),
                        pageable
                )
                .map(bedMapper::toResponseDTO);
    }

    // ========================================
    // GET BY ROOM
    // ========================================

    @Transactional(readOnly = true)
    public List<BedResponseDTO> getBedsByRoom(
            Long roomId
    ) {
        helper.validateRoomExists(roomId);

        return bedMapper.toResponseDTOList(
                bedRepository.findByRoom_Id(roomId)
        );
    }

    @Transactional(readOnly = true)
    public Page<BedResponseDTO> getBedsByRoom(
            Long roomId,
            Pageable pageable
    ) {
        helper.validateRoomExists(roomId);
        helper.validatePageable(pageable,ALLOWED_SORT_PROPERTIES);

        return bedRepository
                .findByRoom_Id(roomId, pageable)
                .map(bedMapper::toResponseDTO);
    }

    // ========================================
    // GET BY STATUS
    // =============================validatePage===========

    @Transactional(readOnly = true)
    public List<BedResponseDTO> getBedsByStatus(
            BedStatus status
    ) {
        helper.validateBedStatus(status);

        return bedMapper.toResponseDTOList(
                bedRepository.findByStatus(status)
        );
    }

    @Transactional(readOnly = true)
    public Page<BedResponseDTO> getBedsByStatus(
            BedStatus status,
            Pageable pageable
    ) {
        helper.validateBedStatus(status);
        helper.validatePageable(pageable,ALLOWED_SORT_PROPERTIES);

        return bedRepository
                .findByStatus(status, pageable)
                .map(bedMapper::toResponseDTO);
    }

    // ========================================
    // GET BY ROOM AND STATUS
    // ========================================

    @Transactional(readOnly = true)
    public List<BedResponseDTO> getBedsByRoomAndStatus(
            Long roomId,
            BedStatus status
    ) {
        helper.validateRoomExists(roomId);
        helper.validateBedStatus(status);

        return bedMapper.toResponseDTOList(
                bedRepository.findByRoom_IdAndStatus(
                        roomId,
                        status
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<BedResponseDTO> getBedsByRoomAndStatus(
            Long roomId,
            BedStatus status,
            Pageable pageable
    ) {
        helper.validateRoomExists(roomId);
        helper.validateBedStatus(status);
        helper.validatePageable(pageable,ALLOWED_SORT_PROPERTIES);

        return bedRepository
                .findByRoom_IdAndStatus(
                        roomId,
                        status,
                        pageable
                )
                .map(bedMapper::toResponseDTO);
    }

    // ========================================
    // GET BY WARD
    // ========================================

    @Transactional(readOnly = true)
    public List<BedResponseDTO> getBedsByWard(
            Long wardId
    ) {
        helper.validateWardExists(wardId);

        return bedMapper.toResponseDTOList(
                bedRepository.findByRoom_Ward_Id(wardId)
        );
    }

    @Transactional(readOnly = true)
    public Page<BedResponseDTO> getBedsByWard(
            Long wardId,
            Pageable pageable
    ) {
        helper.validateWardExists(wardId);
        helper.validatePageable(pageable,ALLOWED_SORT_PROPERTIES);

        return bedRepository
                .findByRoom_Ward_Id(wardId, pageable)
                .map(bedMapper::toResponseDTO);
    }

    // ========================================
    // GET BY WARD AND STATUS
    // ========================================

    @Transactional(readOnly = true)
    public List<BedResponseDTO> getBedsByWardAndStatus(
            Long wardId,
            BedStatus status
    ) {
        helper.validateWardExists(wardId);
        helper.validateBedStatus(status);

        return bedMapper.toResponseDTOList(
                bedRepository
                        .findByRoom_Ward_IdAndStatus(
                                wardId,
                                status
                        )
        );
    }

    @Transactional(readOnly = true)
    public Page<BedResponseDTO> getBedsByWardAndStatus(
            Long wardId,
            BedStatus status,
            Pageable pageable
    ) {
        helper.validateWardExists(wardId);
        helper.validateBedStatus(status);
        helper.validatePageable(pageable,ALLOWED_SORT_PROPERTIES);

        return bedRepository
                .findByRoom_Ward_IdAndStatus(
                        wardId,
                        status,
                        pageable
                )
                .map(bedMapper::toResponseDTO);
    }

    // ========================================
    // GET BY DEPARTMENT
    // ========================================

    @Transactional(readOnly = true)
    public List<BedResponseDTO> getBedsByDepartment(
            Long departmentId
    ) {
        helper.validateDepartmentExist(departmentId);

        return bedMapper.toResponseDTOList(
                bedRepository
                        .findByRoom_Ward_Department_Id(
                                departmentId
                        )
        );
    }

    @Transactional(readOnly = true)
    public Page<BedResponseDTO> getBedsByDepartment(
            Long departmentId,
            Pageable pageable
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validatePageable(pageable,ALLOWED_SORT_PROPERTIES);

        return bedRepository
                .findByRoom_Ward_Department_Id(
                        departmentId,
                        pageable
                )
                .map(bedMapper::toResponseDTO);
    }

    // ========================================
    // GET BY DEPARTMENT AND STATUS
    // ========================================

    @Transactional(readOnly = true)
    public List<BedResponseDTO>
    getBedsByDepartmentAndStatus(
            Long departmentId,
            BedStatus status
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validateBedStatus(status);

        return bedMapper.toResponseDTOList(
                bedRepository
                        .findByRoom_Ward_Department_IdAndStatus(
                                departmentId,
                                status
                        )
        );
    }

    @Transactional(readOnly = true)
    public Page<BedResponseDTO>
    getBedsByDepartmentAndStatus(
            Long departmentId,
            BedStatus status,
            Pageable pageable
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validateBedStatus(status);
        helper.validatePageable(pageable,ALLOWED_SORT_PROPERTIES);

        return bedRepository
                .findByRoom_Ward_Department_IdAndStatus(
                        departmentId,
                        status,
                        pageable
                )
                .map(bedMapper::toResponseDTO);
    }

    // ========================================
    // UPDATE
    // ========================================

    @Transactional
    public BedResponseDTO updateBedById(
            Long id,
            BedUpdateDTO request
    ) {
        log.info("Updating bed by id: {}", id);

        Bed bed = helper.findBedById(id);

        helper.validateBedNumber(request.bedNumber());

        String normalizedNumber =
                request.bedNumber().trim();

        validateBedNumberForUpdate(
                bed,
                normalizedNumber
        );

        bedMapper.updateEntity(bed, request);

        bed.setBedNumber(normalizedNumber);
        bed.setNotes(
                helper.normalizeNullableText(request.notes())
        );

        Bed updatedBed = bedRepository.save(bed);

        log.info(
                "Bed updated successfully with id: {}",
                updatedBed.getId()
        );

        return bedMapper.toResponseDTO(updatedBed);
    }

    private void validateBedNumberForUpdate(
            Bed bed,
            String newNumber
    ) {
        String currentNumber = bed.getBedNumber();

        boolean numberChanged =
                currentNumber == null
                        || !newNumber.equalsIgnoreCase(
                        currentNumber.trim()
                );

        if (numberChanged
                && bedRepository
                .existsByBedNumberIgnoreCaseAndRoom_Id(
                        newNumber,
                        bed.getRoom().getId()
                )) {
            throw new ConflictException(
                    "Bed number already exists in room: "
                            + newNumber
            );
        }
    }

    // ========================================
    // RESERVE
    // ========================================

    @Transactional
    public BedResponseDTO reserveBed(Long id) {
        Bed bed = helper.findBedByIdForUpdate(id);

        executeStatusTransition(
                bed::reserve
        );

        return bedMapper.toResponseDTO(bed);
    }

    // ========================================
    // OCCUPY
    // ========================================

    @Transactional
    public BedResponseDTO occupyBed(Long id) {
        Bed bed = helper.findBedByIdForUpdate(id);

        executeStatusTransition(
                bed::occupy
        );

        return bedMapper.toResponseDTO(bed);
    }

    // ========================================
    // RELEASE
    // ========================================

    @Transactional
    public BedResponseDTO releaseBed(Long id) {
        Bed bed = helper.findBedByIdForUpdate(id);

        executeStatusTransition(
                bed::free
        );

        return bedMapper.toResponseDTO(bed);
    }

    // ========================================
    // FINISH CLEANING
    // ========================================

    @Transactional
    public BedResponseDTO finishBedCleaning(Long id) {
        Bed bed = helper.findBedByIdForUpdate(id);

        executeStatusTransition(
                bed::finishCleaning
        );

        return bedMapper.toResponseDTO(bed);
    }

    // ========================================
    // MAINTENANCE
    // ========================================

    @Transactional
    public BedResponseDTO sendBedToMaintenance(
            Long id
    ) {
        Bed bed = helper.findBedByIdForUpdate(id);

        executeStatusTransition(
                bed::maintenance
        );

        return bedMapper.toResponseDTO(bed);
    }

    // ========================================
    // FINISH MAINTENANCE
    // ========================================

    @Transactional
    public BedResponseDTO finishBedMaintenance(
            Long id
    ) {
        Bed bed = helper.findBedByIdForUpdate(id);

        executeStatusTransition(
                bed::finishMaintenance
        );

        return bedMapper.toResponseDTO(bed);
    }

    // ========================================
    // CANCEL RESERVATION
    // ========================================

    @Transactional
    public BedResponseDTO cancelBedReservation(
            Long id
    ) {
        Bed bed = helper.findBedByIdForUpdate(id);

        executeStatusTransition(
                bed::cancelReservation
        );

        return bedMapper.toResponseDTO(bed);
    }

    // ========================================
    // DELETE
    // ========================================

    @Transactional
    public void deleteBed(Long id) {
        log.info("Deleting bed by id: {}", id);

        Bed bed = helper.findBedByIdForUpdate(id);

        if (bed.getStatus() == BedStatus.OCCUPIED
                || bed.getStatus() == BedStatus.RESERVED) {
            throw new ConflictException(
                    "Occupied or reserved bed cannot be deleted"
            );
        }

        if (bed.getAdmissions() != null
                && !bed.getAdmissions().isEmpty()) {
            throw new ConflictException(
                    "Bed cannot be deleted because it has "
                            + "admission history"
            );
        }

        bedRepository.delete(bed);

        log.info(
                "Bed deleted successfully with id: {}",
                id
        );
    }

    // ========================================
    // EXISTS
    // ========================================

    @Transactional(readOnly = true)
    public boolean existsByNumberAndRoom(
            String bedNumber,
            Long roomId
    ) {
        helper.validateBedNumber(bedNumber);
        helper.validateRoomExists(roomId);

        return bedRepository
                .existsByBedNumberIgnoreCaseAndRoom_Id(
                        bedNumber.trim(),
                        roomId
                );
    }

    // ========================================
    // COUNT
    // ========================================

    @Transactional(readOnly = true)
    public long countAllBeds() {
        return bedRepository.count();
    }

    @Transactional(readOnly = true)
    public long countBedsByStatus(
            BedStatus status
    ) {
        helper.validateBedStatus(status);

        return bedRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countBedsByRoom(
            Long roomId
    ) {
        helper.validateRoomExists(roomId);

        return bedRepository.countByRoom_Id(roomId);
    }

    @Transactional(readOnly = true)
    public long countBedsByRoomAndStatus(
            Long roomId,
            BedStatus status
    ) {
        helper.validateRoomExists(roomId);
        helper.validateBedStatus(status);

        return bedRepository.countByRoom_IdAndStatus(
                roomId,
                status
        );
    }

    @Transactional(readOnly = true)
    public long countBedsByWard(
            Long wardId
    ) {
        helper.validateWardExists(wardId);

        return bedRepository.countByRoom_Ward_Id(
                wardId
        );
    }

    @Transactional(readOnly = true)
    public long countBedsByWardAndStatus(
            Long wardId,
            BedStatus status
    ) {
        helper.validateWardExists(wardId);
        helper.validateBedStatus(status);

        return bedRepository
                .countByRoom_Ward_IdAndStatus(
                        wardId,
                        status
                );
    }

    @Transactional(readOnly = true)
    public long countBedsByDepartment(
            Long departmentId
    ) {
        helper.validateDepartmentExist(departmentId);

        return bedRepository
                .countByRoom_Ward_Department_Id(
                        departmentId
                );
    }

    @Transactional(readOnly = true)
    public long countBedsByDepartmentAndStatus(
            Long departmentId,
            BedStatus status
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validateBedStatus(status);

        return bedRepository
                .countByRoom_Ward_Department_IdAndStatus(
                        departmentId,
                        status
                );
    }

    // ========================================
    // PRIVATE HELPERS
    // ========================================








    private void executeStatusTransition(Runnable transition) {
        try {
            transition.run();
        } catch (IllegalStateException exception) {
            throw new ConflictException(exception.getMessage());
        }
    }




//=================================================







    @Transactional(readOnly = true)
    public Page<BedResponseDTO> getBeds(
            String bedNumber,
            BedStatus status,
            Long roomId,
            Long wardId,
            Long departmentId,
            Pageable pageable
    ) {
        helper.validatePageable(pageable,ALLOWED_SORT_PROPERTIES);

        if (status != null) {
            helper.validateBedStatus(status);
        }

        if (roomId != null) {
            helper.validateRoomExists(roomId);
        }

        if (wardId != null) {
            helper.validateWardExists(wardId);
        }

        if (departmentId != null) {
            helper.validateDepartmentExist(departmentId);
        }

        String normalizedBedNumber =
                normalizeOptionalFilter(bedNumber);

        log.info(
                "Fetching beds with filters: "
                        + "bedNumber={}, status={}, roomId={}, "
                        + "wardId={}, departmentId={}",
                normalizedBedNumber,
                status,
                roomId,
                wardId,
                departmentId
        );

        Specification<Bed> specification =
                numberContains(normalizedBedNumber)
                        .and(hasStatus(status))
                        .and(belongsToRoom(roomId))
                        .and(belongsToWard(wardId))
                        .and(belongsToDepartment(
                                departmentId
                        ));

        return bedRepository
                .findAll(specification, pageable)
                .map(bedMapper::toResponseDTO);
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