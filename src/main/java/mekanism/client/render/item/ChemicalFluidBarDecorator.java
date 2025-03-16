package mekanism.client.render.item;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.GuiUtils;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.fluids.FluidStack;

public class ChemicalFluidBarDecorator implements IItemDecorator {

    private final ContainerType<IChemicalTank, ?, ?>[] chemicalContainerTypes;
    private final boolean showFluid;
    private final Predicate<ItemStack> visibleFor;

    /**
     * @param showFluid              if the fluid capability should be checked for display, display above chemicalCaps if both are present
     * @param visibleFor             checks if bars should be rendered for the given itemstack
     * @param chemicalContainerTypes the container types to be displayed in order, starting from the bottom
     */
    @SafeVarargs
    public ChemicalFluidBarDecorator(boolean showFluid, Predicate<ItemStack> visibleFor, ContainerType<IChemicalTank, ?, ?>... chemicalContainerTypes) {
        this.showFluid = showFluid;
        this.chemicalContainerTypes = chemicalContainerTypes;
        this.visibleFor = visibleFor;
    }

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (!visibleFor.test(stack)) {
            return false;
        }
        yOffset += 12;
        for (ContainerType<? extends IChemicalTank, ?, ?> chemicalContainerType : chemicalContainerTypes) {
            List<? extends IChemicalTank> tanks = chemicalContainerType.getAttachmentContainersIfPresent(stack);
            int tank = getDisplayTank(tanks.size());
            if (tank != -1) {
                renderBar(guiGraphics, xOffset, yOffset, tanks.get(tank));
                yOffset--;
            } else if (tanks.isEmpty()) {
                renderBar(guiGraphics, xOffset, yOffset, 0, 1, 0xFFFFFFFF);
            }
        }

        if (showFluid) {
            List<IExtendedFluidTank> tanks = ContainerType.FLUID.getAttachmentContainersIfPresent(stack);
            int tank = getDisplayTank(tanks.size());
            if (tank != -1) {
                renderBar(guiGraphics, xOffset, yOffset, tanks.get(tank));
            } else if (tanks.isEmpty()) {
                renderBar(guiGraphics, xOffset, yOffset, 0, 1, 0xFFFFFFFF);
            }
        }
        return true;
    }

    protected static void renderBar(GuiGraphics guiGraphics, int stackXPos, int yPos, IChemicalTank tank) {
        renderBar(guiGraphics, stackXPos, yPos, tank.getStored(), tank.getCapacity(), tank.getStack().getChemicalColorRepresentation());
    }

    protected static void renderBar(GuiGraphics guiGraphics, int stackXPos, int yPos, IExtendedFluidTank tank) {
        FluidStack fluid = tank.getFluid();
        renderBar(guiGraphics, stackXPos, yPos, fluid.getAmount(), tank.getCapacity(), FluidUtils.getRGBDurabilityForDisplay(fluid));
    }

    protected static void renderBar(GuiGraphics guiGraphics, int stackXPos, int yPos, long amount, long capacity, int color) {
        int pixelWidth = convertWidth(StorageUtils.getRatio(amount, capacity));
        GuiUtils.fill(guiGraphics, RenderType.guiOverlay(), stackXPos + 2 + pixelWidth, yPos, 13 - pixelWidth, 1, 0xFF000000);
        GuiUtils.fill(guiGraphics, RenderType.guiOverlay(), stackXPos + 2, yPos, pixelWidth, 1, color | 0xFF000000);
    }

    private static int convertWidth(double width) {
        return MathUtils.clampToInt(Math.round(13.0F * width));
    }

    static int getDisplayTank(int tanks) {
        if (tanks == 0) {
            return -1;
        } else if (tanks > 1) {
            //Cycle through multiple tanks every second, to save some space if multiple tanks are present
            return (Minecraft.getInstance().gui.getGuiTicks() / SharedConstants.TICKS_PER_SECOND) % tanks;
        }
        return 0;
    }
}