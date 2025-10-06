package data.User;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@RestController
public class AppUserController {

    @Autowired
    AppUserRepository AppUserRepository;
    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    /**
     * GET /AppUser
     * Retrieves a list of all AppUser records from the database.
     *
     * @return List of AppUser objects
     */
    @GetMapping(path = {"/AppUser"})
    List<AppUser> getAllAppUsers() {
        return this.AppUserRepository.findAll();
    }

    /**
     * GET /AppUser/{id}
     * Retrieves a single AppUser by their ID.
     *
     * @param id the ID of the user
     * @return the AppUser object, or null if not found
     */
    @GetMapping(path = {"/AppUser/{id}"})
    AppUser getAppUserById(@PathVariable long id) {
        return this.AppUserRepository.findById(id);
    }


    /**
     * Creates a new AppUser in the system.
     * @param appUser the AppUser object to create, provided in the request body
     * @return a ResponseEntity containing a message and HTTP status code:
     *         <ul>
     *             <li>200 OK: user was successfully created</li>
     *             <li>400 Bad Request: request body was null</li>
     *             <li>409 Conflict: username already exists (unique constraint violation)</li>
     *         </ul>
     *
     * @see AppUserRepository#save(Object)
     *
     * Example usage:
     * POST /AppUser
     * Request body (JSON):
     * {
     *   "username": "johndoe123",
     *   "password": "SecurePass123!",
     *   "email": "johndoe@example.com",
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "age": 30
     * }
     */
    @PostMapping(path = {"/AppUser"})
    public ResponseEntity<String> createAppUser(@RequestBody AppUser appUser) {
        if (appUser == null) {
            return ResponseEntity.badRequest().body(this.failure); // 400 Bad Request
        }
        try {
            this.AppUserRepository.save(appUser);
            return ResponseEntity.ok(this.success); // 200 OK
        } catch (DataIntegrityViolationException e) {
            // This usually happens if the username already exists (unique constraint)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Username already exists");
        }
    }

    /**
     * PUT /AppUser/{id}
     * Updates an existing AppUser's details works for multiple
     * fields or one at a time as long as id is found
     * If the user does not exist, returns null.
     *
     * @param id the ID of the user to update
     * @param request the updated user data from the request body
     * @return the updated AppUser object, or null if not found
     */
    @PutMapping(path = "/AppUser/{id}")
    AppUser updateAppUser(@PathVariable long id, @RequestBody AppUser request) {
        AppUser existingUser = this.AppUserRepository.findById(id);
        if (existingUser == null) {
            return null;
        } else {
            if (!Objects.equals(request.getFirstName(),"")) {
                existingUser.setFirstName(request.getFirstName());
            }
            if (!Objects.equals(request.getLastName(),"")) {
                existingUser.setLastName(request.getLastName());
            }
            if (!Objects.equals(request.getEmail(),"")) {
                existingUser.setEmail(request.getEmail());
            }
            if (!Objects.equals(request.getPassword(), "")) {
                existingUser.setPassword(request.getPassword());
            }
            if (!Objects.equals(request.getAge(), "")) { // careful: primitive int defaults to 0
                existingUser.setAge(request.getAge());
            }
            if (!Objects.equals(request.getUsername(), "")) {
                existingUser.setUsername(request.getUsername());
            }
            return AppUserRepository.save(existingUser);
        }
    }

    @PutMapping(path = "/AppUser/username/{username}")
    AppUser usernameUpdateAppUser(@PathVariable String username, @RequestBody AppUser request) {
        AppUser existingUser = this.AppUserRepository.findByUsername(username);
        if (existingUser == null) {
            return null;
        } else {
            if (!Objects.equals(request.getFirstName(), "")) {
                existingUser.setFirstName(request.getFirstName());
            }
            if (!Objects.equals(request.getLastName(), "")) {
                existingUser.setLastName(request.getLastName());
            }
            if (!Objects.equals(request.getEmail(), "")) {
                existingUser.setEmail(request.getEmail());
            }
            if (!Objects.equals(request.getPassword(), "")) {
                existingUser.setPassword(request.getPassword());
            }
            if (!Objects.equals(request.getAge(), "")) { // careful: primitive int defaults to 0
                existingUser.setAge(request.getAge());
            }
            if (!Objects.equals(request.getUsername(), "")) {
                existingUser.setUsername(request.getUsername());
            }
            return AppUserRepository.save(existingUser);
        }
    }

    /**
     * DELETE /AppUser/{id}
     * Deletes a user by their ID.
     *
     * @param id the ID of the user to delete
     * @return a JSON success message
     */
    @DeleteMapping(path = {"/AppUser/{id}"})
    String deleteAppUser(@PathVariable long id) {
        this.AppUserRepository.deleteById(id);
        return this.success;
    }

    /**
     * Retrieves an AppUser by their username.
     *
     * @param username the username of the user to retrieve
     * @return the AppUser object corresponding to the given username,
     *         or null if no user with that username exists
     *
     * @see AppUserRepository#findByUsername(String)
     *
     * Example usage:
     * GET /AppUser/username/johndoe
     */
    @GetMapping(path = "/AppUser/username/{username}")
    AppUser getUserByUsername(@PathVariable String username) {
        return this.AppUserRepository.findByUsername(username);
    }

    /**
     * Deletes an AppUser by their username.
     *
     * @param username the username of the user to delete
     * @return a string indicating the result:
     *         - returns {@link #failure} if no user with the username exists
     *         - returns {@link #success} if the user was found and deleted
     *
     * @see AppUserRepository#deleteByUsername(String)
     *
     * Example usage:
     * DELETE /AppUser/username/johndoe
     */
    @DeleteMapping(path = "/AppUser/username/{username}")
    String deleteUserByUsername(@PathVariable String username) {
        if(this.AppUserRepository.findByUsername(username) == null) {
            return this.failure;
        }
        else{
            this.AppUserRepository.deleteByUsername(username);
            return this.success;
        }
    }
}
