package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.common.MekanismLang;
import mekanism.common.util.ItemDataUtils;
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
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

public class ItemGaugeDropper extends Item implements IGasItem {

    public static final int TRANSFER_RATE = 16;
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
        double gasRatio = (double) getGas(stack).getAmount() / (double) CAPACITY;
        //TODO: Better way of doing this?
        FluidStack fluidStack = CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY == null ? FluidStack.EMPTY : FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
        double fluidRatio = (double) fluidStack.getAmount() / (double) CAPACITY;
        return 1D - Math.max(gasRatio, fluidRatio);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.func_225608_bj_() && !world.isRemote) {
            setGas(stack, GasStack.EMPTY);
            FluidUtil.getFluidHandler(stack).ifPresent(handler -> handler.drain(CAPACITY, FluidAction.EXECUTE));
            ((ServerPlayerEntity) player).sendContainerToPlayer(player.openContainer);
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        GasStack gasStack = getGas(stack);
        //TODO: Better way of doing this?
        //TODO: Better way of doing this?
        FluidStack fluidStack = CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY == null ? FluidStack.EMPTY : FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
        if (gasStack.isEmpty() && fluidStack.isEmpty()) {
            tooltip.add(MekanismLang.EMPTY.translate());
        } else if (!gasStack.isEmpty()) {
            tooltip.add(MekanismLang.STORED.translate(gasStack, gasStack.getAmount()));
        } else if (!fluidStack.isEmpty()) {
            tooltip.add(MekanismLang.STORED.translate(fluidStack, fluidStack.getAmount()));
        }
    }

    @Override
    public int getRate(@Nonnull ItemStack stack) {
        return TRANSFER_RATE;
    }

    @Override
    public int addGas(@Nonnull ItemStack itemStack, @Nonnull GasStack stack) {
        GasStack gasInItem = getGas(itemStack);
        if (!gasInItem.isEmpty() && !gasInItem.isTypeEqual(stack)) {
            return 0;
        }
        int toUse = Math.min(getMaxGas(itemStack) - getStored(itemStack), Math.min(getRate(itemStack), stack.getAmount()));
        setGas(itemStack, new GasStack(stack, getStored(itemStack) + toUse));
        return toUse;
    }

    @Nonnull
    @Override
    public GasStack removeGas(@Nonnull ItemStack stack, int amount) {
        GasStack gasInItem = getGas(stack);
        if (gasInItem.isEmpty()) {
            return GasStack.EMPTY;
        }
        Gas type = gasInItem.getType();
        int gasToUse = Math.min(getStored(stack), Math.min(getRate(stack), amount));
        setGas(stack, new GasStack(type, getStored(stack) - gasToUse));
        return new GasStack(type, gasToUse);
    }

    private int getStored(ItemStack stack) {
        return getGas(stack).getAmount();
    }

    @Override
    public boolean canReceiveGas(@Nonnull ItemStack stack, @Nonnull Gas type) {
        GasStack gasInItem = getGas(stack);
        return gasInItem.isEmpty() || gasInItem.getType().equals(type);
    }

    @Override
    public boolean canProvideGas(@Nonnull ItemStack stack, @Nonnull Gas type) {
        GasStack gasInItem = getGas(stack);
        return !gasInItem.isEmpty() && (type.isEmptyType() || gasInItem.isTypeEqual(type));
    }

    private GasStack getGas_do(ItemStack stack) {
        return GasStack.readFromNBT(ItemDataUtils.getCompound(stack, "gasStack"));
    }

    @Nonnull
    @Override
    public GasStack getGas(@Nonnull ItemStack stack) {
        return getGas_do(stack);
    }

    @Override
    public void setGas(@Nonnull ItemStack itemStack, @Nonnull GasStack stack) {
        if (stack.isEmpty()) {
            ItemDataUtils.removeData(itemStack, "gasStack");
        } else {
            int amount = Math.max(0, Math.min(stack.getAmount(), getMaxGas(itemStack)));
            GasStack gasStack = new GasStack(stack, amount);
            ItemDataUtils.setCompound(itemStack, "gasStack", gasStack.write(new CompoundNBT()));
        }
    }

    @Override
    public int getMaxGas(@Nonnull ItemStack stack) {
        return CAPACITY;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new FluidHandlerItemStack(stack, CAPACITY);
    }
}