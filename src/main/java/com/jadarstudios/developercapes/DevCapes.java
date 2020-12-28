/**
 * DeveloperCapes by Jadar
 * License: MIT License
 * (https://raw.github.com/jadar/DeveloperCapes/master/LICENSE)
 * version 4.0.0.x
 */
package com.jadarstudios.developercapes;

import com.jadarstudios.developercapes.cape.CapeConfig;
import com.jadarstudios.developercapes.cape.CapeConfigManager;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * DeveloperCapes is a library for Minecraft. It allows developers to quickly add capes for players they specify. DevCapes uses Minecraft Forge.
 *
 * @author jadar
 */
public class DevCapes {
    private static DevCapes instance;

    public static final Logger logger = LogManager.getLogger("DevCapes");

    protected DevCapes() {
        MinecraftForge.EVENT_BUS.register(new RenderEventHandler());
    }

    public static DevCapes getInstance() {
        if (instance == null) {
            instance = new DevCapes();
        }
        return instance;
    }

    /**
     * InputStream.close() needs to be called on this after you're done!
     * 
     * @return {@link InputStream} for the {@link URL}
     */
    public InputStream getStreamForURL(URL url) {
        InputStream is = null;
        try {
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", System.getProperty("java.version"));
            connection.connect();

            is = connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
    }

    /**
     * InputStream.close() needs to be called on this after you're done!
     * 
     * @return {@link InputStream} for the {@link File}
     */
    public InputStream getStreamForFile(File file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return is;
    }

    @Deprecated
    /**
     * DEPRECATED: Please use {@link #registerConfig(String jsonUrl)} instead.<p>
     * Registers a config with DevCapes.
     *
     * @param jsonUrl
     *            The URL as a String that links to the Json file that you want
     *            to add
     * @param identifier
     *            A unique Identifier, normally your mod id
     * @return The id of the registered config
     */
    public int registerConfig(String jsonURL, String identifier) {
        return this.registerConfig(jsonURL);
    }

    /**
     * Registers a config with DevCapes.
     *
     * @param jsonUrl The URL as a String that links to the Json file that you want
     *                to add
     * @return The id of the registered config
     */
    public int registerConfig(String jsonUrl) {
        int id = -1;
        try {
            URL url = new URL(jsonUrl);
            id = this.registerConfig(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return id;
    }

    @Deprecated
    /**
     * DEPRECATED: Please use {@link #registerConfig(URL url)} instead.<p>
     * Registers a config with DevCapes.
     *
     * @param jsonUrl
     *            A {@link URL} that links to the Json file that you want to add
     * @param identifier
     *            A unique Identifier, normally your mod id
     * @return The id of the registered config
     */
    public int registerConfig(URL url, String identifier) {
        return this.registerConfig(url);
    }

    /**
     * Registers a config with DevCapes and returns the ID of the config.
     *
     * @param jsonUrl A {@link URL} that links to the Json file that you want to add
     * @return The id of the registered config
     */
    public int registerConfig(URL jsonUrl) {
        int id = -1;
        InputStream is = this.getStreamForURL(jsonUrl);

        if (is == null) {
            DevCapes.logger.error(String.format("Unable to establish a connection to the server, %s", jsonUrl.getHost()));
            return id;
        }

        CapeConfig config = CapeConfigManager.getInstance().parse(is);

        try {
            id = CapeConfigManager.getUniqueId();
            CapeConfigManager.getInstance().addConfig(id, config);
        } catch (CapeConfigManager.InvalidCapeConfigIdException e) {
            e.printStackTrace();
        }

        silentClose(is);

        return id;
    }

    private static void silentClose(InputStream is) {
        try {
            is.close();
        } catch (IOException ignored) {
        }
    }
}