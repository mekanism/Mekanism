package mekanism.common.registries;

import mekanism.common.ChemicalConstants;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemNutritionalPasteBucket;
import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.registration.impl.FluidRegistryObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.fluids.ForgeFlowingFluid.Flowing;
import net.minecraftforge.fluids.ForgeFlowingFluid.Source;

public class MekanismFluids {

    private MekanismFluids() {
    }

    public static final FluidDeferredRegister FLUIDS = new FluidDeferredRegister(Mekanism.MODID);

    public static final FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> HYDROGEN = FLUIDS.registerLiquidChemical(ChemicalConstants.HYDROGEN);
    public static final FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> OXYGEN = FLUIDS.registerLiquidChemical(ChemicalConstants.OXYGEN);
    public static final FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> CHLORINE = FLUIDS.registerLiquidChemical(ChemicalConstants.CHLORINE);
    public static final FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> SULFUR_DIOXIDE = FLUIDS.registerLiquidChemical(ChemicalConstants.SULFUR_DIOXIDE);
    public static final FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> SULFUR_TRIOXIDE = FLUIDS.registerLiquidChemical(ChemicalConstants.SULFUR_TRIOXIDE);
    public static final FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> SULFURIC_ACID = FLUIDS.registerLiquidChemical(ChemicalConstants.SULFURIC_ACID);
    public static final FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> HYDROGEN_CHLORIDE = FLUIDS.registerLiquidChemical(ChemicalConstants.HYDROGEN_CHLORIDE);
    public static final FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> HYDROFLUORIC_ACID = FLUIDS.registerLiquidChemical(ChemicalConstants.HYDROFLUORIC_ACID);
    public static final FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> URANIUM_OXIDE = FLUIDS.registerLiquidChemical(ChemicalConstants.URANIUM_OXIDE);
    public static final FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> URANIUM_HEXAFLUORIDE = FLUIDS.registerLiquidChemical(ChemicalConstants.URANIUM_HEXAFLUORIDE);
    //Internal gases
    public static final FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> ETHENE = FLUIDS.registerLiquidChemical(ChemicalConstants.ETHENE);
    public static final FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> SODIUM = FLUIDS.registerLiquidChemical(ChemicalConstants.SODIUM);
    public static final FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> SUPERHEATED_SODIUM = FLUIDS.registerLiquidChemical(ChemicalConstants.SUPERHEATED_SODIUM);
    public static final FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> BRINE = FLUIDS.register("brine", fluidAttributes -> fluidAttributes.color(0xFFFEEF9C));
    public static final FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> LITHIUM = FLUIDS.registerLiquidChemical(ChemicalConstants.LITHIUM);

    public static final FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> STEAM = FLUIDS.register("steam",
          FluidDeferredRegister.getMekBaseBuilder(Mekanism.rl("liquid/steam"), Mekanism.rl("liquid/steam_flow")).gaseous().temperature(373));
    public static final FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> HEAVY_WATER = FLUIDS.register("heavy_water",
          FluidDeferredRegister.getMekBaseBuilder(new ResourceLocation("block/water_still"), new ResourceLocation("block/water_flow")).color(0xFF0D1455));
    public static final FluidRegistryObject<Source, Flowing, LiquidBlock, ItemNutritionalPasteBucket> NUTRITIONAL_PASTE = FLUIDS.register("nutritional_paste",
          ItemNutritionalPasteBucket::new, fluidAttributes -> fluidAttributes.color(0xFFEB6CA3));
}