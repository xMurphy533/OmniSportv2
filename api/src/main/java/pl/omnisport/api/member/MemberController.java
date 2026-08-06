package pl.omnisport.api.member;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping
    public List<Member> getAllMembers(){
        return memberService.getAllMembers();
    }

    @PostMapping
    public void addNewMember(@Valid @RequestBody Member member){
        memberService.saveNewMember(member);
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
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found");
        }
    }
}
