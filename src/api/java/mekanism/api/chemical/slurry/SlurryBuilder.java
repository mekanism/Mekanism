package mekanism.api.chemical.slurry;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SlurryBuilder extends ChemicalBuilder<Slurry, SlurryBuilder> {

    @Nullable
    private TagKey<Item> oreTag;

    protected SlurryBuilder(ResourceLocation texture) {
        super(texture);
    }

    /**
     * Creates a builder for registering a {@link Slurry}, using our default clean {@link Slurry} texture.
     *
     * @return A builder for creating a {@link Slurry}.
     */
    public static SlurryBuilder clean() {
        return builder(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "slurry/clean"));
    }

    /**
     * Creates a builder for registering a {@link Slurry}, using our default dirty {@link Slurry} texture.
     *
     * @return A builder for creating a {@link Slurry}.
     */
    public static SlurryBuilder dirty() {
        return builder(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "slurry/dirty"));
    }

    /**
     * Creates a builder for registering a {@link Slurry}, with a given texture.
     *
     * @param texture A {@link ResourceLocation} representing the texture this {@link Slurry} will use.
     *
     * @return A builder for creating a {@link Slurry}.
     *
     * @apiNote The texture will be automatically stitched to the block texture atlas.
     * <br>
     * It is recommended to override {@link Slurry#getColorRepresentation()} if this builder method is not used in combination with {@link #color(int)} due to the texture
     * not needing tinting.
     */
    public static SlurryBuilder builder(ResourceLocation texture) {
        return new SlurryBuilder(Objects.requireNonNull(texture));
    }

    /**
     * Sets the tag that represents the ore that goes with this {@link Slurry}.
     *
     * @param oreTagLocation {@link ResourceLocation} of the item tag representing the ore.
     */
    public SlurryBuilder ore(ResourceLocation oreTagLocation) {
        return ore(ItemTags.create(Objects.requireNonNull(oreTagLocation)));
    }

    /**
     * Sets the tag that represents the ore that goes with this {@link Slurry}.
     *
     * @param oreTag Tag representing the ore.
     */
    public SlurryBuilder ore(TagKey<Item> oreTag) {
        this.oreTag = Objects.requireNonNull(oreTag);
        return this;
    }

    /**
     * Gets the item tag that represents the ore that goes with this {@link Slurry}.
     */
    @Nullable
    public TagKey<Item> getOreTag() {
        return oreTag;
    }
}