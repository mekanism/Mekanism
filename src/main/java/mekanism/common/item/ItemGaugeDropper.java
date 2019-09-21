package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
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

public class ItemGaugeDropper extends ItemMekanism implements IGasItem {

    public static final int TRANSFER_RATE = 16;
    public static int CAPACITY = FluidAttributes.BUCKET_VOLUME;

    public ItemGaugeDropper() {
        super("gauge_dropper", new Item.Properties().maxStackSize(1));
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
        if (player.isSneaking() && !world.isRemote) {
            setGas(stack, null);
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
            tooltip.add(TextComponentUtil.build(Translation.of("gui.mekanism.empty"), "."));
        } else if (!gasStack.isEmpty()) {
            tooltip.add(TextComponentUtil.build(Translation.of("tooltip.mekanism.stored"), " ", gasStack, ": " + gasStack.getAmount()));
        } else if (!fluidStack.isEmpty()) {
            tooltip.add(TextComponentUtil.build(Translation.of("tooltip.mekanism.stored"), " ", fluidStack, ": " + fluidStack.getAmount()));
        }
    }

    @Override
    public int getRate(@Nonnull ItemStack itemstack) {
        return TRANSFER_RATE;
    }

    @Override
    public int addGas(@Nonnull ItemStack itemstack, @Nonnull GasStack stack) {
        if (getGas(itemstack) != null && getGas(itemstack).getGas() != stack.getGas()) {
            return 0;
        }
        int toUse = Math.min(getMaxGas(itemstack) - getStored(itemstack), Math.min(getRate(itemstack), stack.getAmount()));
        setGas(itemstack, new GasStack(stack.getGas(), getStored(itemstack) + toUse));
        return toUse;
    }

    @Nonnull
    @Override
    public GasStack removeGas(@Nonnull ItemStack itemstack, int amount) {
        if (getGas(itemstack) == null) {
            return null;
        }
        Gas type = getGas(itemstack).getGas();
        int gasToUse = Math.min(getStored(itemstack), Math.min(getRate(itemstack), amount));
        setGas(itemstack, new GasStack(type, getStored(itemstack) - gasToUse));
        return new GasStack(type, gasToUse);
    }

    private int getStored(ItemStack itemstack) {
        return getGas(itemstack).getAmount();
    }

    @Override
    public boolean canReceiveGas(@Nonnull ItemStack itemstack, @Nonnull Gas type) {
        return getGas(itemstack).isEmpty() || getGas(itemstack).getGas() == type;
    }

    @Override
    public boolean canProvideGas(@Nonnull ItemStack itemstack, @Nonnull Gas type) {
        return getGas(itemstack) != null && (type == null || getGas(itemstack).getGas() == type);
    }

    private GasStack getGas_do(ItemStack itemstack) {
        return GasStack.readFromNBT(ItemDataUtils.getCompound(itemstack, "gasStack"));
    }

    @Nonnull
    @Override
    public GasStack getGas(@Nonnull ItemStack itemstack) {
        return getGas_do(itemstack);
    }

    @Override
    public void setGas(@Nonnull ItemStack itemstack, @Nonnull GasStack stack) {
        if (stack.isEmpty()) {
            ItemDataUtils.removeData(itemstack, "gasStack");
        } else {
            int amount = Math.max(0, Math.min(stack.getAmount(), getMaxGas(itemstack)));
            GasStack gasStack = new GasStack(stack.getGas(), amount);
            ItemDataUtils.setCompound(itemstack, "gasStack", gasStack.write(new CompoundNBT()));
        }
    }

    @Override
    public int getMaxGas(@Nonnull ItemStack itemstack) {
        return CAPACITY;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new FluidHandlerItemStack(stack, CAPACITY);
    }
}