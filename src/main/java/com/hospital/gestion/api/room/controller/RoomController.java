package com.hospital.gestion.api.room.controller;


import com.hospital.gestion.api.common.dto.PageResponseDTO;
import com.hospital.gestion.api.common.enums.RoomStatus;
import com.hospital.gestion.api.common.enums.RoomType;
import com.hospital.gestion.api.room.dto.RoomRequestDTO;
import com.hospital.gestion.api.room.dto.RoomResponseDTO;
import com.hospital.gestion.api.room.dto.RoomStatsResponse;
import com.hospital.gestion.api.room.dto.RoomStatusUpdateDTO;
import com.hospital.gestion.api.room.dto.RoomUpdateDTO;
import com.hospital.gestion.api.room.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class RoomController {

    private final RoomService roomService;

    private static final String WITH_PAGINATION = " with pagination";



//=========
//create
//=============

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponseDTO> createRoom(@Valid @RequestBody RoomRequestDTO request){

        log.info("REST request to create room : {}", request.number());
        try {

            RoomResponseDTO response = roomService.createRoom(request);

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(response.id())
                    .toUri();

            return ResponseEntity.created(location).body(response);

        }catch (Exception e){
            log.error("Error creating room: {}", e.getMessage(), e);
            throw e;
        }

    }


    //=======
    //GET ALL
    //==========00
    @GetMapping
    public ResponseEntity<List<RoomResponseDTO>> getAllRooms(){
        log.info("REST request to get all rooms");
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/page")
    public ResponseEntity<PageResponseDTO<RoomResponseDTO>>
    getRoomsPaginated(
            @RequestParam(required = false)
            String number,

            @RequestParam(required = false)
            Integer floor,

            @RequestParam(required = false)
            RoomType roomType,

            @RequestParam(required = false)
            RoomStatus status,

            @RequestParam(required = false)
            Long wardId,

            @RequestParam(required = false)
            Long departmentId,

            @PageableDefault(
                    size = 20,
                    sort = "number",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get rooms with filters: "
                        + "number={}, floor={}, roomType={}, "
                        + "status={}, wardId={}, "
                        + "departmentId={}, pageable={}",
                number,
                floor,
                roomType,
                status,
                wardId,
                departmentId,
                pageable
        );

        Page<RoomResponseDTO> result =
                roomService.getRooms(
                        number,
                        floor,
                        roomType,
                        status,
                        wardId,
                        departmentId,
                        pageable
                );

        return ResponseEntity.ok(
                PageResponseDTO.from(result)
        );
    }
    //=======
    //GET BY ID
    //============

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<RoomResponseDTO> getRoomById(@PathVariable("id") Long id){
        log.info("REST request to get room by id: {}", id);
        return ResponseEntity.ok(roomService.getRoomById(id));
    }
    // =====================
    // GET BY NUMBER
    // ==================

    @GetMapping("/number/{number}")
    public ResponseEntity<RoomResponseDTO> getRoomByNumber(@PathVariable("number") String number){
        log.info("REST request  to get  room by number: {}", number);
        return ResponseEntity.ok(roomService.getRoomByNumber(number));
    }





    // =========================
    // GET BY STATUS
    // ========================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<RoomResponseDTO>> getRoomsByStatus(@PathVariable("status") RoomStatus status){
        log.info("REST request to get rooms by status: {}", status);

        return ResponseEntity.ok(roomService.getRoomsByStatus(status));
    }


    @GetMapping("/status/{status}/page")
    public ResponseEntity<Page<RoomResponseDTO>>
    getRoomsByStatusPaginated(
            @PathVariable RoomStatus status,
            @PageableDefault(
                    size = 20,
                    sort = "number"
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get rooms by status: {} "
                        + WITH_PAGINATION,
                status
        );

        return ResponseEntity.ok(
                roomService.getRoomsByStatus(
                        status,
                        pageable
                )
        );
    }


    //==========
    //GET BY TYPE
    //================
    @GetMapping("/type/{roomType}")
    public ResponseEntity<List<RoomResponseDTO>> getRoomsByRoomType(@PathVariable("roomType") RoomType roomType) {

        log.info("REST request to get rooms by Type: {}", roomType);
        return ResponseEntity.ok(roomService.getRoomsByType(roomType));
    }


    @GetMapping("/type/{roomType}/page")
    public ResponseEntity<Page<RoomResponseDTO>>
    getRoomsByTypePaginated(
            @PathVariable RoomType roomType,
            @PageableDefault(
                    size = 20,
                    sort = "number"
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get rooms by type: {} "
                        + WITH_PAGINATION,
                roomType
        );

        return ResponseEntity.ok(
                roomService.getRoomsByType(
                        roomType,
                        pageable
                )
        );
    }




    //=========
    //GET FLOOR
    //===========
    @GetMapping("/floor/{floor}")
    public ResponseEntity<List<RoomResponseDTO>> getRoomsByFloor(@PathVariable("floor") Integer floor){
        log.info("REST request to get rooms by floor: {}", floor);
        return ResponseEntity.ok(roomService.getRoomsByFloor(floor));
    }


    @GetMapping("/floor/{floor}/page")
    public ResponseEntity<Page<RoomResponseDTO>>
    getRoomsByFloorPaginated(
            @PathVariable Integer floor,
            @PageableDefault(
                    size = 20,
                    sort = "number"
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get rooms by floor: {} "
                        + "with pagination",
                floor
        );

        return ResponseEntity.ok(
                roomService.getRoomsByFloor(
                        floor,
                        pageable
                )
        );
    }


    //=======
    //GET BY WARD  AND STATUS
    //============

    @GetMapping("/ward/{wardId}/status/{status}")
    public ResponseEntity<List<RoomResponseDTO>> getRoomsByWardAndStatus(@PathVariable("wardId") Long wardId, @PathVariable("status") RoomStatus status) {
        log.info("REST request to get rooms by ward: {} and status: {}", wardId, status);

        return ResponseEntity.ok(roomService.getRoomsByWardAndStatus(wardId, status));
    }


    @GetMapping("/ward/{wardId}/status/{status}/page")
    public ResponseEntity<Page<RoomResponseDTO>>
    getRoomsByWardAndStatusPaginated(
            @PathVariable Long wardId,
            @PathVariable RoomStatus status,
            @PageableDefault(
                    size = 20,
                    sort = "number"
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get rooms by ward: {}, "
                        + "status: {} with pagination",
                wardId,
                status
        );

        return ResponseEntity.ok(
                roomService.getRoomsByWardAndStatus(
                        wardId,
                        status,
                        pageable
                )
        );
    }


    //==================0
    //GET BY WARD
    //=========================


    @GetMapping("/ward/{wardId}")
    public ResponseEntity<List<RoomResponseDTO>> getRoomsByWard(
            @PathVariable Long wardId
    ) {
        log.info(
                "REST request to get rooms by ward: {}",
                wardId
        );

        return ResponseEntity.ok(
                roomService.getRoomsByWardId(wardId)
        );
    }


    @GetMapping("/ward/{wardId}/page")
    public ResponseEntity<Page<RoomResponseDTO>>
    getRoomsByWardPaginated(
            @PathVariable Long wardId,
            @PageableDefault(size = 20, sort = "number")
            Pageable pageable
    ) {
        log.info(
                "REST request to get rooms by ward: {} with pagination",
                wardId
        );

        return ResponseEntity.ok(
                roomService.getRoomsByWard(
                        wardId,
                        pageable
                )
        );
    }



    //========
    //GET BY DEPARTMENT
    //=========================

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<RoomResponseDTO>> getRoomsByDepartment(@PathVariable("departmentId")  Long departmentId){
        log.info("REST request to get rooms by department: {}", departmentId);


        return ResponseEntity.ok(roomService.getRoomsByDepartment(departmentId));
    }

    @GetMapping("/department/{departmentId}/page")
    public ResponseEntity<Page<RoomResponseDTO>>
    getRoomsByDepartmentPaginated(
            @PathVariable Long departmentId,
            @PageableDefault(
                    size = 20,
                    sort = "number"
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get rooms by department: {} "
                        + "with pagination",
                departmentId
        );

        return ResponseEntity.ok(
                roomService.getRoomsByDepartment(
                        departmentId,
                        pageable
                )
        );
    }










    //=============================
    //get by department and status
    //==============================
    @GetMapping("/department/{departmentId}/status/{status}")
    public ResponseEntity<List<RoomResponseDTO>> getRoomsByDepartmentAndStatus(@PathVariable("departmentId") Long departmentId, @PathVariable("status") RoomStatus status) {
        log.info("REST request to get rooms by department: {}, status: {}", departmentId, status);

        return ResponseEntity.ok(roomService.getRoomsByDepartmentAndStatus(departmentId, status));
    }


    @GetMapping(
            "/department/{departmentId}/status/{status}/page"
    )
    public ResponseEntity<Page<RoomResponseDTO>>
    getRoomsByDepartmentAndStatusPaginated(
            @PathVariable Long departmentId,
            @PathVariable RoomStatus status,
            @PageableDefault(
                    size = 20,
                    sort = "number"
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get rooms by department: {}, "
                        + "status: {} with pagination",
                departmentId,
                status
        );

        return ResponseEntity.ok(
                roomService.getRoomsByDepartmentAndStatus(
                        departmentId,
                        status,
                        pageable
                )
        );
    }


    //=========0
    //UPDATE
    //==============
    @PutMapping("/{id:[0-9]+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponseDTO> updateRoom(@PathVariable("id") Long id,@Valid @RequestBody RoomUpdateDTO request){
        log.info("REST request to update room: {}", id);

        return ResponseEntity.ok(roomService.updateRoomById(id, request));
    }


    // ========================================
    // UPDATE STATUS
    // ========================================


    @PatchMapping("/{id:[0-9]+}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    public ResponseEntity<RoomResponseDTO> updateRoomStatus(
            @PathVariable Long id,
            @Valid @RequestBody RoomStatusUpdateDTO request
    ) {
        log.info(
                "REST request to update status of room: {}",
                id
        );

        return ResponseEntity.ok(
                roomService.updateRoomStatus(id, request)
        );
    }



    //========
    //DELETE
    //============
    @DeleteMapping("/{id:[0-9]+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable("id") Long id){
        log.info("REST request to delete room: {}", id);
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }


    // ========================================
    // EXISTS
    // ========================================

    @GetMapping("/exists/number")
    public ResponseEntity<Boolean> existsByNumber(
            @RequestParam String number
    ) {
        log.info(
                "REST request to check room number: {}",
                number
        );

        return ResponseEntity.ok(
                roomService.existsByNumber(number)
        );
    }

    // ========================================
    // STATISTICS
    // ========================================

    @GetMapping("/stats")
    public ResponseEntity<RoomStatsResponse> getRoomStats() {
        log.info("REST request to get room statistics");

        return ResponseEntity.ok(
                roomService.getRoomStats()
        );
    }


    // ========================================
    // COUNT ALL
    // ========================================

    @GetMapping("/count")
    public ResponseEntity<Long> countAllRooms() {
        return ResponseEntity.ok(
                roomService.countAllRooms()
        );
    }



    // ========================================
    // COUNT BY STATUS
    // ========================================

    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countRoomsByStatus(
            @PathVariable RoomStatus status
    ) {
        return ResponseEntity.ok(
                roomService.countRoomsByStatus(status)
        );
    }

    // ========================================
    // COUNT BY TYPE
    // ========================================

    @GetMapping("/count/type/{roomType}")
    public ResponseEntity<Long> countRoomsByType(
            @PathVariable RoomType roomType
    ) {
        return ResponseEntity.ok(
                roomService.countRoomsByType(roomType)
        );
    }



    // ========================================
    // COUNT BY FLOOR
    // ========================================

    @GetMapping("/count/floor/{floor}")
    public ResponseEntity<Long> countRoomsByFloor(
            @PathVariable Integer floor
    ) {
        return ResponseEntity.ok(
                roomService.countRoomsByFloor(floor)
        );
    }

    // ========================================
    // COUNT BY WARD
    // ========================================

    @GetMapping("/count/ward/{wardId}")
    public ResponseEntity<Long> countRoomsByWard(
            @PathVariable Long wardId
    ) {
        return ResponseEntity.ok(
                roomService.countRoomsByWard(wardId)
        );
    }

    // ========================================
    // COUNT BY WARD AND STATUS
    // ========================================

    @GetMapping(
            "/count/ward/{wardId}/status/{status}"
    )
    public ResponseEntity<Long> countRoomsByWardAndStatus(
            @PathVariable Long wardId,
            @PathVariable RoomStatus status
    ) {
        return ResponseEntity.ok(
                roomService.countRoomsByWardAndStatus(
                        wardId,
                        status
                )
        );
    }

    // ========================================
    // COUNT BY DEPARTMENT
    // ========================================

    @GetMapping("/count/department/{departmentId}")
    public ResponseEntity<Long> countRoomsByDepartment(
            @PathVariable Long departmentId
    ) {
        return ResponseEntity.ok(
                roomService.countRoomsByDepartment(
                        departmentId
                )
        );
    }

    // ========================================
    // COUNT BY DEPARTMENT AND STATUS
    // ========================================

    @GetMapping(
            "/count/department/{departmentId}/status/{status}"
    )
    public ResponseEntity<Long>
    countRoomsByDepartmentAndStatus(
            @PathVariable Long departmentId,
            @PathVariable RoomStatus status
    ) {
        return ResponseEntity.ok(
                roomService.countRoomsByDepartmentAndStatus(
                        departmentId,
                        status
                )
        );
    }

}
