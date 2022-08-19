package mekanism.client.render.item;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Optional;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.GenericTankSpec;
import mekanism.common.capabilities.chemical.item.ChemicalTankSpec;
import mekanism.common.capabilities.fluid.item.RateLimitMultiTankFluidHandler.FluidTankSpec;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.util.FluidUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class MekaSuitBarDecorator implements IItemDecorator {

    public static final MekaSuitBarDecorator INSTANCE = new MekaSuitBarDecorator();

    private MekaSuitBarDecorator() {
    }

    @Override
    public boolean render(Font font, ItemStack stack, int xOffset, int yOffset, float blitOffset) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemMekaSuitArmor armor)) {
            return false;
        }
        RenderSystem.disableDepthTest();
        yOffset += 12;

        if (tryRender(stack, Capabilities.GAS_HANDLER, xOffset, yOffset, armor.getGasTankSpecs())) {
            yOffset--;
        }
        //TODO: Other chemical types as they get added to different meka suit pieces

        List<FluidTankSpec> fluidTankSpecs = armor.getFluidTankSpecs();
        if (!fluidTankSpecs.isEmpty()) {
            Optional<IFluidHandlerItem> capabilityInstance = FluidUtil.getFluidHandler(stack).resolve();
            if (capabilityInstance.isPresent()) {
                IFluidHandlerItem fluidHandler = capabilityInstance.get();
                int tank = getDisplayTank(fluidTankSpecs, stack, fluidHandler.getTanks());
                if (tank != -1) {
                    FluidStack fluidInTank = fluidHandler.getFluidInTank(tank);
                    ChemicalFluidBarDecorator.renderBar(xOffset, yOffset, fluidInTank.getAmount(), fluidHandler.getTankCapacity(tank),
                          FluidUtils.getRGBDurabilityForDisplay(stack).orElse(0xFFFFFFFF));
                }
            }
        }
        return true;
    }

    private <CHEMICAL extends Chemical<CHEMICAL>> boolean tryRender(ItemStack stack, Capability<? extends IChemicalHandler<CHEMICAL, ?>> capability,
          int xOffset, int yOffset, List<ChemicalTankSpec<CHEMICAL>> chemicalTankSpecs) {
        if (!chemicalTankSpecs.isEmpty() && chemicalTankSpecs.stream().anyMatch(spec -> spec.supportsStack(stack))) {
            Optional<? extends IChemicalHandler<CHEMICAL, ?>> capabilityInstance = stack.getCapability(capability).resolve();
            if (capabilityInstance.isPresent()) {
                IChemicalHandler<CHEMICAL, ?> chemicalHandler = capabilityInstance.get();
                int tank = getDisplayTank(chemicalTankSpecs, stack, chemicalHandler.getTanks());
                if (tank != -1) {
                    ChemicalStack<CHEMICAL> chemicalInTank = chemicalHandler.getChemicalInTank(tank);
                    ChemicalFluidBarDecorator.renderBar(xOffset, yOffset, chemicalInTank.getAmount(), chemicalHandler.getTankCapacity(tank),
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
            return tankIndices.getInt((int) (Minecraft.getInstance().level.getGameTime() / 20) % tankIndices.size());
        }
        for (int i = 0; i < tanks && i < tankSpecs.size(); i++) {
            if (tankSpecs.get(i).supportsStack(stack)) {
                return i;
            }
        }
        return -1;
    }
}