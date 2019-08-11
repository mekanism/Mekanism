package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.TextComponentUtil;
import mekanism.common.util.TextComponentUtil.Translation;
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
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

public class ItemGaugeDropper extends ItemMekanism implements IGasItem {

    public static final int TRANSFER_RATE = 16;
    public static int CAPACITY = Fluid.BUCKET_VOLUME;

    public ItemGaugeDropper() {
        super("gauge_dropper", new Item.Properties().maxStackSize(1));
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        double gasRatio = (getGas(stack) != null ? (double) getGas(stack).amount : 0D) / (double) CAPACITY;
        LazyOptionalHelper<FluidStack> fluidContained = new LazyOptionalHelper<>(FluidUtil.getFluidContained(stack));
        double fluidRatio = fluidContained.getIfPresentElse(fluid -> (double) fluid.amount, 0D) / (double) CAPACITY;
        return 1D - Math.max(gasRatio, fluidRatio);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking() && !world.isRemote) {
            setGas(stack, null);
            FluidUtil.getFluidHandler(stack).ifPresent(handler -> handler.drain(CAPACITY, true));
            ((ServerPlayerEntity) player).sendContainerToPlayer(player.openContainer);
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        GasStack gasStack = getGas(itemstack);
        LazyOptional<FluidStack> fluidStack = FluidUtil.getFluidContained(itemstack);
        if (gasStack == null && !fluidStack.isPresent()) {
            tooltip.add(TextComponentUtil.build(Translation.of("mekanism.gui.empty"), "."));
        } else if (gasStack != null) {
            tooltip.add(TextComponentUtil.build(Translation.of("mekanism.tooltip.stored"), " ", gasStack, ": " + gasStack.amount));
        } else {
            fluidStack.ifPresent(fluid -> tooltip.add(TextComponentUtil.build(Translation.of("mekanism.tooltip.stored"), " ", fluid, ": " + fluid.amount)));
        }
    }

    @Override
    public int getRate(ItemStack itemstack) {
        return TRANSFER_RATE;
    }

    @Override
    public int addGas(ItemStack itemstack, GasStack stack) {
        if (getGas(itemstack) != null && getGas(itemstack).getGas() != stack.getGas()) {
            return 0;
        }
        int toUse = Math.min(getMaxGas(itemstack) - getStored(itemstack), Math.min(getRate(itemstack), stack.amount));
        setGas(itemstack, new GasStack(stack.getGas(), getStored(itemstack) + toUse));
        return toUse;
    }

    @Override
    public GasStack removeGas(ItemStack itemstack, int amount) {
        if (getGas(itemstack) == null) {
            return null;
        }
        Gas type = getGas(itemstack).getGas();
        int gasToUse = Math.min(getStored(itemstack), Math.min(getRate(itemstack), amount));
        setGas(itemstack, new GasStack(type, getStored(itemstack) - gasToUse));
        return new GasStack(type, gasToUse);
    }

    private int getStored(ItemStack itemstack) {
        return getGas(itemstack) != null ? getGas(itemstack).amount : 0;
    }

    @Override
    public boolean canReceiveGas(ItemStack itemstack, Gas type) {
        return getGas(itemstack) == null || getGas(itemstack).getGas() == type;
    }

    @Override
    public boolean canProvideGas(ItemStack itemstack, Gas type) {
        return getGas(itemstack) != null && (type == null || getGas(itemstack).getGas() == type);
    }

    private GasStack getGas_do(ItemStack itemstack) {
        return GasStack.readFromNBT(ItemDataUtils.getCompound(itemstack, "gasStack"));
    }

    @Override
    public GasStack getGas(ItemStack itemstack) {
        return getGas_do(itemstack);
    }

    @Override
    public void setGas(ItemStack itemstack, GasStack stack) {
        if (stack == null || stack.amount == 0) {
            ItemDataUtils.removeData(itemstack, "gasStack");
        } else {
            int amount = Math.max(0, Math.min(stack.amount, getMaxGas(itemstack)));
            GasStack gasStack = new GasStack(stack.getGas(), amount);
            ItemDataUtils.setCompound(itemstack, "gasStack", gasStack.write(new CompoundNBT()));
        }
    }

    @Override
    public int getMaxGas(ItemStack itemstack) {
        return CAPACITY;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new FluidHandlerItemStack(stack, CAPACITY);
    }
}