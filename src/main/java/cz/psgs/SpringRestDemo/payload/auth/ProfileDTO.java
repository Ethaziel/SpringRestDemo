package cz.psgs.SpringRestDemo.payload.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ProfileDTO {

    private long id;
    private String email;
    private String authority;

    private String name;
    private String job;
    private int age;
    private String personalInfo;
    private boolean male;
    private String avatar;
}
