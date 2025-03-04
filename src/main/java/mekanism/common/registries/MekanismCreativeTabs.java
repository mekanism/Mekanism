package mekanism.common.registries;

import mekanism.api.MekanismAPI;
import mekanism.api.MekanismAPITags;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.attribute.Attributes.AttributeComparator;
import mekanism.common.block.prefab.BlockBase;
import mekanism.common.block.transmitter.BlockTransmitter;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ore.OreBlockType;
import mekanism.common.tier.TransporterTier;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.FluidUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public class MekanismCreativeTabs {

    public static final CreativeTabDeferredRegister CREATIVE_TABS = new CreativeTabDeferredRegister(Mekanism.MODID, MekanismCreativeTabs::addToExistingTabs);

    public static final MekanismDeferredHolder<CreativeModeTab, CreativeModeTab> MEKANISM = CREATIVE_TABS.registerMain(MekanismLang.MEKANISM,
          MekanismBlocks.METALLURGIC_INFUSER.getItemHolder(), builder ->
                builder.withSearchBar()//Allow our tabs to be searchable for convenience purposes
                      .displayItems((displayParameters, output) -> {
                          CreativeTabDeferredRegister.addToDisplay(MekanismItems.ITEMS, output);
                          CreativeTabDeferredRegister.addToDisplay(MekanismBlocks.BLOCKS, output);
                          CreativeTabDeferredRegister.addToDisplay(MekanismFluids.FLUIDS, output);
                          addFilledTanks(displayParameters, output, true);
                      })
    );

    private static void addFilledTanks(ItemDisplayParameters parameters, CreativeModeTab.Output output, boolean chemical) {
        if (MekanismConfig.general.isLoaded()) {
            //Fluid Tanks
            if (MekanismConfig.general.prefilledFluidTanks.get()) {
                parameters.holders().lookupOrThrow(Registries.FLUID)
                      //Only add sources and fluids that aren't hidden
                      .filterElements(fluid -> fluid != Fluids.EMPTY && fluid.isSource(fluid.defaultFluidState()))
                      .listElements()
                      .filter(holder -> !holder.is(Tags.Fluids.HIDDEN_FROM_RECIPE_VIEWERS))
                      .forEach(holder -> output.accept(FluidUtils.getFilledVariant(MekanismBlocks.CREATIVE_FLUID_TANK.getItemHolder(), holder)));
            }
            if (chemical) {
                //Chemical Tanks
                if (MekanismConfig.general.prefilledChemicalTanks.get()) {
                    parameters.holders().lookupOrThrow(MekanismAPI.CHEMICAL_REGISTRY_NAME)
                          .listElements()
                          .filter(holder -> !holder.is(MekanismAPITags.Chemicals.HIDDEN_FROM_RECIPE_VIEWERS) && !holder.is(MekanismAPI.EMPTY_CHEMICAL_KEY))
                          .forEach(holder -> output.accept(ChemicalUtil.getFilledVariant(MekanismBlocks.CREATIVE_CHEMICAL_TANK.getItemHolder(), holder)));
                }
            }
        }
    }

    private static void addToExistingTabs(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> tabKey = event.getTabKey();
        if (tabKey == CreativeModeTabs.BUILDING_BLOCKS) {
            CreativeTabDeferredRegister.addToDisplay(event, MekanismBlocks.SALT_BLOCK, MekanismBlocks.BRONZE_BLOCK, MekanismBlocks.STEEL_BLOCK,
                  MekanismBlocks.CHARCOAL_BLOCK, MekanismBlocks.REFINED_OBSIDIAN_BLOCK, MekanismBlocks.REFINED_GLOWSTONE_BLOCK);
            for (PrimaryResource resource : EnumUtils.PRIMARY_RESOURCES) {
                if (resource.getResourceBlockInfo() != null) {
                    CreativeTabDeferredRegister.addToDisplay(event, MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(resource));
                }
            }
        } else if (tabKey == CreativeModeTabs.NATURAL_BLOCKS) {
            CreativeTabDeferredRegister.addToDisplay(event, MekanismBlocks.SALT_BLOCK);
            for (OreBlockType oreType : MekanismBlocks.ORES.values()) {
                CreativeTabDeferredRegister.addToDisplay(event, oreType.stone(), oreType.deepslate());
            }
        } else if (tabKey == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            for (Holder<Item> item : MekanismBlocks.BLOCKS.getSecondaryEntries()) {
                //Note: This should always be a block item
                if (item.value() instanceof BlockItem blockItem) {
                    Block block = blockItem.getBlock();
                    if (block instanceof BlockTransmitter || block instanceof BlockBase<?> base && base.getType() instanceof Machine) {
                        CreativeTabDeferredRegister.addToDisplay(event, item);
                    }
                }
            }
            CreativeTabDeferredRegister.addToDisplay(event, MekanismBlocks.SECURITY_DESK, MekanismBlocks.RADIOACTIVE_WASTE_BARREL, MekanismBlocks.PERSONAL_CHEST,
                  MekanismBlocks.PERSONAL_BARREL, MekanismBlocks.CHARGEPAD, MekanismBlocks.LASER, MekanismBlocks.LASER_AMPLIFIER, MekanismBlocks.LASER_TRACTOR_BEAM,
                  MekanismBlocks.QUANTUM_ENTANGLOPORTER, MekanismBlocks.OREDICTIONIFICATOR, MekanismBlocks.FUELWOOD_HEATER, MekanismBlocks.MODIFICATION_STATION,
                  MekanismBlocks.QIO_DRIVE_ARRAY, MekanismBlocks.QIO_DASHBOARD, MekanismBlocks.QIO_EXPORTER, MekanismBlocks.QIO_IMPORTER, MekanismBlocks.QIO_REDSTONE_ADAPTER);
        } else if (tabKey == CreativeModeTabs.REDSTONE_BLOCKS) {
            CreativeTabDeferredRegister.addToDisplay(event, MekanismBlocks.INDUSTRIAL_ALARM);
            for (Holder<Item> item : MekanismBlocks.BLOCKS.getSecondaryEntries()) {
                //Note: This should always be a block item
                if (item.value() instanceof BlockItem blockItem) {
                    Block block = blockItem.getBlock();
                    if (Attribute.has(block, AttributeComparator.class)) {
                        CreativeTabDeferredRegister.addToDisplay(event, item);
                    } else if (block instanceof BlockTransmitter) {
                        AttributeTier<?> attribute = Attribute.get(block, AttributeTier.class);
                        if (attribute != null && !(attribute.tier() instanceof TransporterTier)) {
                            CreativeTabDeferredRegister.addToDisplay(event, item);
                        }
                    }
                }
            }
            CreativeTabDeferredRegister.addToDisplay(event, MekanismBlocks.DIVERSION_TRANSPORTER);
        } else if (tabKey == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            CreativeTabDeferredRegister.addToDisplay(event,
                  MekanismItems.CONFIGURATOR, MekanismItems.NETWORK_READER, MekanismItems.DOSIMETER, MekanismItems.GEIGER_COUNTER, MekanismItems.DICTIONARY,
                  MekanismItems.CONFIGURATION_CARD, MekanismItems.GAUGE_DROPPER, MekanismItems.CRAFTING_FORMULA, MekanismItems.PORTABLE_QIO_DASHBOARD,
                  MekanismItems.ATOMIC_DISASSEMBLER, MekanismItems.MEKA_TOOL, MekanismItems.SCUBA_MASK, MekanismItems.SCUBA_TANK, MekanismItems.FREE_RUNNERS,
                  MekanismItems.ARMORED_FREE_RUNNERS, MekanismItems.JETPACK, MekanismItems.ARMORED_JETPACK, MekanismItems.HDPE_REINFORCED_ELYTRA,
                  MekanismItems.HAZMAT_MASK, MekanismItems.HAZMAT_GOWN, MekanismItems.HAZMAT_PANTS, MekanismItems.HAZMAT_BOOTS, MekanismBlocks.CARDBOARD_BOX.getItemHolder(),
                  //Installers
                  MekanismItems.BASIC_TIER_INSTALLER, MekanismItems.ADVANCED_TIER_INSTALLER, MekanismItems.ELITE_TIER_INSTALLER, MekanismItems.ULTIMATE_TIER_INSTALLER,
                  //Upgrades
                  MekanismItems.SPEED_UPGRADE, MekanismItems.ENERGY_UPGRADE, MekanismItems.FILTER_UPGRADE, MekanismItems.MUFFLING_UPGRADE, MekanismItems.CHEMICAL_UPGRADE,
                  MekanismItems.ANCHOR_UPGRADE, MekanismItems.STONE_GENERATOR_UPGRADE
            );
            //Tanks
            CreativeTabDeferredRegister.addToDisplay(event,
                  MekanismBlocks.BASIC_FLUID_TANK, MekanismBlocks.ADVANCED_FLUID_TANK, MekanismBlocks.ELITE_FLUID_TANK, MekanismBlocks.ULTIMATE_FLUID_TANK,
                  MekanismBlocks.CREATIVE_FLUID_TANK
            );
            CreativeTabDeferredRegister.addToDisplay(MekanismFluids.FLUIDS, event);
            addFilledTanks(event.getParameters(), event, false);
        } else if (tabKey == CreativeModeTabs.COMBAT) {
            CreativeTabDeferredRegister.addToDisplay(event, MekanismItems.ATOMIC_DISASSEMBLER, MekanismItems.FLAMETHROWER, MekanismItems.ELECTRIC_BOW,
                  MekanismItems.MEKA_TOOL, MekanismItems.MEKASUIT_HELMET, MekanismItems.MEKASUIT_BODYARMOR, MekanismItems.MEKASUIT_PANTS, MekanismItems.MEKASUIT_BOOTS,
                  MekanismItems.ARMORED_FREE_RUNNERS, MekanismItems.ARMORED_JETPACK);
        } else if (tabKey == CreativeModeTabs.FOOD_AND_DRINKS) {
            //Only add the filled canteen
            event.accept(FluidUtils.getFilledVariant(MekanismItems.CANTEEN, MekanismFluids.NUTRITIONAL_PASTE), CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
        } else if (tabKey == CreativeModeTabs.INGREDIENTS) {
            CreativeTabDeferredRegister.addToDisplay(event,
                  MekanismItems.MODULE_BASE, MekanismItems.INFUSED_ALLOY, MekanismItems.REINFORCED_ALLOY, MekanismItems.ATOMIC_ALLOY,
                  MekanismItems.BASIC_CONTROL_CIRCUIT, MekanismItems.ADVANCED_CONTROL_CIRCUIT, MekanismItems.ELITE_CONTROL_CIRCUIT, MekanismItems.ULTIMATE_CONTROL_CIRCUIT,
                  MekanismItems.ENRICHED_CARBON, MekanismItems.ENRICHED_REDSTONE, MekanismItems.ENRICHED_DIAMOND, MekanismItems.ENRICHED_OBSIDIAN,
                  MekanismItems.ENRICHED_GOLD, MekanismItems.ENRICHED_TIN,
                  MekanismItems.BIO_FUEL, MekanismBlocks.BIO_FUEL_BLOCK.getItemHolder(), MekanismItems.SUBSTRATE, MekanismItems.HDPE_PELLET, MekanismItems.HDPE_ROD,
                  MekanismItems.HDPE_SHEET, MekanismItems.ANTIMATTER_PELLET, MekanismItems.PLUTONIUM_PELLET, MekanismItems.POLONIUM_PELLET,
                  MekanismItems.REPROCESSED_FISSILE_FRAGMENT, MekanismItems.ELECTROLYTIC_CORE, MekanismItems.TELEPORTATION_CORE, MekanismItems.ENRICHED_IRON,
                  MekanismItems.SAWDUST, MekanismItems.SALT, MekanismItems.DYE_BASE, MekanismItems.FLUORITE_GEM, MekanismItems.FLUORITE_DUST,
                  MekanismItems.YELLOW_CAKE_URANIUM, MekanismItems.DIRTY_NETHERITE_SCRAP, MekanismItems.NETHERITE_DUST, MekanismItems.CHARCOAL_DUST,
                  MekanismItems.COAL_DUST, MekanismItems.SULFUR_DUST, MekanismItems.BRONZE_DUST, MekanismItems.LAPIS_LAZULI_DUST, MekanismItems.QUARTZ_DUST,
                  MekanismItems.EMERALD_DUST, MekanismItems.DIAMOND_DUST, MekanismItems.STEEL_DUST, MekanismItems.OBSIDIAN_DUST, MekanismItems.REFINED_OBSIDIAN_DUST,
                  MekanismItems.BRONZE_NUGGET, MekanismItems.STEEL_NUGGET, MekanismItems.REFINED_OBSIDIAN_NUGGET, MekanismItems.REFINED_GLOWSTONE_NUGGET,
                  MekanismItems.BRONZE_INGOT, MekanismItems.STEEL_INGOT, MekanismItems.REFINED_OBSIDIAN_INGOT, MekanismItems.REFINED_GLOWSTONE_INGOT
            );
            for (Holder<Item> item : MekanismItems.PROCESSED_RESOURCES.values()) {
                CreativeTabDeferredRegister.addToDisplay(event, item);
            }
        }
    }
}