package mekanism.common.registries;

import java.util.function.UnaryOperator;
import mekanism.common.ChemicalConstants;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemNutritionalPasteBucket;
import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.registration.impl.FluidDeferredRegister.MekanismFluidType;
import mekanism.common.registration.impl.FluidRegistryObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.LiquidBlock;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Flowing;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Source;

public class MekanismFluids {

    private MekanismFluids() {
    }

    public static final FluidDeferredRegister FLUIDS = new FluidDeferredRegister(Mekanism.MODID);

    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> HYDROGEN = FLUIDS.registerLiquidChemical(ChemicalConstants.HYDROGEN);
    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> OXYGEN = FLUIDS.registerLiquidChemical(ChemicalConstants.OXYGEN);
    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> CHLORINE = FLUIDS.registerLiquidChemical(ChemicalConstants.CHLORINE);
    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> SULFUR_DIOXIDE = FLUIDS.registerLiquidChemical(ChemicalConstants.SULFUR_DIOXIDE);
    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> SULFUR_TRIOXIDE = FLUIDS.registerLiquidChemical(ChemicalConstants.SULFUR_TRIOXIDE);
    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> SULFURIC_ACID = FLUIDS.registerLiquidChemical(ChemicalConstants.SULFURIC_ACID);
    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> HYDROGEN_CHLORIDE = FLUIDS.registerLiquidChemical(ChemicalConstants.HYDROGEN_CHLORIDE);
    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> HYDROFLUORIC_ACID = FLUIDS.registerLiquidChemical(ChemicalConstants.HYDROFLUORIC_ACID);
    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> URANIUM_OXIDE = FLUIDS.registerLiquidChemical(ChemicalConstants.URANIUM_OXIDE);
    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> URANIUM_HEXAFLUORIDE = FLUIDS.registerLiquidChemical(ChemicalConstants.URANIUM_HEXAFLUORIDE);
    //Internal gases
    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> ETHENE = FLUIDS.registerLiquidChemical(ChemicalConstants.ETHENE);
    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> SODIUM = FLUIDS.registerLiquidChemical(ChemicalConstants.SODIUM);
    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> SUPERHEATED_SODIUM = FLUIDS.registerLiquidChemical(ChemicalConstants.SUPERHEATED_SODIUM);
    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> BRINE = FLUIDS.register("brine", properties -> properties.tint(0xFFFEEF9C));
    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> LITHIUM = FLUIDS.registerLiquidChemical(ChemicalConstants.LITHIUM);

    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> STEAM = FLUIDS.register("steam", properties -> properties.temperature(373).density(0),
          renderProperties -> renderProperties.texture(Mekanism.rl("liquid/steam"), Mekanism.rl("liquid/steam_flow")));
    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> HEAVY_WATER = FLUIDS.register("heavy_water",
          renderProperties -> renderProperties.texture(ResourceLocation.withDefaultNamespace("block/water_still"),
                ResourceLocation.withDefaultNamespace("block/water_flow")).tint(0xFF0D1455));
    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, ItemNutritionalPasteBucket> NUTRITIONAL_PASTE = FLUIDS.register("nutritional_paste",
          ItemNutritionalPasteBucket::new, UnaryOperator.identity(), renderProperties -> renderProperties.tint(0xFFEB6CA3));
}