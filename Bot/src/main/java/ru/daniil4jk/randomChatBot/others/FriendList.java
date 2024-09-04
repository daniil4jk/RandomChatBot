package ru.daniil4jk.randomChatBot.others;

import ru.daniil4jk.randomChatBot.models.Friend;

import java.util.ArrayList;

public class FriendList extends ArrayList<Friend> {
    public Friend getById(long id) {
        for (Friend f : this) {
            if (f.getId() == id) {
                return f;
            }
        }
        return null;
    }

    public boolean containsFriendWithId(long id) {
        return getById(id) != null;
    }
}
