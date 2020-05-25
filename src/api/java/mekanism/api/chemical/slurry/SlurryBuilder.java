package mekanism.api.chemical.slurry;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalBuilder;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SlurryBuilder extends ChemicalBuilder<Slurry, SlurryBuilder> {

    @Nullable
    private Tag<Item> oreTag;

    protected SlurryBuilder(ResourceLocation texture) {
        super(texture);
    }

    public static SlurryBuilder clean() {
        return builder(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "slurry/clean"));
    }

    public static SlurryBuilder dirty() {
        return builder(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "slurry/dirty"));
    }

    public static SlurryBuilder builder(ResourceLocation texture) {
        return new SlurryBuilder(Objects.requireNonNull(texture));
    }

    public SlurryBuilder ore(ResourceLocation oreTagLocation) {
        return ore(new ItemTags.Wrapper(Objects.requireNonNull(oreTagLocation)));
    }

    public SlurryBuilder ore(Tag<Item> oreTag) {
        this.oreTag = Objects.requireNonNull(oreTag);
        return this;
    }

    @Nullable
    public Tag<Item> getOreTag() {
        return oreTag;
    }
}