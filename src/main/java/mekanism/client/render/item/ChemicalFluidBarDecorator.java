package mekanism.client.render.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.math.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
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
    public ChemicalFluidBarDecorator(boolean showFluid, Predicate<ItemStack> visibleFor, Capability<? extends IChemicalHandler<?,?>>... chemicalCaps) {
        this.showFluid = showFluid;
        this.chemicalCaps = chemicalCaps;
        this.visibleFor = visibleFor;
    }

    @Override
    public boolean render(Font font, ItemStack stack, int xOffset, int yOffset, float blitOffset) {
        if (!visibleFor.test(stack))
            return false;
        RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        yOffset = yOffset + 12;
        for (Capability<? extends IChemicalHandler<?,?>> chemicalCap : chemicalCaps) {
            Optional<? extends IChemicalHandler<?, ?>> capabilityInstance = stack.getCapability(chemicalCap).resolve();
            if (capabilityInstance.isPresent()) {
                IChemicalHandler<?, ?> chemicalHandler = capabilityInstance.get();
                if (chemicalHandler.getTanks() == 0) {
                    continue;
                }
                int tank = 0;
                if (chemicalHandler.getTanks() > 1 && Minecraft.getInstance().level != null) {
                    //Cycle through multiple tanks every second, to save some space if multiple tanks are present
                    tank = (int) (Minecraft.getInstance().level.getGameTime() / 20) % chemicalHandler.getTanks();
                }
                renderBar(bufferBuilder, xOffset, yOffset, chemicalHandler.getChemicalInTank(tank), chemicalHandler.getTankCapacity(tank));
                yOffset--;
            }
        }

        if (showFluid) {
            Optional<IFluidHandlerItem> capabilityInstance = FluidUtil.getFluidHandler(stack).resolve();
            if (capabilityInstance.isPresent()) {
                IFluidHandlerItem fluidHandler = capabilityInstance.get();
                if (fluidHandler.getTanks() != 0) {
                    int tank = 0;
                    if (fluidHandler.getTanks() > 1 && Minecraft.getInstance().level != null) {
                        //Cycle through multiple tanks every second, to save some space if multiple tanks are present
                        tank = (int) (Minecraft.getInstance().level.getGameTime() / 20) % fluidHandler.getTanks();
                    }
                    renderBar(bufferBuilder, xOffset, yOffset, fluidHandler.getFluidInTank(tank), fluidHandler.getTankCapacity(tank));
                }
            }
        }
        return true;
    }

    private void renderBar(BufferBuilder bufferBuilder, int stackXPos, int yPos, ChemicalStack<?> stack, long capacity) {
        int color = stack.getChemicalColorRepresentation();
        renderBar(bufferBuilder, stackXPos, yPos, (float)((double) stack.getAmount() / capacity), color);
    }

    private void renderBar(BufferBuilder bufferBuilder, int stackXPos, int yPos, FluidStack stack, long capacity) {
        int color = IClientFluidTypeExtensions.of(stack.getFluid()).getTintColor(stack);
        renderBar(bufferBuilder, stackXPos, yPos, (float)((double) stack.getAmount() / capacity), color);
    }

    private void renderBar(BufferBuilder bufferBuilder, int stackXPos, int yPos, float width, int color) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.fillRect(bufferBuilder, stackXPos + 2, yPos, 13, 1, 0, 0, 0, 255);
        itemRenderer.fillRect(bufferBuilder, stackXPos + 2, yPos, convertWidth(width), 1, getRed(color), getGreen(color), getBlue(color), 255);
    }

    private int convertWidth(double width) {
        return MathUtils.clampToInt(Math.round(13.0F * width));
    }

    private int getRed(int color) {
        return color >> 16 & 0xFF;
    }

    private int getGreen(int color) {
        return color >> 8 & 0xFF;
    }

    private int getBlue(int color) {
        return color & 0xFF;
    }
}
