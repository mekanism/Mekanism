package mekanism.common.config;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.common.config.Configuration;

/**
 * Created by Thiakil on 15/03/2019.
 */
public abstract class BaseConfig {

    /**
     * No-op owner for making option fields not be null
     */
    protected static BaseConfig NULL_OWNER = new BaseConfig() {
        @Override
        protected void registerOption(Option option) {
        }
    };

    private List<Option> options = new ArrayList<>();

    /**
     * Registers an option to the save/load list. Should only be used by {@link Option}'s constructor
     *
     * @param option the option to add.
     */
    protected void registerOption(Option option) {
        options.add(option);
    }

    /**
     * Loads all options from the config file NB: saving back is done automatically by the config system / GUIs
     *
     * @param config Configuration to save to
     */
    public void load(Configuration config) {
        options.forEach(o -> o.load(config));
    }

    /**
     * Saves all options to the network buffer
     *
     * @param config buffer to save to
     */
    public void write(ByteBuf config) {
        options.forEach(o -> o.write(config));
    }

    /**
     * Reads all options from the network buffer
     *
     * @param config buffer to read from
     */
    public void read(ByteBuf config) {
        options.forEach(o -> o.read(config));
    }
}
