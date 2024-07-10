package bot.RandomChatBot.repository;

import bot.RandomChatBot.models.UserProperties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserProperties, Long> {
    Optional<UserProperties> findByUserName(String userName);
}
