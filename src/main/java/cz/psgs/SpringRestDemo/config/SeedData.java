package cz.psgs.SpringRestDemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import cz.psgs.SpringRestDemo.model.Account;
import cz.psgs.SpringRestDemo.service.AccountService;

@Component
public class SeedData implements CommandLineRunner{

    @Autowired
    private AccountService accountService;

    @Override
    public void run(String... args) throws Exception {
        Account account01 = new Account();
        Account account02 = new Account();

        account01.setEmail("tatka@smoula.cz");
        account01.setPassword("tatka");
        account01.setRole("ROLE_ADMIN");
        accountService.save(account01);
        
        account02.setEmail("koumak@smoula.cz");
        account02.setPassword("koumak");
        account02.setRole("ROLE_USER");
        accountService.save(account02);



    }

}
