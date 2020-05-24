package mekanism.api.chemical.gas;

import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

//TODO - V10: Transition this over to using the proper Slurry Chemical Type
@Deprecated
public class GasSlurry extends Gas {

    private static final ResourceLocation CLEAN_LOCATION = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "slurry/clean");
    private static final ResourceLocation DIRTY_LOCATION = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "slurry/dirty");

    private final Tag<Item> oreTag;

    public GasSlurry(boolean isClean, int tint, Tag<Item> oreTag) {
        super(GasBuilder.builder(isClean ? CLEAN_LOCATION : DIRTY_LOCATION).hidden().color(tint));
        this.oreTag = oreTag;
    }

    @Nonnull
    public Tag<Item> getOreTag() {
        return oreTag;
    }
}