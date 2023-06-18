package mekanism.common.registries;

import java.util.function.BooleanSupplier;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.Chemical;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.IItemProvider;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.registration.impl.CreativeTabRegistryObject;
import mekanism.common.registration.impl.FluidRegistryObject;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class MekanismCreativeTabs {

    public static final CreativeTabDeferredRegister CREATIVE_TABS = new CreativeTabDeferredRegister(Mekanism.MODID);

    //TODO - 1.20: Implement
    //TODO - 1.20: Can this be method reference or is it a circular dep
    public static final CreativeTabRegistryObject MEKANISM = CREATIVE_TABS.register("mekanism", () -> CreativeModeTab.builder()
          .title(MekanismLang.MEKANISM.translate()).icon(() -> MekanismItems.ATOMIC_ALLOY.getItemStack())
          .displayItems((displayParameters, output) -> {
              //TODO - 1.20: What are display items? And see if this even makes sense. We also need to add other module's items
              // unless we decide to split up into multiple tabs which might be time to do so
              //TODO - 1.20: Add filled variants??
              for (IItemProvider itemProvider : MekanismItems.ITEMS.getAllItems()) {
                  output.accept(itemProvider);
              }
              for (IBlockProvider blockProvider : MekanismBlocks.BLOCKS.getAllBlocks()) {
                  if (blockProvider.getBlock() instanceof BlockEnergyCube energyCube) {
                      //TODO - 1.20: Re-evaluate
                      EnergyCubeTier tier = Attribute.getTier(energyCube, EnergyCubeTier.class);
                      if (tier == EnergyCubeTier.CREATIVE) {
                          //Add the empty and charged variants
                          output.accept(applyEnergyCubeSideConfig(DataType.INPUT, blockProvider.getItemStack()));
                          output.accept(StorageUtils.getFilledEnergyVariant(applyEnergyCubeSideConfig(DataType.OUTPUT, blockProvider.getItemStack()), tier.getMaxEnergy()));
                      } else {
                          output.accept(blockProvider);
                          if (tier != null) {
                              //This should never be null, but validate it just in case, and then add the charged variant
                              output.accept(StorageUtils.getFilledEnergyVariant(blockProvider.getItemStack(), tier.getMaxEnergy()));
                          }
                      }
                  } else {
                      output.accept(blockProvider);
                  }
              }
              for (FluidRegistryObject<?, ?, ?, ?, ?> fluidRO : MekanismFluids.FLUIDS.getAllFluids()) {
                  output.accept(fluidRO.getBucket());
              }

              //TODO - 1.20: Test this
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
          //TODO - 1.20: do we care about what way to align it

          //TODO - 1.20: Figure out what tabs we need to declare as before it
          //.withTabsBefore()
    );

    private static ItemStack applyEnergyCubeSideConfig(DataType dataType, ItemStack stack) {
        CompoundTag sideConfig = new CompoundTag();
        for (RelativeSide side : EnumUtils.SIDES) {
            NBTUtils.writeEnum(sideConfig, NBTConstants.SIDE + side.ordinal(), dataType);
        }
        CompoundTag configNBT = new CompoundTag();
        configNBT.put(NBTConstants.CONFIG + TransmissionType.ENERGY.ordinal(), sideConfig);
        ItemDataUtils.setCompound(stack, NBTConstants.COMPONENT_CONFIG, configNBT);
        return stack;
    }

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

    //TODO - 1.20: Re-evaluate this and maybe inline the stuff somehow into
    public interface ICustomCreativeTabContents {

        default void addItems(CreativeModeTab.Output tabOutput, IItemProvider self) {
            tabOutput.accept(self);
        }
    }
}