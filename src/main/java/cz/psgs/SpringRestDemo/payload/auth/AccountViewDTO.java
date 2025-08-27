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

    private String name;
    private String job;
    private int age;
    private String personalInfo;
    private boolean male;
    private String avatar;

    public AccountViewDTO (long id, String email, String authorities){
        this.id = id;
        this.email = email;
        this.authorities = authorities;
        this.name = null;
        this.job = null;
        this.age = 0;
        this.personalInfo = null;
        this.male = true;
        this.avatar = null;
    }
}
