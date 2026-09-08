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
import pl.omnisport.api.coach.UpdateSpecializationRequest;

import java.util.Optional;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MemberMapper memberMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<MemberResponse>> getAllMembers(Pageable pageable){
        return ResponseEntity.ok(memberService.getAllMembers(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MemberResponse getMemberById(@PathVariable Long id) throws EntityNotFoundException{
        return memberService.getMemberById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public void addNewMember(@Valid @RequestBody MemberRegisterRequest request){
        memberService.saveNewMember(request);
    }

    @PutMapping("/{id}/section")
    public ResponseEntity<Void> updateMembersSection(@PathVariable Long id, @Valid @RequestBody UpdateSectionRequest request){
        memberService.updateMembersSection(id, request.newSection());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteMember(@PathVariable Long id){
        memberService.removeMember(id);
    }

    @PatchMapping("/{id}/extend-pass")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<Void> extendPassValidity(@PathVariable Long id){
        try{
            memberService.extendPassValidity(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found");
        }
    }

    @PatchMapping("/{memberId}/coach/{newCoachId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeMembersCoach(@PathVariable Long newCoachId, @PathVariable Long memberId){
        memberService.changeMembersCoach(newCoachId, memberId);
        return ResponseEntity.ok().build();
    }
}
