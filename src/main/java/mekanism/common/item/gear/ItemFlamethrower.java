package mekanism.common.item.gear;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.client.render.item.gear.RenderFlameThrower;
import mekanism.common.MekanismGases;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.ItemMekanism;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
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

public class ItemFlamethrower extends ItemMekanism implements IGasItem {

    public int TRANSFER_RATE = 16;

    public ItemFlamethrower() {
        super("flamethrower", new Item.Properties().maxStackSize(1).setNoRepair().setTEISR(() -> RenderFlameThrower::new));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        GasStack gasStack = getGas(stack);
        if (gasStack.isEmpty()) {
            tooltip.add(TextComponentUtil.build(Translation.of("tooltip.mekanism.noGas"), "."));
        } else {
            tooltip.add(TextComponentUtil.build(Translation.of("tooltip.mekanism.stored"), " ", gasStack, ": " + gasStack.getAmount()));
        }
        tooltip.add(TextComponentUtil.build(EnumColor.GRAY, Translation.of("tooltip.mekanism.mode"), ": ", EnumColor.GRAY, getMode(stack)));
    }

    public void useGas(ItemStack stack) {
        GasStack gas = getGas(stack);
        if (!gas.isEmpty()) {
            setGas(stack, new GasStack(gas, gas.getAmount() - 1));
        }
    }

    @Override
    public int getMaxGas(@Nonnull ItemStack itemstack) {
        return MekanismConfig.general.maxFlamethrowerGas.get();
    }

    @Override
    public int getRate(@Nonnull ItemStack itemstack) {
        return TRANSFER_RATE;
    }

    @Override
    public int addGas(@Nonnull ItemStack itemstack, @Nonnull GasStack stack) {
        GasStack gasInItem = getGas(itemstack);
        if (!gasInItem.isEmpty() && gasInItem.getGas() != stack.getGas()) {
            return 0;
        }
        if (!stack.getGas().isIn(MekanismTags.HYDROGEN)) {
            return 0;
        }
        int toUse = Math.min(getMaxGas(itemstack) - getStored(itemstack), Math.min(getRate(itemstack), stack.getAmount()));
        setGas(itemstack, new GasStack(stack, getStored(itemstack) + toUse));
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
        return type.isIn(MekanismTags.HYDROGEN);
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
        setGas(filled, MekanismGases.HYDROGEN.getGasStack(((IGasItem) filled.getItem()).getMaxGas(filled)));
        items.add(filled);
    }

    public void incrementMode(ItemStack stack) {
        setMode(stack, getMode(stack).increment());
    }

    public FlamethrowerMode getMode(ItemStack stack) {
        return FlamethrowerMode.values()[ItemDataUtils.getInt(stack, "mode")];
    }

    public void setMode(ItemStack stack, FlamethrowerMode mode) {
        ItemDataUtils.setInt(stack, "mode", mode.ordinal());
    }

    public enum FlamethrowerMode implements IHasTextComponent {
        COMBAT("tooltip.flamethrower.combat", EnumColor.YELLOW),
        HEAT("tooltip.flamethrower.heat", EnumColor.ORANGE),
        INFERNO("tooltip.flamethrower.inferno", EnumColor.DARK_RED);

        private String unlocalized;
        private EnumColor color;

        FlamethrowerMode(String s, EnumColor c) {
            unlocalized = s;
            color = c;
        }

        public FlamethrowerMode increment() {
            return ordinal() < values().length - 1 ? values()[ordinal() + 1] : values()[0];
        }

        @Override
        public ITextComponent getTextComponent() {
            return TextComponentUtil.build(color, unlocalized);
        }
    }
}