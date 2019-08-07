package mekanism.common.config.options;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.common.config.BaseConfig;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/**
 * Created by Thiakil on 15/03/2019.
 */
@ParametersAreNonnullByDefault
public class BooleanOption extends Option<BooleanOption> {

    private boolean value;
    private final boolean defaultValue;

    public BooleanOption(BaseConfig owner, String category, String key, boolean defaultValue, @Nullable String comment) {
        super(owner, category, key, comment);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public BooleanOption(BaseConfig owner, String category, String key, boolean defaultValue) {
        this(owner, category, key, defaultValue, null);
    }

    public BooleanOption(BaseConfig owner, String category, String key) {
        this(owner, category, key, false, null);
    }

    public boolean val() {
        return value;
    }

    public void set(boolean value) {
        this.value = value;
    }

    @Override
    public void load(Configuration config) {
        Property prop = config.get(this.category, this.key, this.defaultValue, this.comment);
        prop.setRequiresMcRestart(this.requiresGameRestart);
        prop.setRequiresWorldRestart(this.requiresWorldRestart);
        this.value = prop.getBoolean();
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeBoolean(this.value);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.value = buf.readBoolean();
    }
}