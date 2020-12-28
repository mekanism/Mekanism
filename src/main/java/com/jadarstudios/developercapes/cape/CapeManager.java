/**
 * DeveloperCapes by Jadar
 * License: MIT License
 * (https://raw.github.com/jadar/DeveloperCapes/master/LICENSE)
 * version 4.0.0.x
 */
package com.jadarstudios.developercapes.cape;

import com.jadarstudios.developercapes.DevCapes;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

/**
 * This manages all of the capes, be nice to it or you won't get one!
 * 
 * @author jadar
 */
public class CapeManager {

    protected static CapeManager instance;

    private HashMap<String, ICape> capes;

    public CapeManager() {
        this.capes = new HashMap<String, ICape>();
    }

    public static CapeManager getInstance() {
        if (instance == null) {
            instance = new CapeManager();
        }
        return instance;
    }

    public void addCape(ICape cape) {
        if (!capes.containsValue(cape)) {
            capes.put(cape.getName(), cape);
        }
    }

    public void addCapes(Collection<ICape> capes) {
        for (ICape c : capes) {
            this.addCape(c);
        }
    }

    public ICape getCape(String capeName) {
        return capes.get(capeName);
    }

    public ICape newCape(String name) {
        StaticCape cape = new StaticCape(name);
        this.capes.put(name, cape);
        return cape;
    }
    
    public ICape parse(String name, Object object) {
        ICape cape = null;
        if(object instanceof String || object instanceof URL){
        	cape = parse(name, object.toString());
        }else{
        	DevCapes.logger.error(String.format("Cape, %s, could not be parsed because it is not in an accepted format!", object));
        }
        return cape;
    }

    protected ICape parse(String name, String url) {
        ICape cape = null;

        try {
            cape = new StaticCape(name, new URL(url));
        } catch (MalformedURLException e) {
            DevCapes.logger.error(String.format("Are you crazy?? \"%s\" is not a valid URL!", url));
            e.printStackTrace();
        }
        return cape;
    }
}