package mekanism.api.chemical.slurry;

import com.mojang.serialization.Codec;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.providers.ISlurryProvider;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a slurry chemical subtype
 */
@NothingNullByDefault
public class Slurry extends Chemical<Slurry> implements ISlurryProvider {

    public static final Codec<Slurry> CODEC = MekanismAPI.SLURRY_REGISTRY.byNameCodec();

    @Nullable
    private final TagKey<Item> oreTag;

    public Slurry(SlurryBuilder builder) {
        super(builder);
        this.oreTag = builder.getOreTag();
    }

    @Override
    public String toString() {
        return "[Slurry: " + getRegistryName() + "]";
    }

    @Override
    public final boolean isEmptyType() {
        return this == MekanismAPI.EMPTY_SLURRY;
    }

    @Override
    protected final Registry<Slurry> getRegistry() {
        return MekanismAPI.SLURRY_REGISTRY;
    }

    @Override
    protected String getDefaultTranslationKey() {
        return Util.makeDescriptionId("slurry", getRegistryName());
    }

    /**
     * Gets the item tag representing the ore for this slurry.
     *
     * @return The tag for the item the slurry goes with. May be null.
     */
    @Nullable
    public TagKey<Item> getOreTag() {
        return oreTag;
    }
}