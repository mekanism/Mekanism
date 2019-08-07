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
public class IntListOption extends Option<IntListOption> {

    private int[] value;
    private final int[] defaultValue;
    private boolean hasRange = false;
    private int min;
    private int max;

    public IntListOption(BaseConfig owner, String category, String key, int[] defaultValue, @Nullable String comment) {
        super(owner, category, key, comment);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public IntListOption(BaseConfig owner, String category, String key, int[] defaultValue) {
        this(owner, category, key, defaultValue, null);
    }

    public IntListOption(BaseConfig owner, String category, String key) {
        this(owner, category, key, new int[0], null);
    }

    public IntListOption(BaseConfig owner, String category, String key, int[] defaultValue, @Nullable String comment, int min, int max) {
        this(owner, category, key, defaultValue, comment);
        this.hasRange = true;
        this.min = min;
        this.max = max;
    }

    public int[] val() {
        return value;
    }

    public void set(int[] value) {
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
        this.value = prop.getIntList();
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeInt(this.value.length);
        for (int i : value) {
            buf.writeInt(i);
        }
    }

    @Override
    public void read(PacketBuffer buf) {
        int size = buf.readInt();
        this.value = new int[size];
        for (int i = 0; i < size; i++) {
            this.value[i] = buf.readInt();
        }
    }
}