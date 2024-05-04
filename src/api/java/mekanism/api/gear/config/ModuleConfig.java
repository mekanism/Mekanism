package mekanism.api.gear.config;

import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.Objects;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

//TODO - 1.20.5: Docs
//TODO - 1.20.5: Re-evaluate the config name part and if we actually need this here or just in module data to allow lookup?
//TODO - 1.20.5: Do we want to make module configs be a registry or something rather than being named?
//TODO - 1.20.5: Sealed?
@NothingNullByDefault
public abstract class ModuleConfig<DATA> {

    public static final String ENABLED_KEY = "enabled";
    public static final String HANDLES_MODE_CHANGE_KEY = "handle_mode_change";
    public static final String RENDER_HUD_KEY = "render_hud";

    protected static <DATA, CONFIG extends ModuleConfig<DATA>> P1<Mu<CONFIG>, String> baseCodec(Instance<CONFIG> instance) {
        return instance.group(ExtraCodecs.NON_EMPTY_STRING.fieldOf(NBTConstants.NAME).forGetter(ModuleConfig::name));
    }

    //TODO - 1.20.5: Instance method for stream codec that takes the name and description as params?

    private final String name;

    protected ModuleConfig(String name) {
        this.name = Objects.requireNonNull(name, "Name cannot be null.");
    }

    public final String name() {
        return name;
    }

    public abstract StreamCodec<? super RegistryFriendlyByteBuf, ModuleConfig<DATA>> namedStreamCodec(String name);

    public abstract DATA get();

    public abstract ModuleConfig<DATA> with(DATA value);

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