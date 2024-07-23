package ru.daniil4jk.randomChatBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.daniil4jk.randomChatBot.models.RandomChatBotUser;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<RandomChatBotUser, Long> {
    Optional<RandomChatBotUser> findByUserName(String userName);
}
