package data.User;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
        AppUser findById(long UserID);

        @Transactional
        void deleteById(long UserID);


        AppUser findByUsername(String username);

        @Transactional
        void deleteByUsername(String Username);

        @Query("SELECT u FROM AppUser u " +
                "LEFT JOIN FETCH u.userStats us " +
                "LEFT JOIN FETCH us.gameStats " +
                "WHERE u.username = :username")
        AppUser findByUsernameWithStats(@Param("username") String username);
    }
