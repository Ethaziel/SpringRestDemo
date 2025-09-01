package cz.psgs.SpringRestDemo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cz.psgs.SpringRestDemo.model.Account;
import cz.psgs.SpringRestDemo.payload.auth.AccountDTO;
import cz.psgs.SpringRestDemo.payload.auth.AccountViewDTO;
import cz.psgs.SpringRestDemo.payload.auth.AuthoritiesDTO;
import cz.psgs.SpringRestDemo.payload.auth.PasswordDTO;
import cz.psgs.SpringRestDemo.payload.auth.ProfileDTO;
import cz.psgs.SpringRestDemo.payload.auth.TokenDTO;
import cz.psgs.SpringRestDemo.payload.auth.UserLoginDTO;
import cz.psgs.SpringRestDemo.service.AccountService;
import cz.psgs.SpringRestDemo.service.TokenService;
import cz.psgs.SpringRestDemo.util.constants.AccountError;
import cz.psgs.SpringRestDemo.util.constants.AccountSuccess;
import cz.psgs.SpringRestDemo.util.constants.Authority;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true", maxAge = 3600)
@Tag(name = "Auth Controller", description = "Controller for account management")
@Slf4j
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AccountService accountService;

    /* public AuthController(TokenService tokenService, AuthenticationManager authenticationManager){
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
    } */

    @PostMapping("/token")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TokenDTO> token(@Valid @RequestBody UserLoginDTO userLogin) throws AuthenticationException{
        try {
            Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(userLogin.getEmail(), userLogin.getPassword()));
            return ResponseEntity.ok(new TokenDTO(tokenService.generateToken(authentication)));
        } catch (Exception e) {
            log.debug(AccountError.TOKEN_GENERATION_ERROR.toString() + ": " + e.getMessage());
            return new ResponseEntity<>(new TokenDTO(null), HttpStatus.BAD_REQUEST);
        }
        
    }

    @PostMapping(value = "/users/add", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "Please enter a valid email and password 6-20 characters long")
    @ApiResponse(responseCode = "200", description = "Account added")
    @Operation(summary = "Add a new user")
    public ResponseEntity<String> addUser(@Valid @RequestBody AccountDTO accountDTO){
        try {
            Account account = new Account();
            account.setEmail(accountDTO.getEmail());
            account.setPassword(accountDTO.getPassword());
            account.setAge(accountDTO.getAge());
            account.setJob(accountDTO.getJob());
            account.setName(accountDTO.getName());
            account.setPersonalInfo(accountDTO.getPersonalInfo());
            account.setMale(accountDTO.isMale());

            if (accountDTO.getAvatar() != null && !accountDTO.getAvatar().isEmpty()){
                account.setAvatar(accountDTO.getAvatar());
            } else {
                account.setAvatar(getRandomAvatar(account.isMale()));
            }

            accountService.save(account);

            return ResponseEntity.ok(AccountSuccess.ACCOUNT_ADDED.toString());

        } catch (Exception e) {
            log.debug(AccountError.ADD_ACCOUNT_ERROR.toString() + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping(value = "/profile", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "View profile")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "403", description = "Token error")
    @Operation(summary = "View profile")
    @SecurityRequirement(name = "psgs-demo-api")
    public ProfileDTO profile(Authentication authentication){
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        
        Account account = optionalAccount.get();
        ProfileDTO profileDTO = new ProfileDTO(account.getId(), account.getEmail(), account.getAuthorities(), account.getName(), 
                                                account.getJob(), account.getAge(), account.getPersonalInfo(), account.isMale(), 
                                                account.getAvatar());
        return profileDTO;
        

    }
    @GetMapping(value = "/users", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "List of users")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "403", description = "Token error")
    @Operation(summary = "List user API")
    @SecurityRequirement(name = "psgs-demo-api")
    public List<AccountViewDTO> users(Authentication authentication){
        List<AccountViewDTO> accounts = new ArrayList<>();
        for (Account account : accountService.findAll()){
            accounts.add(new AccountViewDTO(account.getId(), account.getEmail(), account.getAuthorities(), account.getName(), 
                                            account.getJob(), account.getAge(), account.getPersonalInfo(), account.isMale(), 
                                            account.getAvatar()));
        }
        return accounts;

    }

    @PutMapping(value = "/users/{user_id}/update-authorities", produces = "application/json", consumes = "application/json")
    @ApiResponse(responseCode = "200", description = "Update authorities")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "400", description = "Invalid user")
    @ApiResponse(responseCode = "403", description = "Token error")
    @Operation(summary = "Update authorities")
    @SecurityRequirement(name = "psgs-demo-api")
    public ResponseEntity<AccountViewDTO> updateAuth(@Valid @RequestBody AuthoritiesDTO authoritiesDTO, @PathVariable long user_id){
        
        Optional<Account> optionalAccount = accountService.findById(user_id);
        if (optionalAccount.isPresent()){
            Account account = optionalAccount.get();
            account = accountService.updateAuthorities(account, authoritiesDTO.getAuthorities());
            
            return ResponseEntity.ok(new AccountViewDTO(account.getId(), account.getEmail(), account.getAuthorities()));
        }
        return new ResponseEntity<AccountViewDTO>(new AccountViewDTO(), HttpStatus.BAD_REQUEST);

    }

    @PutMapping(value = "/profile/update-password", produces = "application/json", consumes = "application/json")
    @ApiResponse(responseCode = "200", description = "Update password")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "403", description = "Token error")
    @Operation(summary = "Update password")
    @SecurityRequirement(name = "psgs-demo-api")
    public AccountViewDTO updatePassword(@Valid @RequestBody PasswordDTO passwordDTO, Authentication authentication){
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        
        Account account = optionalAccount.get();
        account.setPassword(passwordDTO.getPassword());
        accountService.save(account);
        return new AccountViewDTO(account.getId(), account.getEmail(), account.getAuthorities());
    }

    @PutMapping(value = "/profile/update-profile", produces = "application/json", consumes = "application/json")
    @ApiResponse(responseCode = "200", description = "Update profile")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "403", description = "Token error")
    @Operation(summary = "Update profile")
    @SecurityRequirement(name = "psgs-demo-api")
    public AccountViewDTO updateProfile(@Valid @RequestBody ProfileDTO profileDTO, Authentication authentication){
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        
        Account acc = optionalAccount.get();

        acc = accountService.updateProfile(acc, profileDTO);

        return new AccountViewDTO(acc.getId(), acc.getEmail(), acc.getAuthorities(), acc.getName(), 
                                    acc.getJob(), acc.getAge(), acc.getPersonalInfo(), acc.isMale(), 
                                    acc.getAvatar());
    }

    @GetMapping(value = "/profile/account", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "Get account")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "403", description = "Token error")
    @Operation(summary = "Get account info")
    @SecurityRequirement(name = "psgs-demo-api")
    public AccountViewDTO getAccount(Authentication authentication){
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        Account acc = optionalAccount.get();
        return new AccountViewDTO(acc.getId(), acc.getEmail(), acc.getAuthorities(), acc.getName(), acc.getJob(), acc.getAge(), acc.getPersonalInfo(), acc.isMale(), acc.getAvatar());
    }


    @DeleteMapping(value = "/profile/delete")
    @ApiResponse(responseCode = "200", description = "Delete profile")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "403", description = "Token error")
    @Operation(summary = "Delete profile")
    @SecurityRequirement(name = "psgs-demo-api")
    public ResponseEntity<String> deleteProfile(Authentication authentication){
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        
        return removeAccount(optionalAccount);

    }

    @DeleteMapping(value = "/users/{user_id}/delete")
    @ApiResponse(responseCode = "200", description = "Delete user")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "403", description = "Token error")
    @Operation(summary = "Delete user")
    @SecurityRequirement(name = "psgs-demo-api")
    public ResponseEntity<String> deleteUser (@PathVariable long user_id){
        Optional<Account> optionalAccount = accountService.findById(user_id);
        
        return removeAccount(optionalAccount);
    }

    private ResponseEntity<String> removeAccount(Optional<Account> optionalAccount){
        
        if (optionalAccount.isPresent()){
            accountService.deleteById(optionalAccount.get().getId());
            return ResponseEntity.ok("User deleted");
        }

        return new ResponseEntity<String>("Bad request", HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/roles")
    @SecurityRequirement(name = "psgs-demo-api")
    @Operation(summary = "Get user roles")
    public Authority[] getAuthorities(){
        return Authority.values();
    }

    private String getRandomAvatar (boolean isMale){
        String[] maleAvatars = { "avatar-male-1.png", "avatar-male-2.png", "avatar-male-3.png" };
        String[] femaleAvatars = { "avatar-female-1.png", "avatar-female-2.png", "avatar-female-3.png" };
        String [] avatars;
        if (isMale){
            avatars = maleAvatars;
        } else {
            avatars = femaleAvatars;
        }
        return avatars[new Random().nextInt(avatars.length)];
    
    }

    

}
