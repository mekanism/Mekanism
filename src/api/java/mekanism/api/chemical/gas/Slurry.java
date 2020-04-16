package mekanism.api.chemical.gas;

import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

//TODO - V10: Make this directly be a "chemical" instead of a "gas"
public class Slurry extends Gas {

    //TODO: Rename texture paths
    private static final ResourceLocation CLEAN_LOCATION = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "block/liquid/liquid_clean_ore");
    private static final ResourceLocation DIRTY_LOCATION = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "block/liquid/liquid_ore");

    private final Tag<Item> oreTag;

    public Slurry(boolean isClean, int tint, Tag<Item> oreTag) {
        super(GasBuilder.builder(isClean ? CLEAN_LOCATION : DIRTY_LOCATION).hidden().color(tint));
        this.oreTag = oreTag;
    }

    @Nonnull
    public Tag<Item> getOreTag() {
        return oreTag;
    }
}