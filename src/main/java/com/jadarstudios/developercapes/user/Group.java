/**
 * DeveloperCapes by Jadar
 * License: MIT License
 * (https://raw.github.com/jadar/DeveloperCapes/master/LICENSE)
 * version 4.0.0.x
 */
package com.jadarstudios.developercapes.user;

import com.jadarstudios.developercapes.cape.ICape;

import java.util.HashMap;
import java.util.Set;

/**
 * This represents a group of players that share a cape
 * 
 * @author jadar
 */
public class Group {

    protected HashMap<String, User> users;
    protected ICape cape;
    public final String name;

    public Group(String name) {
        this.users = new HashMap<String, User>();
        this.name = name;
    }

    public void addUser(User user) {
        if (!this.users.containsValue(user)) {
            user.capes.add(this.cape);
            this.users.put(user.userUUID, user);
        }
    }

    public void addUsers(Set<User> users) {
        for (User user : users) {
            this.addUser(user);
        }
    }

    public void removeUser(User user) {
        if (this.users.containsValue(user)) {
            this.users.remove(user);
            user.capes.remove(this.cape);
        }
    }

    public ICape getCape() {
        return this.cape;
    }

    public void setCape(ICape cape) {
        for (User user : this.users.values()) {
            user.capes.remove(this.cape);
        }

        this.cape = cape;
    }
}