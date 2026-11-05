package pl.omnisport.api.member;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import pl.omnisport.api.attendance.AttendanceService;
import pl.omnisport.api.auth.MemberRegisterRequest;
import pl.omnisport.api.contracts.OldCoachingContractResponse;
import pl.omnisport.api.payment.PaymentRequest;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name="Members", description = "Member management")
public class MemberController {
    private final MemberService memberService;
    private final AttendanceService attendanceService;

    //CREATE
    @Operation(summary = "Add new member")
    @PostMapping("/add-new-member")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<Void> addNewMember(
            @Valid @RequestBody MemberRegisterRequest request){
        memberService.saveNewMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //READ
    @Operation(summary = "Get all members")
    @GetMapping("/get-all-members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<MemberResponse>> getAllMembers(
            @ParameterObject Pageable pageable){
        return ResponseEntity.ok(memberService.getAllMembers(pageable));
    }

    @Operation(summary = "Get member by ID")
    @GetMapping("/{id}/get-member-by-id")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberGetByIdResponse> getMemberById(
            @PathVariable Long id) throws EntityNotFoundException{
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    @Operation(summary = "Get current member contracts")
    @GetMapping("/{memberId}/get-current-contracts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OldCoachingContractResponse>> getCurrentMemberContracts(
            @PathVariable Long memberId,
            @ParameterObject Pageable pageable) throws EntityNotFoundException{
        return  ResponseEntity.ok(memberService.getCurrentMemberContracts(memberId, pageable));
    }

    //UPDATE
    @Operation(summary = "Update member section")
    @PutMapping("/{id}/update-member-section")
    public ResponseEntity<Void> updateMembersSection(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSectionRequest request){
        memberService.updateMembersSection(id, request.newSection());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Extend pass validity")
    @PatchMapping("/{id}/extend-member-pass")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<Void> extendPassValidity(
            @PathVariable Long id,
            @RequestBody PaymentRequest request){
        memberService.extendPassValidity(id, request.amount());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Register check in")
    @PatchMapping("{memberId}/register-check-in")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> registerCheckIn(
            @PathVariable Long memberId){
        attendanceService.registerCheckIn(memberId);
        return ResponseEntity.ok().build();
    }

    //DELETE
    @Operation(summary = "Deactivate member")
    @PatchMapping("/{id}/deactivate-member")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateMember(
            @PathVariable Long id){
        memberService.deactivateMember(id);
        return ResponseEntity.ok().build();
    }
}
