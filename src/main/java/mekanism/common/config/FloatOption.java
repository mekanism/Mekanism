package mekanism.common.config;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/**
 * Created by Thiakil on 15/03/2019.
 */
@ParametersAreNonnullByDefault
public class FloatOption extends Option<FloatOption> {

    private final float defaultValue;
    private float value;
    private boolean hasRange = false;
    private float min;
    private float max;

    FloatOption(BaseConfig owner, String category, String key, float defaultValue, @Nullable String comment) {
        super(owner, category, key, comment);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    FloatOption(BaseConfig owner, String category, String key, float defaultValue) {
        this(owner, category, key, defaultValue, null);
    }

    FloatOption(BaseConfig owner, String category, String key) {
        this(owner, category, key, 0, null);
    }

    FloatOption(BaseConfig owner, String category, String key, float defaultValue, @Nullable String comment, float min,
          float max) {
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
    protected void load(Configuration config) {
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
    protected void write(ByteBuf buf) {
        buf.writeFloat(this.value);
    }

    @Override
    protected void read(ByteBuf buf) {
        this.value = buf.readFloat();
    }
}
