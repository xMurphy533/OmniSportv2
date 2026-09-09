package pl.omnisport.api.member;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Pageable;
import pl.omnisport.api.auth.MemberRegisterRequest;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    //CREATE
    @PostMapping("/add-new-member")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<Void> addNewMember(@Valid @RequestBody MemberRegisterRequest request){
        memberService.saveNewMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //READ
    @GetMapping("/get-all-members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<MemberResponse>> getAllMembers(Pageable pageable){
        return ResponseEntity.ok(memberService.getAllMembers(pageable));
    }

    @GetMapping("/{id}/get-member-by-id")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberResponse> getMemberById(@PathVariable Long id) throws EntityNotFoundException{
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    //UPDATE
    @PutMapping("/{id}/update-member-section")
    public ResponseEntity<Void> updateMembersSection(@PathVariable Long id, @Valid @RequestBody UpdateSectionRequest request){
        memberService.updateMembersSection(id, request.newSection());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/extend-member-pass")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<Void> extendPassValidity(@PathVariable Long id){
        try{
            memberService.extendPassValidity(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found");
        }
    }

    @PatchMapping("/{memberId}/{newCoachId}/change-member-coach")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeMembersCoach(@PathVariable Long newCoachId, @PathVariable Long memberId){
        memberService.changeMembersCoach(newCoachId, memberId);
        return ResponseEntity.ok().build();
    }

    //DELETE
    @PatchMapping("/{id}/deactivate-member")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateMember(@PathVariable Long id){
        memberService.deactivateMember(id);
        return ResponseEntity.ok().build();
    }
}
