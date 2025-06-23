package cz.psgs.SpringRestDemo.payload.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDTO {
    private String email;
    private String password;
}
