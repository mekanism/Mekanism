package mekanism.common.config.options;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.common.config.BaseConfig;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraftforge.common.config.Configuration;

/**
 * Created by Thiakil on 15/03/2019.
 */
@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
public abstract class Option<THISTYPE extends Option> {

    protected final String key;

    @Nullable
    protected final String comment;

    protected final String category;

    protected boolean requiresGameRestart = false;

    protected boolean requiresWorldRestart = false;

    public Option(BaseConfig owner, String category, String key, @Nullable String comment) {
        this.category = category;
        this.key = key;
        this.comment = comment;
        owner.registerOption(this);
    }

    /**
     * Loads this option from the config file NB: saving back is handled by the config system / GUIs, load will be
     * re-called
     *
     * @param config where to load from
     */
    public abstract void load(Configuration config);

    /**
     * Serialise value to network buffer Must write the same way {@link #read(ByteBuf)} reads
     *
     * @param buf where to write to.
     */
    public abstract void write(ByteBuf buf);

    /**
     * Deserialise from network buffer Must read the same way {@link #write(ByteBuf)} writes
     *
     * @param buf where to read from
     */
    public abstract void read(ByteBuf buf);

    public THISTYPE setRequiresWorldRestart(boolean requiresWorldRestart) {
        this.requiresWorldRestart = requiresWorldRestart;
        //noinspection unchecked
        return (THISTYPE) this;
    }

    public THISTYPE setRequiresGameRestart(boolean requiresGameRestart) {
        this.requiresGameRestart = requiresGameRestart;
        //noinspection unchecked
        return (THISTYPE) this;
    }
}