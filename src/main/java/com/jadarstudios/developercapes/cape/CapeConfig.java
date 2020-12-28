/**
 * DeveloperCapes by Jadar
 * License: MIT License
 * (https://raw.github.com/jadar/DeveloperCapes/master/LICENSE)
 * version 4.0.0.x
 */
package com.jadarstudios.developercapes.cape;

import com.jadarstudios.developercapes.user.Group;
import com.jadarstudios.developercapes.user.User;

import java.util.HashMap;

/**
 * The players that need to be outfitted are stored here
 * 
 * @author jadar
 */
public class CapeConfig {
    public HashMap<String, Group> groups;
    public HashMap<String, User> users;

    public CapeConfig() {
        groups = new HashMap<String, Group>();
        users = new HashMap<String, User>();
    }
}