package cz.psgs.SpringRestDemo.payload.auth;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountViewDTO {
    
  
    private long id;

    private String email;

    private String authorities;
}
