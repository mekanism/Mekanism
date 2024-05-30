package mekanism.api.gear.config;

import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

/**
 * Immutable class representing a module config (name and value).
 *
 * @param <DATA> Type of the data stored by this config.
 *
 * @apiNote Currently Mekanism only has rendering/GUI support for handling {@link ModuleBooleanConfig}, {@link ModuleColorConfig}, and {@link ModuleEnumConfig}; if more
 * types are needed either open an issue or create a PR implementing support for them.
 * @since 10.6.0
 */
@NothingNullByDefault
public abstract class ModuleConfig<DATA> {

    /**
     * Constant representing the module config for enabled state.
     */
    public static final String ENABLED_KEY = "enabled";
    /**
     * Constant representing the module config for if a handling of mode change state.
     */
    public static final String HANDLES_MODE_CHANGE_KEY = "handle_mode_change";
    /**
     * Constant representing the module config for whether the module should render on the HUD.
     */
    public static final String RENDER_HUD_KEY = "render_hud";

    /**
     * Helper method to get the base part of a codec that contains all the parts necessary by this parent class.
     */
    protected static <DATA, CONFIG extends ModuleConfig<DATA>> P1<Mu<CONFIG>, String> baseCodec(Instance<CONFIG> instance) {
        return instance.group(ExtraCodecs.NON_EMPTY_STRING.fieldOf(SerializationConstants.NAME).forGetter(ModuleConfig::name));
    }

    //TODO: Do we want to make module configs be a registry or something rather than being named?
    // It probably won't make that much difference as it still would need to keep track of the
    // "config type" which would basically just be a named registry object
    private final String name;

    protected ModuleConfig(String name) {
        this.name = Objects.requireNonNull(name, "Name cannot be null.");
    }

    /**
     * {@return the name of this config option, should be unique}
     */
    public final String name() {
        return name;
    }

    /**
     * Gets a stream codec that is capable of sending this config over the network, without syncing the name, and instead assuming the name is the given one.
     *
     * @param name Name to use during config decoding.
     */
    public abstract StreamCodec<? super RegistryFriendlyByteBuf, ModuleConfig<DATA>> namedStreamCodec(String name);

    /**
     * {@return the value of this config option}
     */
    public abstract DATA get();

    /**
     * Creates a new immutable module config object that has the specified value.
     *
     * @param value Desired value.
     *
     * @return A new module config with the specified value.
     *
     * @throws NullPointerException     If value is null.
     * @throws IllegalArgumentException If the specified value is not valid for the current config (used for invalid packets)
     */
    public abstract ModuleConfig<DATA> with(DATA value);

    /**
     * Used to check if this config is currently disabled, and if so will be hidden from the module tweaker screen.
     *
     * @return {@code false}, unless overridden to conditionally disable.
     *
     * @apiNote When overriding, make sure to also override {@link #get()}, {@link #with(Object)}, {@link #namedStreamCodec(String)}, and use a custom codec and stream
     * codec when adding the config to the module data.
     */
    public boolean isConfigDisabled() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        return name.equals(((ModuleConfig<?>) obj).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}