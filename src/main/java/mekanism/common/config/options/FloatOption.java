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
public class FloatOption extends Option<FloatOption> {

    private float value;
    private final float defaultValue;
    private boolean hasRange = false;
    private float min;
    private float max;

    public FloatOption(BaseConfig owner, String category, String key, float defaultValue, @Nullable String comment) {
        super(owner, category, key, comment);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public FloatOption(BaseConfig owner, String category, String key, float defaultValue) {
        this(owner, category, key, defaultValue, null);
    }

    public FloatOption(BaseConfig owner, String category, String key) {
        this(owner, category, key, 0, null);
    }

    public FloatOption(BaseConfig owner, String category, String key, float defaultValue, @Nullable String comment,
          float min, float max) {
        this(owner, category, key, defaultValue, comment);
        this.hasRange = true;
        this.min = min;
        this.max = max;
    }

    public float val() {
        return value;
    }

    public void set(float value) {
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

        this.value = (float) prop.getDouble();
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeFloat(this.value);
    }

    @Override
    public void read(ByteBuf buf) {
        this.value = buf.readFloat();
    }
}