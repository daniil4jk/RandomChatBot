package ru.daniil4jk.randomChatBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.daniil4jk.randomChatBot.models.RandomChatBotUser;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<RandomChatBotUser, Long> {
    Optional<RandomChatBotUser> findByUserName(String userName);
}
