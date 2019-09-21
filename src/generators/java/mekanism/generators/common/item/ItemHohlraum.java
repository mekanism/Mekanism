package mekanism.generators.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismGases;
import mekanism.common.item.ItemMekanism;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemHohlraum extends ItemMekanism implements IGasItem {

    public static final int MAX_GAS = 10;
    public static final int TRANSFER_RATE = 1;

    public ItemHohlraum() {
        super(MekanismGenerators.MODID, "hohlraum", new Item.Properties().maxStackSize(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        GasStack gasStack = getGas(stack);
        if (gasStack.isEmpty()) {
            tooltip.add(TextComponentUtil.build(Translation.of("tooltip.mekanism.noGas"), "."));
            tooltip.add(TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("tooltip.insufficientFuel")));
        } else {
            tooltip.add(TextComponentUtil.build(Translation.of("tooltip.stored"), " ", gasStack, ": " + gasStack.getAmount()));
            if (gasStack.getAmount() == getMaxGas(stack)) {
                tooltip.add(TextComponentUtil.build(EnumColor.DARK_GREEN, Translation.of("tooltip.readyForReaction"), "!"));
            } else {
                tooltip.add(TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("tooltip.insufficientFuel")));
            }
        }
    }

    @Override
    public int getMaxGas(@Nonnull ItemStack itemstack) {
        return MAX_GAS;
    }

    @Override
    public int getRate(@Nonnull ItemStack itemstack) {
        return TRANSFER_RATE;
    }

    @Override
    public int addGas(@Nonnull ItemStack itemstack, @Nonnull GasStack stack) {
        if (!getGas(itemstack).isEmpty() && getGas(itemstack).getGas() != stack.getGas()) {
            return 0;
        }
        if (!stack.getGas().isIn(MekanismTags.FUSION_FUEL)) {
            return 0;
        }
        int toUse = Math.min(getMaxGas(itemstack) - getStored(itemstack), Math.min(getRate(itemstack), stack.getAmount()));
        setGas(itemstack, new GasStack(stack.getGas(), getStored(itemstack) + toUse));
        return toUse;
    }

    @Nonnull
    @Override
    public GasStack removeGas(@Nonnull ItemStack itemstack, int amount) {
        return GasStack.EMPTY;
    }

    public int getStored(ItemStack itemstack) {
        return getGas(itemstack).getAmount();
    }

    @Override
    public boolean canReceiveGas(@Nonnull ItemStack itemstack, @Nonnull Gas type) {
        return type.isIn(MekanismTags.FUSION_FUEL);
    }

    @Override
    public boolean canProvideGas(@Nonnull ItemStack itemstack, @Nonnull Gas type) {
        return false;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1D - ((double) getGas(stack).getAmount() / (double) getMaxGas(stack));
    }

    @Override
    public int getRGBDurabilityForDisplay(@Nonnull ItemStack stack) {
        return MathHelper.hsvToRGB(Math.max(0.0F, (float) (1 - getDurabilityForDisplay(stack))) / 3.0F, 1.0F, 1.0F);
    }

    @Nonnull
    @Override
    public GasStack getGas(@Nonnull ItemStack itemstack) {
        return GasStack.readFromNBT(ItemDataUtils.getCompound(itemstack, "stored"));
    }

    @Override
    public void setGas(@Nonnull ItemStack itemstack, @Nonnull GasStack stack) {
        if (stack.isEmpty()) {
            ItemDataUtils.removeData(itemstack, "stored");
        } else {
            int amount = Math.max(0, Math.min(stack.getAmount(), getMaxGas(itemstack)));
            GasStack gasStack = new GasStack(stack, amount);
            ItemDataUtils.setCompound(itemstack, "stored", gasStack.write(new CompoundNBT()));
        }
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
        if (!isInGroup(group)) {
            return;
        }
        ItemStack filled = new ItemStack(this);
        setGas(filled, new GasStack(MekanismGases.FUSION_FUEL, ((IGasItem) filled.getItem()).getMaxGas(filled)));
        items.add(filled);
    }
}