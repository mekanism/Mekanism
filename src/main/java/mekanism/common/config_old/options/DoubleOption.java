package mekanism.common.config_old.options;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.common.config_old.BaseConfig;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/**
 * Created by Thiakil on 15/03/2019.
 */
@ParametersAreNonnullByDefault
public class DoubleOption extends Option<DoubleOption> {

    private double value;
    private final double defaultValue;
    private boolean hasRange = false;
    private double min;
    private double max;

    public DoubleOption(BaseConfig owner, String category, String key, double defaultValue, @Nullable String comment) {
        super(owner, category, key, comment);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public DoubleOption(BaseConfig owner, String category, String key, double defaultValue) {
        this(owner, category, key, defaultValue, null);
    }

    public DoubleOption(BaseConfig owner, String category, String key) {
        this(owner, category, key, 0, null);
    }

    public DoubleOption(BaseConfig owner, String category, String key, double defaultValue, @Nullable String comment, double min, double max) {
        this(owner, category, key, defaultValue, comment);
        this.hasRange = true;
        this.min = min;
        this.max = max;
    }

    public double get() {
        return value;
    }

    public void set(double value) {
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
        this.value = prop.getDouble();

    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeDouble(this.value);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.value = buf.readDouble();
    }
}