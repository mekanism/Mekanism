package mekanism.common.item;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.fluid.IExtendedFluidHandler;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.GaugeDropperContentsHandler;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class ItemGaugeDropper extends Item {

    public ItemGaugeDropper(Properties properties) {
        super(properties.maxStackSize(1));
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return StorageUtils.getDurabilityForDisplay(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        //TODO: Technically doesn't support things where the color is part of the texture such as lava
        GasStack gasStack = StorageUtils.getStoredGasFromNBT(stack);
        if (!gasStack.isEmpty()) {
            return gasStack.getType().getTint();
        }
        InfusionStack infusionStack = StorageUtils.getStoredInfusionFromNBT(stack);
        if (!infusionStack.isEmpty()) {
            return infusionStack.getType().getTint();
        }
        FluidStack fluidStack = StorageUtils.getStoredFluidFromNBT(stack);
        if (!fluidStack.isEmpty()) {
            return fluidStack.getFluid().getAttributes().getColor(fluidStack);
        }
        return 0;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isShiftKeyDown() && !world.isRemote) {
            Optional<IGasHandler> gasCapability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
            if (gasCapability.isPresent()) {
                IGasHandler gasHandlerItem = gasCapability.get();
                for (int tank = 0; tank < gasHandlerItem.getGasTankCount(); tank++) {
                    gasHandlerItem.setGasInTank(tank, GasStack.EMPTY);
                }
            }
            Optional<IInfusionHandler> infusionCapability = MekanismUtils.toOptional(stack.getCapability(Capabilities.INFUSION_HANDLER_CAPABILITY));
            if (infusionCapability.isPresent()) {
                IInfusionHandler infusionHandlerItem = infusionCapability.get();
                for (int tank = 0; tank < infusionHandlerItem.getInfusionTankCount(); tank++) {
                    infusionHandlerItem.setInfusionInTank(tank, InfusionStack.EMPTY);
                }
            }
            Optional<IFluidHandlerItem> fluidCapability = MekanismUtils.toOptional(stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY));
            if (fluidCapability.isPresent()) {
                IFluidHandlerItem fluidHandler = fluidCapability.get();
                if (fluidHandler instanceof IExtendedFluidHandler) {
                    IExtendedFluidHandler fluidHandlerItem = (IExtendedFluidHandler) fluidHandler;
                    for (int tank = 0; tank < fluidHandlerItem.getTanks(); tank++) {
                        fluidHandlerItem.setFluidInTank(tank, FluidStack.EMPTY);
                    }
                }
            }
            ((ServerPlayerEntity) player).sendContainerToPlayer(player.openContainer);
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        GasStack gasStack = StorageUtils.getStoredGasFromNBT(stack);
        InfusionStack infusionStack = StorageUtils.getStoredInfusionFromNBT(stack);
        FluidStack fluidStack = StorageUtils.getStoredFluidFromNBT(stack);
        if (gasStack.isEmpty() && infusionStack.isEmpty() && fluidStack.isEmpty()) {
            tooltip.add(MekanismLang.EMPTY.translate());
        } else if (!gasStack.isEmpty()) {
            tooltip.add(MekanismLang.STORED.translate(gasStack, gasStack.getAmount()));
        } else if (!infusionStack.isEmpty()) {
            tooltip.add(MekanismLang.STORED.translate(infusionStack, infusionStack.getAmount()));
        } else if (!fluidStack.isEmpty()) {
            tooltip.add(MekanismLang.STORED.translate(fluidStack, fluidStack.getAmount()));
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemCapabilityWrapper(stack, GaugeDropperContentsHandler.create());
    }
}