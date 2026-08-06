package pl.omnisport.api.member;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public void addNewMember(@RequestBody Member member){
        memberService.saveNewMember(member);
    }

    @DeleteMapping("/{id}")
    public void deleteMember(@PathVariable Long id){
        memberService.removeMember(id);
    }

    @PatchMapping("/{id}/extend-pass")
    public void extendPassValidity(@PathVariable Long id){
        memberService.extendPassValidity(id);
    }
}
