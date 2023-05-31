package hello.jdbc.domain;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Member {
    private String memberId;
    private int money;
}
