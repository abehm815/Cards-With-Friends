package data.User.Stats;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStatsRepository extends JpaRepository<UserStats, Long>{
    UserStats findByAppUserId(Long userId);
}
