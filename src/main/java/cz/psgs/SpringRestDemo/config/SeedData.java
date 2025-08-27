package cz.psgs.SpringRestDemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import cz.psgs.SpringRestDemo.model.Account;
import cz.psgs.SpringRestDemo.service.AccountService;
import cz.psgs.SpringRestDemo.util.constants.Authority;

@Profile("dev")
@Component
public class SeedData implements CommandLineRunner{

    @Autowired
    private AccountService accountService;

    @Override
    public void run(String... args) throws Exception {
        Account account01 = new Account();
        Account account02 = new Account();
        Account account03 = new Account();
        Account account04 = new Account();

        account01.setEmail("tatka@smoula.cz");
        account01.setPassword("tatka1");
        account01.setAuthorities(Authority.ADMIN.toString() + " " + Authority.USER.toString());
        account01.setAge(89);
        account01.setJob("Village manager");
        account01.setName("Tatka Smoula");
        account01.setPersonalInfo("Smart Smurf with magical powers, protects the village and fights Gargamel when necessary...");
        account01.setMale(true);
        account01.setAvatar("avatar-male-3.png");
        accountService.save(account01);
        
        account02.setEmail("koumak@smoula.cz");
        account02.setPassword("koumak");
        account02.setAuthorities(Authority.USER.toString());
        account02.setAge(35);
        account02.setJob("Knowitall Smurf");
        account02.setName("Koumak Smoula");
        account02.setPersonalInfo("Smurf that believes he is the smartest of them all, but in the end he usually messes everything up...");
        account02.setMale(true);
        account02.setAvatar("avatar-male-2.png");
        accountService.save(account02);
        
        account03.setEmail("smoula@smoula.cz");
        account03.setPassword("password");
        account03.setAuthorities(Authority.USER.toString());
        account03.setAge(20);
        account03.setJob("Random Smurf");
        account03.setName("Radovy Smoula");
        account03.setPersonalInfo("Just a noname Smurf smurfing around the village...");
        account03.setMale(true);
        account03.setAvatar("avatar-male-2.png");
        accountService.save(account03);

        account04.setEmail("smoulinka@smoula.cz");
        account04.setPassword("blondynka");
        account04.setAuthorities(Authority.USER.toString());
        account04.setAge(20);
        account04.setJob("The only female Smurf");
        account04.setName("Smoulinka");
        account04.setPersonalInfo("As the only female Smurf in the village, she is very privilaged and special...");
        account04.setMale(false);
        account04.setAvatar("avatar-female-2.png");
        accountService.save(account04);



    }

}
