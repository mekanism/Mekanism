package mekanism.client.render.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.GuiUtils;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.Optional;
import java.util.function.Predicate;

public class ChemicalFluidBarDecorator implements IItemDecorator {

    private final Capability<? extends IChemicalHandler<?,?>>[] chemicalCaps;
    private final boolean showFluid;
    private final Predicate<ItemStack> visibleFor;

    /**
     * @param showFluid if the fluidcapability should be checked for display, display above chemicalCaps if both are present
     * @param visibleFor checks if bars should be rendered for the given itemstack
     * @param chemicalCaps the capabilities to be displayed in order, starting from the bottom
     */
    @SafeVarargs
    public ChemicalFluidBarDecorator(boolean showFluid, Predicate<ItemStack> visibleFor, Capability<? extends IChemicalHandler<?, ?>>... chemicalCaps) {
        this.showFluid = showFluid;
        this.chemicalCaps = chemicalCaps;
        this.visibleFor = visibleFor;
    }

    @Override
    public boolean render(Font font, ItemStack stack, int xOffset, int yOffset, float blitOffset) {
        if (!visibleFor.test(stack)) {
            return false;
        }
        RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();
        yOffset = yOffset + 12;
        for (Capability<? extends IChemicalHandler<?,?>> chemicalCap : chemicalCaps) {
            Optional<? extends IChemicalHandler<?, ?>> capabilityInstance = stack.getCapability(chemicalCap).resolve();
            if (capabilityInstance.isPresent()) {
                IChemicalHandler<?, ?> chemicalHandler = capabilityInstance.get();
                if (chemicalHandler.getTanks() == 0) {
                    continue;
                }
                int tank = getDisplayTank(chemicalHandler.getTanks());
                if (tank != -1) {
                    renderBarChemical(xOffset, yOffset, chemicalHandler.getChemicalInTank(tank), chemicalHandler.getTankCapacity(tank));
                    yOffset--;
                }
            }
        }

        if (showFluid) {
            Optional<IFluidHandlerItem> capabilityInstance = FluidUtil.getFluidHandler(stack).resolve();
            if (capabilityInstance.isPresent()) {
                IFluidHandlerItem fluidHandler = capabilityInstance.get();
                if (fluidHandler.getTanks() != 0) {
                    int tank = getDisplayTank(fluidHandler.getTanks());
                    if (tank != -1) {
                        renderBarFluid(xOffset, yOffset, fluidHandler.getFluidInTank(tank), fluidHandler.getTankCapacity(tank));
                    }
                }
            }
        }
        return true;
    }

    private void renderBarChemical( int stackXPos, int yPos, ChemicalStack<?> stack, long capacity) {
        int color = stack.getChemicalColorRepresentation();
        renderBar(stackXPos, yPos, StorageUtils.getRatio(stack.getAmount(), capacity), color);
    }

    private void renderBarFluid(int stackXPos, int yPos, FluidStack stack, long capacity) {
        int color = FluidUtils.getRGBDurabilityForDisplay(stack).orElse(0xFFFFFFFF);
        renderBar(stackXPos, yPos, StorageUtils.getRatio(stack.getAmount(), capacity), color);
    }

    private void renderBar(int stackXPos, int yPos, double width, int color) {
        int pixelWidth = convertWidth(width);
        GuiUtils.fill(new PoseStack(), stackXPos + 2 + pixelWidth, yPos, 13 - pixelWidth, 1, 0xFF000000);
        GuiUtils.fill(new PoseStack(), stackXPos + 2, yPos, pixelWidth, 1, color);
    }

    private int convertWidth(double width) {
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
