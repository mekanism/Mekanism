package mekanism.client.render.item;

import java.util.function.Predicate;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.GuiUtils;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public class ChemicalFluidBarDecorator implements IItemDecorator {

    private final ItemCapability<? extends IChemicalHandler<?, ?>, Void>[] chemicalCaps;
    private final boolean showFluid;
    private final Predicate<ItemStack> visibleFor;

    /**
     * @param showFluid    if the fluid capability should be checked for display, display above chemicalCaps if both are present
     * @param visibleFor   checks if bars should be rendered for the given itemstack
     * @param chemicalCaps the capabilities to be displayed in order, starting from the bottom
     */
    @SafeVarargs
    public ChemicalFluidBarDecorator(boolean showFluid, Predicate<ItemStack> visibleFor, ItemCapability<? extends IChemicalHandler<?, ?>, Void>... chemicalCaps) {
        this.showFluid = showFluid;
        this.chemicalCaps = chemicalCaps;
        this.visibleFor = visibleFor;
    }

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (!visibleFor.test(stack)) {
            return false;
        }
        yOffset += 12;
        for (ItemCapability<? extends IChemicalHandler<?, ?>, Void> chemicalCap : chemicalCaps) {
            IChemicalHandler<?, ?> chemicalHandler = stack.getCapability(chemicalCap);
            if (chemicalHandler != null) {
                int tank = getDisplayTank(chemicalHandler.getTanks());
                if (tank != -1) {
                    ChemicalStack<?> chemicalInTank = chemicalHandler.getChemicalInTank(tank);
                    renderBar(guiGraphics, xOffset, yOffset, chemicalInTank.getAmount(), chemicalHandler.getTankCapacity(tank), chemicalInTank.getChemicalColorRepresentation());
                    yOffset--;
                }
            }
        }

        if (showFluid) {
            IFluidHandlerItem fluidHandler = Capabilities.FLUID.getCapability(stack);
            if (fluidHandler != null) {
                int tank = getDisplayTank(fluidHandler.getTanks());
                if (tank != -1) {
                    FluidStack fluidInTank = fluidHandler.getFluidInTank(tank);
                    renderBar(guiGraphics, xOffset, yOffset, fluidInTank.getAmount(), fluidHandler.getTankCapacity(tank), FluidUtils.getRGBDurabilityForDisplay(stack).orElse(0xFFFFFFFF));
                }
            }
        }
        return true;
    }

    protected static void renderBar(GuiGraphics guiGraphics, int stackXPos, int yPos, long amount, long capacity, int color) {
        int pixelWidth = convertWidth(StorageUtils.getRatio(amount, capacity));
        GuiUtils.fill(guiGraphics, RenderType.guiOverlay(), stackXPos + 2 + pixelWidth, yPos, 13 - pixelWidth, 1, 0xFF000000);
        GuiUtils.fill(guiGraphics, RenderType.guiOverlay(), stackXPos + 2, yPos, pixelWidth, 1, color | 0xFF000000);
    }

    private static int convertWidth(double width) {
        return MathUtils.clampToInt(Math.round(13.0F * width));
    }

    private int getDisplayTank(int tanks) {
        if (tanks == 0) {
            return -1;
        } else if (tanks > 1 && Minecraft.getInstance().level != null) {
            //Cycle through multiple tanks every second, to save some space if multiple tanks are present
            return (int) (Minecraft.getInstance().level.getGameTime() / 20) % tanks;
        }
        return 0;
    }
}