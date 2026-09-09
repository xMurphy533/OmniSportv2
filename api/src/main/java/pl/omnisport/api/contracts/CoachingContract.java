package pl.omnisport.api.contracts;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.omnisport.api.coach.Coach;
import pl.omnisport.api.member.Member;

import java.time.LocalDate;

@Entity
@Table(name = "coaching_contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoachingContract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @NotNull(message = "Member cannot be null")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    @NotNull(message = "Coach cannot be null")
    private Coach coach;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = true)
    private LocalDate endDate;

    @Column(name = "isActive")
    private boolean isActive;
}
