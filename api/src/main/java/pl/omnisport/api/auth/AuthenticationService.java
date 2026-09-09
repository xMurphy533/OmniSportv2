package pl.omnisport.api.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.omnisport.api.admin.AdminRepository;
import pl.omnisport.api.contracts.CoachingContract;
import pl.omnisport.api.member.Member;
import pl.omnisport.api.member.MemberRepository;
import pl.omnisport.api.security.JwtService;
import pl.omnisport.api.user.AppUser;
import pl.omnisport.api.user.AppUserRepository;
import pl.omnisport.api.user.Role;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AdminRepository adminRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MemberRepository memberRepository;

    public AuthenticationResponse register(RegisterRequest request){
        AppUser appUser = new AppUser();
        appUser.setEmail(request.getEmail());
        appUser.setPassword(passwordEncoder.encode(request.getPassword()));
        appUser.setRole(Role.MEMBER);
        appUser.setActive(true);

        appUserRepository.save(appUser);

        Member member = new Member();
        member.setName(request.getName());
        member.setSurname(request.getSurname());
        member.setAge(request.getAge());
        member.setSection(request.getSection());
        List<CoachingContract> coachingContracts = new ArrayList<>();
        member.setContracts(coachingContracts);
        member.setExpiryDate(null);
        member.setAppUser(appUser);

        memberRepository.save(member);

        var jwtToken = jwtService.generateToken(appUser);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = appUserRepository.findByEmail(request.getEmail()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder().token(jwtToken).build();
    }
}
