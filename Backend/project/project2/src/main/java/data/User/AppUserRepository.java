package data.User;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
        AppUser findById(long UserID);

        @Transactional
        void deleteById(long UserID);


        AppUser findByUsername(String username);

        @Transactional
        void deleteByUsername(String Username);
    }
