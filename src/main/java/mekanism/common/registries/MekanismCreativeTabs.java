package mekanism.common.registries;

import java.util.function.BooleanSupplier;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.registration.impl.CreativeTabRegistryObject;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.FluidUtils;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class MekanismCreativeTabs {

    public static final CreativeTabDeferredRegister CREATIVE_TABS = new CreativeTabDeferredRegister(Mekanism.MODID);

    public static final CreativeTabRegistryObject MEKANISM = CREATIVE_TABS.registerMain(MekanismLang.MEKANISM, MekanismItems.ATOMIC_ALLOY, builder ->
          builder.withSearchBar()//Allow our tabs to be searchable for convenience purposes
                .displayItems((displayParameters, output) -> {
                    CreativeTabDeferredRegister.addToDisplay(MekanismItems.ITEMS, output);
                    CreativeTabDeferredRegister.addToDisplay(MekanismBlocks.BLOCKS, output);
                    CreativeTabDeferredRegister.addToDisplay(MekanismFluids.FLUIDS, output);
                    //TODO - 1.20: Decide if we want to move these to using ICustomCreativeTabContents
                    if (MekanismConfig.general.isLoaded()) {
                        //Fluid Tanks
                        if (MekanismConfig.general.prefilledFluidTanks.get()) {
                            int capacity = FluidTankTier.CREATIVE.getStorage();
                            for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
                                if (fluid.isSource(fluid.defaultFluidState())) {//Only add sources
                                    output.accept(FluidUtils.getFilledVariant(MekanismBlocks.CREATIVE_FLUID_TANK.getItemStack(), capacity, () -> fluid));
                                }
                            }
                        }
                        //Chemical Tanks
                        addFilled(MekanismConfig.general.prefilledGasTanks, MekanismAPI.gasRegistry(), output);
                        addFilled(MekanismConfig.general.prefilledInfusionTanks, MekanismAPI.infuseTypeRegistry(), output);
                        addFilled(MekanismConfig.general.prefilledPigmentTanks, MekanismAPI.pigmentRegistry(), output);
                        addFilled(MekanismConfig.general.prefilledSlurryTanks, MekanismAPI.slurryRegistry(), output);
                    }
                })
    );

    private static <CHEMICAL extends Chemical<CHEMICAL>> void addFilled(BooleanSupplier shouldAdd, IForgeRegistry<CHEMICAL> registry, CreativeModeTab.Output tabOutput) {
        if (shouldAdd.getAsBoolean()) {
            long capacity = ChemicalTankTier.CREATIVE.getStorage();
            for (CHEMICAL type : registry.getValues()) {
                if (!type.isHidden()) {
                    tabOutput.accept(ChemicalUtil.getFilledVariant(MekanismBlocks.CREATIVE_CHEMICAL_TANK.getItemStack(), capacity, type));
                }
            }
        }
    }
}