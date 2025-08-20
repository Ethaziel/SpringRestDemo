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

        account01.setEmail("tatka@smoula.cz");
        account01.setPassword("tatka1");
        account01.setAuthorities(Authority.ADMIN.toString() + " " + Authority.USER.toString());
        account01.setAge(89);
        account01.setJob("Village manager");
        account01.setName("Tatka Smoula");
        account01.setPersonalInfo("Smart Smurf with magical powers, protects the village and fights Gargamel when necessary...");
        account01.setMale(true);
        accountService.save(account01);
        
        account02.setEmail("koumak@smoula.cz");
        account02.setPassword("koumak");
        account02.setAuthorities(Authority.USER.toString());
        accountService.save(account02);
        
        account03.setEmail("smoula@smoula.cz");
        account03.setPassword("password");
        account03.setAuthorities(Authority.USER.toString());
        accountService.save(account03);



    }

}
