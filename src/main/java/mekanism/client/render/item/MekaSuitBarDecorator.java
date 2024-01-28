package mekanism.client.render.item;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.GenericTankSpec;
import mekanism.common.capabilities.chemical.item.ChemicalTankSpec;
import mekanism.common.capabilities.fluid.item.RateLimitMultiTankFluidHandler.FluidTankSpec;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.util.FluidUtils;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

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

        if (tryRender(guiGraphics, stack, Capabilities.GAS.item(), xOffset, yOffset, armor.getGasTankSpecs())) {
            yOffset--;
        }
        //TODO: Other chemical types as they get added to different meka suit pieces

        List<FluidTankSpec> fluidTankSpecs = armor.getFluidTankSpecs();
        if (!fluidTankSpecs.isEmpty()) {
            IFluidHandlerItem fluidHandler = Capabilities.FLUID.getCapability(stack);
            if (fluidHandler != null) {
                int tank = getDisplayTank(fluidTankSpecs, stack, fluidHandler.getTanks());
                if (tank != -1) {
                    FluidStack fluidInTank = fluidHandler.getFluidInTank(tank);
                    ChemicalFluidBarDecorator.renderBar(guiGraphics, xOffset, yOffset, fluidInTank.getAmount(), fluidHandler.getTankCapacity(tank),
                          FluidUtils.getRGBDurabilityForDisplay(stack).orElse(0xFFFFFFFF));
                }
            }
        }
        return true;
    }

    private <CHEMICAL extends Chemical<CHEMICAL>> boolean tryRender(GuiGraphics guiGraphics, ItemStack stack,
          ItemCapability<? extends IChemicalHandler<CHEMICAL, ?>, Void> capability, int xOffset, int yOffset, List<ChemicalTankSpec<CHEMICAL>> chemicalTankSpecs) {
        if (!chemicalTankSpecs.isEmpty() && chemicalTankSpecs.stream().anyMatch(spec -> spec.supportsStack(stack))) {
            IChemicalHandler<CHEMICAL, ?> chemicalHandler = stack.getCapability(capability);
            if (chemicalHandler != null) {
                int tank = getDisplayTank(chemicalTankSpecs, stack, chemicalHandler.getTanks());
                if (tank != -1) {
                    ChemicalStack<CHEMICAL> chemicalInTank = chemicalHandler.getChemicalInTank(tank);
                    ChemicalFluidBarDecorator.renderBar(guiGraphics, xOffset, yOffset, chemicalInTank.getAmount(), chemicalHandler.getTankCapacity(tank),
                          chemicalInTank.getChemicalColorRepresentation());
                    return true;
                }
            }
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
            if (tankIndices.isEmpty()) {
                return -1;
            } else if (tankIndices.size() == 1) {
                return tankIndices.getInt(0);
            }
            //Cycle through multiple tanks every second, to save some space if multiple tanks are present
            return tankIndices.getInt((int) (Minecraft.getInstance().level.getGameTime() / SharedConstants.TICKS_PER_SECOND) % tankIndices.size());
        }
        for (int i = 0; i < tanks && i < tankSpecs.size(); i++) {
            if (tankSpecs.get(i).supportsStack(stack)) {
                return i;
            }
        }
        return -1;
    }
}