package mekanism.common.config.options;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.common.config.BaseConfig;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/**
 * Created by Thiakil on 15/03/2019.
 */
@ParametersAreNonnullByDefault
public class IntOption extends Option<IntOption> {

    private int value;
    private final int defaultValue;
    private boolean hasRange = false;
    private int min;
    private int max;

    public IntOption(BaseConfig owner, String category, String key, int defaultValue, @Nullable String comment) {
        super(owner, category, key, comment);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public IntOption(BaseConfig owner, String category, String key, int defaultValue) {
        this(owner, category, key, defaultValue, null);
    }

    public IntOption(BaseConfig owner, String category, String key) {
        this(owner, category, key, 0, null);
    }

    public IntOption(BaseConfig owner, String category, String key, int defaultValue, @Nullable String comment, int min, int max) {
        this(owner, category, key, defaultValue, comment);
        this.hasRange = true;
        this.min = min;
        this.max = max;
    }

    public int val() {
        return value;
    }

    public void set(int value) {
        this.value = value;
    }

    @SuppressWarnings("Duplicates")//types are different
    @Override
    public void load(Configuration config) {
        Property prop;
        if (hasRange) {
            prop = config.get(this.category, this.key, this.defaultValue, this.comment, this.min, this.max);
        } else {
            prop = config.get(this.category, this.key, this.defaultValue, this.comment);
        }
        prop.setRequiresMcRestart(this.requiresGameRestart);
        prop.setRequiresWorldRestart(this.requiresWorldRestart);
        this.value = prop.getInt();
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(this.value);
    }

    @Override
    public void read(ByteBuf buf) {
        this.value = buf.readInt();
    }
}