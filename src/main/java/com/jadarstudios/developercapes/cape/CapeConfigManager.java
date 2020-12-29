/**
 * DeveloperCapes by Jadar
 * License: MIT License
 * (https://raw.github.com/jadar/DeveloperCapes/master/LICENSE)
 * version 4.0.0.x
 */
package com.jadarstudios.developercapes.cape;

import com.google.common.collect.HashBiMap;
import com.google.common.primitives.UnsignedBytes;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.jadarstudios.developercapes.DevCapes;
import com.jadarstudios.developercapes.user.Group;
import com.jadarstudios.developercapes.user.GroupManager;
import com.jadarstudios.developercapes.user.User;
import com.jadarstudios.developercapes.user.UserManager;
import org.apache.commons.lang3.ObjectUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.BitSet;
import java.util.Map;

/**
 * All configs need a manager, this is it.
 * 
 * @author jadar
 */
public class CapeConfigManager {

    protected static CapeConfigManager instance;
    
    protected static BitSet availableIds = new BitSet(256);
    protected HashBiMap<Integer, CapeConfig> configs;

    static {
        availableIds.clear(availableIds.size());
    }

    public CapeConfigManager() {
        configs = HashBiMap.create();
    }

    public static CapeConfigManager getInstance() {
        if (instance == null) {
            instance = new CapeConfigManager();
        }
        return instance;
    }

    public void addConfig(int id, CapeConfig config) throws InvalidCapeConfigIdException {
        int realId = claimId(id);
        this.configs.put(realId, config);
        addUsers(config.users);
        addGroups(config.groups);
    }
    
    protected void addUsers(Map<String, User> users){
    	try {
    		UserManager.getInstance().addUsers(users.values());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected void addGroups(Map<String, Group> groups){
    	try {
    		GroupManager.getInstance().addGroups(groups.values());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CapeConfig getConfig(int id) {
        return this.configs.get(id);
    }

    public int getIdForConfig(CapeConfig config) {
        return this.configs.inverse().get(config);
    }

    public static int getUniqueId() {
        return availableIds.nextClearBit(1);
    }

    public static int claimId(int id) throws InvalidCapeConfigIdException {
        if(id <= 0){
            throw new InvalidCapeConfigIdException("The config ID must be a positive non-zero integer");
        }
        try {
            UnsignedBytes.checkedCast(id);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        boolean isRegistered = availableIds.get(id);
        if (isRegistered) {
            throw new InvalidCapeConfigIdException(String.format("The config ID %d is already claimed.", id));
        }

        availableIds.set(id);
        return id;
    }

    public CapeConfig parse(InputStream is) {
        if (is == null) {
            throw new NullPointerException("Can not parse a null input stream!");
        }

        CapeConfig instance = new CapeConfig();
        InputStreamReader isr = new InputStreamReader(is);

        try {
            Map<String, Object> entries = new Gson().fromJson(isr, Map.class);

            for (Map.Entry<String, Object> entry : entries.entrySet()) {
                final String nodeName = entry.getKey();
                final Object obj = entry.getValue();
                if (obj instanceof Map) {
                    parseGroup(instance, nodeName, (Map) obj);
                } else if (obj instanceof String) {
                	parseUser(instance, nodeName, (String) obj);
                }
            }
        } catch (JsonSyntaxException e) {
        	DevCapes.logger.error("CapeConfig could not be parsed because:");
            e.printStackTrace();
        }

        return instance;
    }
    
    protected void parseGroup(CapeConfig config, String node, Map group) {
        Group g = GroupManager.getInstance().parse(node, group);
        if (g != null) {
        	config.groups.put(g.name, g);
        }
    }
    
    protected void parseUser(CapeConfig config, String node, String user) {
    	User u = UserManager.getInstance().parse(node, user);
        if (u != null) {
        	config.users.put(node, u);
        }
    }

    /**
     * DEPRECATED! Please use {@link com.jadarstudios.developercapes.cape.CapeConfigManager#parse(java.io.InputStream is)}
     * This will be removed in the next major release.
     */
    @Deprecated
    public CapeConfig parseFromStream(InputStream is) {
        return this.parse(is);
    }

    public static class InvalidCapeConfigIdException extends Exception {
        public InvalidCapeConfigIdException() {
            super();
        }

        public InvalidCapeConfigIdException(String s) {
            super(s);
        }

        public InvalidCapeConfigIdException(Throwable cause) {
            super(cause);
        }

        public InvalidCapeConfigIdException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}