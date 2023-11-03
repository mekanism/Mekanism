package mekanism.common.config.value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import mekanism.common.config.IMekanismConfig;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public abstract class CachedMapConfigValue<KEY, VALUE> extends CachedResolvableConfigValue<Map<KEY, VALUE>, List<? extends String>> {

    protected CachedMapConfigValue(IMekanismConfig config, ConfigValue<List<? extends String>> internal) {
        super(config, internal);
    }

    protected abstract void resolve(String encoded, Map<KEY, VALUE> resolved);

    protected abstract void encode(KEY key, VALUE value, Consumer<String> adder);

    @Override
    protected final Map<KEY, VALUE> resolve(List<? extends String> encoded) {
        Map<KEY, VALUE> resolved = new HashMap<>(encoded.size());
        for (String s : encoded) {
            resolve(s, resolved);
        }
        return resolved;
    }

    @Override
    protected final List<? extends String> encode(Map<KEY, VALUE> values) {
        return encodeStatic(values, this::encode);
    }

    /**
     * Helper method for use in encoding defaults.
     */
    protected static <KEY, VALUE> List<? extends String> encodeStatic(Map<KEY, VALUE> values, ValueEncoder<KEY, VALUE> encoder) {
        List<String> encoded = new ArrayList<>(values.size());
        for (Map.Entry<KEY, VALUE> entry : values.entrySet()) {
            encoder.encode(entry.getKey(), entry.getValue(), encoded::add);
        }
        //Sort it so that it is deterministic
        Collections.sort(encoded);
        return encoded;
    }

    protected interface ValueEncoder<KEY, VALUE> {

        void encode(KEY key, VALUE value, Consumer<String> adder);
    }
}