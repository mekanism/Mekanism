package mekanism.common.tags;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasTags;
import mekanism.common.Mekanism;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

//TODO: Put Tag Wrappers used throughout in this class
public class MekanismTags {

    public static final Tag<Item> GOLD_DUST = new ItemTags.Wrapper(new ResourceLocation("forge", "dusts/gold"));

    //TODO: Decide if we should not actually have a tag for each gas type
    public static final Tag<Gas> BRINE = makeGasTag("brine");
    public static final Tag<Gas> CHLORINE = makeGasTag("chlorine");
    public static final Tag<Gas> DEUTERIUM = makeGasTag("deuterium");
    //TODO: Should this be called ethylene
    public static final Tag<Gas> ETHENE = makeGasTag("ethene");
    public static final Tag<Gas> FUSION_FUEL = makeGasTag("fusion_fuel");
    public static final Tag<Gas> HYDROGEN = makeGasTag("hydrogen");
    public static final Tag<Gas> HYDROGEN_CHLORIDE = makeGasTag("hydrogen_chloride");
    public static final Tag<Gas> LITHIUM = makeGasTag("lithium");
    public static final Tag<Gas> OXYGEN = makeGasTag("oxygen");
    public static final Tag<Gas> SODIUM = makeGasTag("sodium");
    public static final Tag<Gas> STEAM = makeGasTag("steam");
    public static final Tag<Gas> SULFUR_DIOXIDE = makeGasTag("sulfur_dioxide");
    public static final Tag<Gas> SULFUR_TRIOXIDE = makeGasTag("sulfur_trioxide");
    public static final Tag<Gas> SULFURIC_ACID = makeGasTag("sulfuric_acid");
    public static final Tag<Gas> TRITIUM = makeGasTag("tritium");

    public static final Tag<Gas> DIRTY_SLURRY = makeGasTag("dirty_slurry");
    public static final Tag<Gas> CLEAN_SLURRY = makeGasTag("clean_slurry");

    private static Tag<Gas> makeGasTag(String name) {
        return new GasTags.Wrapper(new ResourceLocation(Mekanism.MODID, name));
    }
}