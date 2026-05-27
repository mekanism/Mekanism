package mekanism.client.render.item;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.function.ToIntFunction;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.resource.IResourceContainer;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.attachments.containers.type.ResourceContainerType;
import mekanism.common.capabilities.GenericTankSpec;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.util.FluidUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;

public class MekaSuitBarDecorator implements IItemDecorator {

    public static final MekaSuitBarDecorator INSTANCE = new MekaSuitBarDecorator();

    private MekaSuitBarDecorator() {
    }

    @Override
    public boolean render(GuiGraphicsExtractor guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemMekaSuitArmor armor)) {
            return false;
        }
        yOffset += 12;

        ItemAccess itemAccess = ItemAccess.forStack(stack);
        if (tryRender(guiGraphics, itemAccess, xOffset, yOffset, ContainerType.CHEMICAL, armor.getChemicalTankSpecs(), ChemicalResource::getChemicalColorRepresentation)) {
            yOffset--;
        }

        tryRender(guiGraphics, itemAccess, xOffset, yOffset, ContainerType.FLUID, armor.getFluidTankSpecs(), FluidUtils::getRGBDurabilityForDisplay);
        return true;
    }

    private <RESOURCE extends Resource> boolean tryRender(GuiGraphicsExtractor guiGraphics, ItemAccess itemAccess, int xOffset, int yOffset,
          ResourceContainerType<RESOURCE, ?> containerType, List<GenericTankSpec<RESOURCE>> tankSpecs, ToIntFunction<RESOURCE> color) {
        if (!tankSpecs.isEmpty()) {
            //Note: We just directly query the stored contents of the containers and don't care about the size of the item access
            List<? extends IResourceContainer<RESOURCE>> tanks = containerType.getAttachmentContainersIfPresent(itemAccess);
            int tank = getDisplayTank(tankSpecs, itemAccess.getResource(), tanks.size());
            return ChemicalFluidBarDecorator.renderBars(guiGraphics, xOffset, yOffset, tanks, tank, color);
        }
        return false;
    }

    private static <RESOURCE extends Resource> int getDisplayTank(List<GenericTankSpec<RESOURCE>> tankSpecs, ItemResource itemType, int tanks) {
        if (tanks == 0) {
            return -1;
        } else if (tanks > 1 && tanks == tankSpecs.size() && Minecraft.getInstance().level != null) {
            IntList tankIndices = new IntArrayList(tanks);
            for (int i = 0; i < tanks; i++) {
                if (tankSpecs.get(i).supportsStack(itemType)) {
                    tankIndices.add(i);
                }
            }
            int displayTank = ChemicalFluidBarDecorator.getDisplayTank(tankIndices.size());
            return displayTank == -1 ? -1 : tankIndices.getInt(displayTank);
        }
        for (int i = 0; i < tanks && i < tankSpecs.size(); i++) {
            if (tankSpecs.get(i).supportsStack(itemType)) {
                return i;
            }
        }
        return -1;
    }
}