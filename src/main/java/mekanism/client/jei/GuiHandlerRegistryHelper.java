package mekanism.client.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import mekanism.api.providers.IBlockProvider;
import mekanism.client.gui.machine.GuiAdvancedElectricMachine;
import mekanism.client.gui.machine.GuiElectricMachine;
import mekanism.client.gui.machine.GuiRotaryCondensentrator;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.machine.TileEntityChemicalInjectionChamber;
import mekanism.common.tile.machine.TileEntityCrusher;
import mekanism.common.tile.machine.TileEntityEnergizedSmelter;
import mekanism.common.tile.machine.TileEntityEnrichmentChamber;
import mekanism.common.tile.machine.TileEntityOsmiumCompressor;
import mekanism.common.tile.machine.TileEntityPurificationChamber;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.util.ResourceLocation;

public class GuiHandlerRegistryHelper {

    public static <T extends ContainerScreen<?>> void register(IGuiHandlerRegistration registry, IBlockProvider mekanismBlock, Class<? extends T> guiContainerClass,
          int xPos, int yPos, int width, int height) {
        registry.addRecipeClickArea(guiContainerClass, xPos, yPos, width, height, mekanismBlock.getRegistryName());
    }

    public static <T extends ContainerScreen<?>> void register(IGuiHandlerRegistration registry, Class<? extends T> guiContainerClass, ResourceLocation recipeType,
        int xPos, int yPos, int width, int height) {
        registry.addRecipeClickArea(guiContainerClass, xPos, yPos, width, height, recipeType);
    }

    public static void registerCondensentrator(IGuiHandlerRegistration registry) {
        ResourceLocation condensentrating = new ResourceLocation(Mekanism.MODID, "rotary_condensentrator_condensentrating");
        ResourceLocation decondensentrating = new ResourceLocation(Mekanism.MODID, "rotary_condensentrator_decondensentrating");
        registry.addRecipeClickArea(GuiRotaryCondensentrator.class, 64, 39, 48, 8, condensentrating, decondensentrating);
    }

    @SuppressWarnings("rawtypes")
    public static void registerElectricMachines(IGuiHandlerRegistration registry) {
        registry.addGuiContainerHandler(GuiElectricMachine.class, new IGuiContainerHandler<GuiElectricMachine>() {
            @Override
            public Collection<IGuiClickableArea> getGuiClickableAreas(GuiElectricMachine containerScreen) {
                TileEntityMekanism tile = containerScreen.getTileEntity();
                List<ResourceLocation> categories = new ArrayList<>();
                if (tile instanceof TileEntityCrusher) {
                    categories.add(MekanismBlocks.CRUSHER.getRegistryName());
                } else if (tile instanceof TileEntityEnrichmentChamber) {
                    categories.add(MekanismBlocks.ENRICHMENT_CHAMBER.getRegistryName());
                } else if (tile instanceof TileEntityEnergizedSmelter) {
                    IBlockProvider mekanismBlock = MekanismBlocks.ENERGIZED_SMELTER;
                    /*if (Mekanism.hooks.CraftTweakerLoaded && EnergizedSmelter.hasRemovedRecipe()) {// Removed / Removed + Added
                        categories.add(mekanismBlock.getRegistryName());
                    } else if (Mekanism.hooks.CraftTweakerLoaded && EnergizedSmelter.hasAddedRecipe()) {// Added but not removed
                        categories.add(VanillaRecipeCategoryUid.FURNACE);
                        categories.add(mekanismBlock.getRegistryName());
                    } else {*/
                        //Only use furnace list, so no extra registration.
                        categories.add(VanillaRecipeCategoryUid.FURNACE);
                    //}
                }
                return Collections.singleton(IGuiClickableArea.createBasic(79, 40, 24, 7, categories.toArray(new ResourceLocation[0])));
            }
        });
    }

    @SuppressWarnings("rawtypes")
    public static void registerAdvancedElectricMachines(IGuiHandlerRegistration registry) {
        registry.addGuiContainerHandler(GuiAdvancedElectricMachine.class, new IGuiContainerHandler<GuiAdvancedElectricMachine>() {
            @Override
            public Collection<IGuiClickableArea> getGuiClickableAreas(GuiAdvancedElectricMachine containerScreen) {
                TileEntityMekanism tile = containerScreen.getTileEntity();
                List<ResourceLocation> categories = new ArrayList<>();
                if (tile instanceof TileEntityOsmiumCompressor) {
                    categories.add(MekanismBlocks.OSMIUM_COMPRESSOR.getRegistryName());
                } else if (tile instanceof TileEntityPurificationChamber) {
                    categories.add(MekanismBlocks.PURIFICATION_CHAMBER.getRegistryName());
                } else if (tile instanceof TileEntityChemicalInjectionChamber) {
                    categories.add(MekanismBlocks.CHEMICAL_INJECTION_CHAMBER.getRegistryName());
                }
                return Collections.singleton(IGuiClickableArea.createBasic(87, 41, 24, 7, categories.toArray(new ResourceLocation[0])));
            }
        });
    }
}