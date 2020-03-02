package mekanism.common.item;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.chemical.RateLimitGasHandler;
import mekanism.common.capabilities.fluid.RateLimitFluidHandler;
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
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class ItemGaugeDropper extends Item {

    private static final int TRANSFER_RATE = 16;
    public static int CAPACITY = FluidAttributes.BUCKET_VOLUME;

    public ItemGaugeDropper(Properties properties) {
        super(properties.maxStackSize(1));
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        if (CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY != null && Capabilities.GAS_HANDLER_CAPABILITY != null) {
            //Ensure the capability is not null, as the first call to getDurabilityForDisplay happens before capability injection
            double gasRatio = 0;
            Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
            if (capability.isPresent()) {
                IGasHandler gasHandlerItem = capability.get();
                if (gasHandlerItem.getGasTankCount() > 0) {
                    //Validate something didn't go terribly wrong and we actually do have the tank we expect to have
                    gasRatio = gasHandlerItem.getGasInTank(0).getAmount() / (double) gasHandlerItem.getGasTankCapacity(0);
                }
            }
            FluidStack fluidStack = StorageUtils.getStoredFluidFromNBT(stack);
            double fluidRatio = fluidStack.getAmount() / (double) CAPACITY;
            return 1D - Math.max(gasRatio, fluidRatio);
        }
        return 1;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isShiftKeyDown() && !world.isRemote) {
            Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
            if (capability.isPresent()) {
                IGasHandler gasHandlerItem = capability.get();
                for (int tank = 0; tank < gasHandlerItem.getGasTankCount(); tank++) {
                    gasHandlerItem.setGasInTank(tank, GasStack.EMPTY);
                }
            }
            FluidUtil.getFluidHandler(stack).ifPresent(handler -> handler.drain(CAPACITY, FluidAction.EXECUTE));
            ((ServerPlayerEntity) player).sendContainerToPlayer(player.openContainer);
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        if (CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY == null || Capabilities.GAS_HANDLER_CAPABILITY == null) {
            //Ensure the capability is not null, as the first call to addInformation happens before capability injection
            tooltip.add(MekanismLang.EMPTY.translate());
            return;
        }
        GasStack gasStack = GasStack.EMPTY;
        Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            if (gasHandlerItem.getGasTankCount() > 0) {
                //Validate something didn't go terribly wrong and we actually do have the tank we expect to have
                gasStack = gasHandlerItem.getGasInTank(0);
            }
        }
        FluidStack fluidStack = StorageUtils.getStoredFluidFromNBT(stack);
        if (gasStack.isEmpty() && fluidStack.isEmpty()) {
            tooltip.add(MekanismLang.EMPTY.translate());
        } else if (!gasStack.isEmpty()) {
            tooltip.add(MekanismLang.STORED.translate(gasStack, gasStack.getAmount()));
        } else if (!fluidStack.isEmpty()) {
            tooltip.add(MekanismLang.STORED.translate(fluidStack, fluidStack.getAmount()));
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemCapabilityWrapper(stack, RateLimitFluidHandler.create(() -> TRANSFER_RATE, () -> CAPACITY), RateLimitGasHandler.create(() -> TRANSFER_RATE, () -> CAPACITY));
    }
}