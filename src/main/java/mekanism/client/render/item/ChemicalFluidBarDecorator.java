package mekanism.client.render.item;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.GuiUtils;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.attachments.containers.type.ResourceContainerType;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.resource.Resource;

public class ChemicalFluidBarDecorator implements IItemDecorator {

    private final boolean showFluid;
    private final boolean showChemical;
    private final Predicate<ItemStack> visibleFor;

    /// @param showFluid    if the fluid capability should be checked for display, display above chemicalCaps if both are present
    /// @param showChemical if the chemical capability should be checked for display
    /// @param visibleFor   checks if bars should be rendered for the given itemstack
    public ChemicalFluidBarDecorator(boolean showFluid, boolean showChemical, Predicate<ItemStack> visibleFor) {
        this.showFluid = showFluid;
        this.showChemical = showChemical;
        this.visibleFor = visibleFor;
    }

    @Override
    public boolean render(GuiGraphicsExtractor guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (!visibleFor.test(stack)) {
            return false;
        }
        yOffset += 12;
        ItemAccess itemAccess = ItemAccessUtils.sideEffectFreeAccess(stack);
        if (showChemical && renderBars(guiGraphics, xOffset, yOffset, ContainerType.CHEMICAL, itemAccess, ChemicalResource::getChemicalColorRepresentation)) {
            yOffset--;
        }

        if (showFluid) {
            renderBars(guiGraphics, xOffset, yOffset, ContainerType.FLUID, itemAccess, FluidUtils::getRGBDurabilityForDisplay);
        }
        return true;
    }

    private static <RESOURCE extends Resource> boolean renderBars(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset, ResourceContainerType<RESOURCE, ?> containerType,
          ItemAccess itemAccess, ToIntFunction<RESOURCE> color) {
        //Note: We just directly query the stored contents of the containers and don't care about the size of the item access
        ResourceHandler<RESOURCE> handler = containerType.getCapOrUnexposed(itemAccess);
        return handler != null && renderBars(guiGraphics, xOffset, yOffset, handler, getDisplayTank(handler.size()), color);
    }

    protected static <RESOURCE extends Resource> boolean renderBars(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset,
          ResourceHandler<RESOURCE> handler, int index, ToIntFunction<RESOURCE> color) {
        if (index != -1) {
            RESOURCE resource = handler.getResource(index);
            renderBar(guiGraphics, xOffset, yOffset, handler.getAmountAsLong(index), handler.getCapacityAsLong(index, resource), color.applyAsInt(resource));
        } else if (handler.size() == 0) {
            renderBar(guiGraphics, xOffset, yOffset, 0, 1, 0xFFFFFFFF);
        } else {
            return false;
        }
        return true;
    }

    private static void renderBar(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset, long amount, long capacity, int color) {
        int pixelWidth = StorageUtils.getBarWidth(MathUtils.divideToLevel(amount, capacity));
        GuiUtils.fill(guiGraphics, xOffset + 2 + pixelWidth, yOffset, 13 - pixelWidth, 1, 0xFF000000);
        GuiUtils.fill(guiGraphics, xOffset + 2, yOffset, pixelWidth, 1, color | 0xFF000000);
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