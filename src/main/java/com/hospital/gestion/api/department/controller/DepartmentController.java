package com.hospital.gestion.api.department.controller;


import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.department.dto.DepartmentRequestDTO;
import com.hospital.gestion.api.department.dto.DepartmentResponseDTO;
import com.hospital.gestion.api.department.dto.DepartmentUpdateDTO;
import com.hospital.gestion.api.department.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;


import com.hospital.gestion.api.common.dto.PageResponseDTO;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class DepartmentController {

    private final DepartmentService departmentService;

    //======
    //CREATE
    //=======

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponseDTO> createDepartment(@Valid @RequestBody DepartmentRequestDTO request){

        log.info("REST request  to create department with type: {}",request.departmentType());

        DepartmentResponseDTO response = departmentService.createDepartment(request);
        URI location= ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }


    //=========0
    //READ
    //======

    @GetMapping
    public ResponseEntity<List<DepartmentResponseDTO>> getAllDepartments(){
        return ResponseEntity.ok(departmentService.getAllDepartments());

    }



    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponseDTO> getDepartmentById(@PathVariable("id") Long id){
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @GetMapping("/type/{departmentType}")
    public ResponseEntity<DepartmentResponseDTO> getDepartmentByType(@PathVariable("departmentType") DepartmentType  departmentType){

       return ResponseEntity.ok(departmentService.getDepartmentByType(departmentType));
    }



    @GetMapping("/active/type/{departmentType}")
    public ResponseEntity<DepartmentResponseDTO>
    getActiveDepartmentByType(
            @PathVariable DepartmentType departmentType
    ) {
        return ResponseEntity.ok(
                departmentService.getActiveDepartmentActiveByType(
                        departmentType
                )
        );
    }

    @GetMapping("/status")
    public ResponseEntity<List<DepartmentResponseDTO>>
    getDepartmentsByActiveStatus(
            @RequestParam Boolean isActive
    ) {
        return ResponseEntity.ok(
                departmentService.getDepartmentByActiveStatus(
                        isActive
                )
        );
    }



    @GetMapping("/active/ordered")
    public ResponseEntity<List<DepartmentResponseDTO>>
    getActiveDepartmentsOrdered() {
        return ResponseEntity.ok(
                departmentService.getActiveDepartmentsOrdered()
        );
    }

    @GetMapping("/ordered")
    public ResponseEntity<List<DepartmentResponseDTO>>
    getAllDepartmentsOrderedByType() {
        return ResponseEntity.ok(
                departmentService.getAllDepartmentsOrderedByType()
        );
    }


    //=========
    //Location
    //============0
    @GetMapping("/location")
    public ResponseEntity<DepartmentResponseDTO>
    getDepartmentByLocation(
            @RequestParam String location
    ) {
        return ResponseEntity.ok(
                departmentService.getDepartmentByLocation(location)
        );
    }

    @GetMapping("/search/location")
    public ResponseEntity<List<DepartmentResponseDTO>>
    searchDepartmentsByLocation(
            @RequestParam String location
    ) {
        return ResponseEntity.ok(
                departmentService.getDepartmentsByLocation(location)
        );
    }


    //======
    //Description
    //===========
    @GetMapping("/search/description")
    public ResponseEntity<List<DepartmentResponseDTO>>
    searchDepartmentsByDescription(
            @RequestParam String description
    ) {
        return ResponseEntity.ok(
                departmentService.getDepartmentsByDescription(
                        description
                )
        );
    }


    //=====
    //UPDATE
    //==========

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponseDTO> updateDepartment(@PathVariable("id") Long id, @Valid @RequestBody DepartmentUpdateDTO request){

        log.info("REST request to update department with id: {}",id);
        return ResponseEntity.ok(departmentService.updateDepartmentById(id, request));


    }


    //=====
    //DELETE
    //==========


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDepartment(@PathVariable("id") Long id){

        log.info("REST request to delete department with id: {}",id);
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }


    //=======
    //AACTIVATE/DEACTIVATE
    //=============


    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateDepartment(@PathVariable("id") Long id){
        log.info("REST request to activate department with id: {}",id);
        departmentService.activateDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateDepartment(@PathVariable("id") Long id){
        log.info("REST request to deactivate department with id: {}",id);
        departmentService.deactivateDepartment(id);
        return ResponseEntity.noContent().build();
    }


    //======
    //EXISTS
    //===========
    @GetMapping("/exists/type/{departmentType}")
    public ResponseEntity<Boolean> existsByDepartmentType(
            @PathVariable DepartmentType departmentType
    ) {
        return ResponseEntity.ok(
                departmentService.existsByDepartmentType(
                        departmentType
                )
        );
    }


    @GetMapping("/exists/active/type/{departmentType}")
    public ResponseEntity<Boolean> existsActiveByDepartmentType(
            @PathVariable DepartmentType departmentType
    ) {
        return ResponseEntity.ok(
                departmentService.existsActiveByDepartmentType(
                        departmentType
                )
        );
    }

    // ========================================
    // COUNT
    // ========================================

    @GetMapping("/count")
    public ResponseEntity<Long> countDepartmentsByActiveStatus(
            @RequestParam Boolean isActive
    ) {
        return ResponseEntity.ok(
                departmentService.countDepartmentsByActiveStatus(
                        isActive
                )
        );
    }

    @GetMapping("/count/active")
    public ResponseEntity<Long> countActiveDepartments() {
        return ResponseEntity.ok(
                departmentService.countActiveDepartments()
        );
    }


    // ========================================
    // WARDS
    // ========================================

    @GetMapping("/{id}/with-wards")
    public ResponseEntity<DepartmentResponseDTO>
    getDepartmentWithWardsById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                departmentService.getDepartmentWithWardsById(id)
        );
    }

    @GetMapping("/with-wards")
    public ResponseEntity<List<DepartmentResponseDTO>>
    getAllDepartmentsWithWards() {
        return ResponseEntity.ok(
                departmentService.getAllDepartmentsWithWards()
        );
    }


    @GetMapping("/page")
    public ResponseEntity<
            PageResponseDTO<DepartmentResponseDTO>
            >
    getDepartmentsPaginated(
            @RequestParam(required = false)
            DepartmentType departmentType,

            @RequestParam(required = false)
            Boolean isActive,

            @RequestParam(required = false)
            String location,

            @RequestParam(required = false)
            String description,

            @PageableDefault(
                    size = 20,
                    sort = "departmentType",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        Page<DepartmentResponseDTO> result =
                departmentService.getDepartments(
                        departmentType,
                        isActive,
                        location,
                        description,
                        pageable
                );

        return ResponseEntity.ok(
                PageResponseDTO.from(result)
        );
    }



}
