package pl.omnisport.api.member;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.omnisport.api.admin.MemberMapper;
import pl.omnisport.api.coach.MemberRequest;
import pl.omnisport.api.coach.MemberResponse;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MemberMapper memberMapper;

    @GetMapping
    public List<MemberResponse> getAllMembers(){
        List<Member> members = memberService.getAllMembers();
        return memberMapper.toResponseList(members);
    }

    @GetMapping("/{id}")
    public Optional<MemberResponse> getMemberById(@PathVariable Long id) throws EntityNotFoundException{
        return memberService.getMemberById(id).map(memberMapper::toResponse);
    }

    @PostMapping
    public void addNewMember(@Valid @RequestBody MemberRequest request){
        memberService.saveNewMember(request);
    }

    @PutMapping("/{id}")
    public Member updateMember(@PathVariable Long id, @Valid @RequestBody MemberRequest request){
        Member member = memberMapper.toEntity(request);
        try{
            return memberService.updateMember(id, member);
        } catch (EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found");
        }
    }

    @DeleteMapping("/{id}")
    public void deleteMember(@PathVariable Long id){
        memberService.removeMember(id);
    }

    @PatchMapping("/{id}/extend-pass")
    public ResponseEntity<Void> extendPassValidity(@PathVariable Long id){
        try{
            memberService.extendPassValidity(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found");
        }
    }
}
