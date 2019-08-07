package mekanism.common.config.options;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
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
public class IntSetOption extends Option<IntSetOption> {

    private final int[] defaultValue;
    private IntSet value;
    private boolean hasRange = false;
    private int min;
    private int max;

    public IntSetOption(BaseConfig owner, String category, String key, int[] defaultValue, @Nullable String comment) {
        super(owner, category, key, comment);
        this.defaultValue = defaultValue;
        this.value = new IntArraySet();
        for (int i : defaultValue) {
            this.value.add(i);
        }
    }

    public IntSetOption(BaseConfig owner, String category, String key, int[] defaultValue) {
        this(owner, category, key, defaultValue, null);
    }

    public IntSetOption(BaseConfig owner, String category, String key) {
        this(owner, category, key, new int[0], null);
    }

    public IntSetOption(BaseConfig owner, String category, String key, int[] defaultValue, @Nullable String comment, int min, int max) {
        this(owner, category, key, defaultValue, comment);
        this.hasRange = true;
        this.min = min;
        this.max = max;
    }

    public IntSet val() {
        return value;
    }

    public void set(IntSet value) {
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
        this.value.clear();
        for (int i : prop.getIntList()) {
            this.value.add(i);
        }
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeInt(this.value.size());
        for (int i : value) {
            buf.writeInt(i);
        }
    }

    @Override
    public void read(PacketBuffer buf) {
        int size = buf.readInt();
        this.value.clear();
        for (int i = 0; i < size; i++) {
            this.value.add(buf.readInt());
        }
    }
}