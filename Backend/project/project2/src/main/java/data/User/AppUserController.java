package data.User;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AppUserController {

    @Autowired
    AppUserRepository AppUserRepository;
    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @GetMapping(path = {"/AppUser"})
    List<AppUser> getAllAppUsers() {
        return this.AppUserRepository.findAll();
    }

    @GetMapping(path = {"/AppUser/{id}"})
    AppUser getAppUserById(@PathVariable long id) {
        return this.AppUserRepository.findById(id);
    }

    @PostMapping(path = {"/AppUser"})
    String createAppUser(@RequestBody AppUser AppUser) {
        if (AppUser == null) {
            return this.failure;
        } else {
            this.AppUserRepository.save(AppUser);
            return this.success;
        }
    }

    @PutMapping(path = {"/AppUser/{id}"})
    AppUser updateAppUser(@PathVariable long id, @RequestBody AppUser request) {
        AppUser AppUser = this.AppUserRepository.findById(id);
        if (AppUser == null) {
            return null;
        } else {
            this.AppUserRepository.save(request);
            return this.AppUserRepository.findById(id);
        }
    }

    @DeleteMapping(path = {"/AppUser/{id}"})
    String deleteAppUser(@PathVariable long id) {
        this.AppUserRepository.deleteById(id);
        return this.success;
    }
}
