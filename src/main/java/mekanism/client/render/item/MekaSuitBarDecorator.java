package mekanism.client.render.item;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.GenericTankSpec;
import mekanism.common.capabilities.chemical.item.ChemicalTankSpec;
import mekanism.common.capabilities.fluid.item.FluidTankSpec;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

public class MekaSuitBarDecorator implements IItemDecorator {

    public static final MekaSuitBarDecorator INSTANCE = new MekaSuitBarDecorator();

    private MekaSuitBarDecorator() {
    }

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemMekaSuitArmor armor)) {
            return false;
        }
        yOffset += 12;

        if (tryRender(guiGraphics, stack, xOffset, yOffset, armor.getChemicalTankSpecs())) {
            yOffset--;
        }

        List<FluidTankSpec> fluidTankSpecs = armor.getFluidTankSpecs();
        if (!fluidTankSpecs.isEmpty()) {
            List<IExtendedFluidTank> tanks = ContainerType.FLUID.getAttachmentContainersIfPresent(stack);
            int tank = getDisplayTank(fluidTankSpecs, stack, tanks.size());
            if (tank != -1) {
                ChemicalFluidBarDecorator.renderBar(guiGraphics, xOffset, yOffset, tanks.get(tank));
            } else if (tanks.isEmpty()) {
                ChemicalFluidBarDecorator.renderBar(guiGraphics, xOffset, yOffset, 0, 1, 0xFFFFFFFF);
            }
        }
        return true;
    }

    private boolean tryRender(GuiGraphics guiGraphics, ItemStack stack, int xOffset, int yOffset, List<ChemicalTankSpec> chemicalTankSpecs) {
        if (!chemicalTankSpecs.isEmpty()) {
            List<IChemicalTank> tanks = ContainerType.CHEMICAL.getAttachmentContainersIfPresent(stack);
            int tank = getDisplayTank(chemicalTankSpecs, stack, tanks.size());
            if (tank != -1) {
                ChemicalFluidBarDecorator.renderBar(guiGraphics, xOffset, yOffset, tanks.get(tank));
            } else if (tanks.isEmpty()) {
                ChemicalFluidBarDecorator.renderBar(guiGraphics, xOffset, yOffset, 0, 1, 0xFFFFFFFF);
            }
            return true;
        }
        return false;
    }

    private static <TYPE> int getDisplayTank(List<? extends GenericTankSpec<TYPE>> tankSpecs, ItemStack stack, int tanks) {
        if (tanks == 0) {
            return -1;
        } else if (tanks > 1 && tanks == tankSpecs.size() && Minecraft.getInstance().level != null) {
            IntList tankIndices = new IntArrayList(tanks);
            for (int i = 0; i < tanks; i++) {
                if (tankSpecs.get(i).supportsStack(stack)) {
                    tankIndices.add(i);
                }
            }
            int displayTank = ChemicalFluidBarDecorator.getDisplayTank(tankIndices.size());
            return displayTank == -1 ? -1 : tankIndices.getInt(displayTank);
        }
        for (int i = 0; i < tanks && i < tankSpecs.size(); i++) {
            if (tankSpecs.get(i).supportsStack(stack)) {
                return i;
            }
        }
        return -1;
    }
}