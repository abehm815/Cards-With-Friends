package data.User;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
        AppUser findById(int UserID);

        @Transactional
        void deleteById(int UserID);
    }
