package mekanism.common.item.gear;

import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IIncrementalEnum;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.client.render.item.gear.RenderFlameThrower;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
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

public class ItemFlamethrower extends Item implements IGasItem {

    public int TRANSFER_RATE = 16;

    public ItemFlamethrower(Properties properties) {
        super(properties.maxStackSize(1).setNoRepair().setTEISR(() -> getTEISR()));
    }

    @OnlyIn(Dist.CLIENT)
    private static Callable<ItemStackTileEntityRenderer> getTEISR() {
        //NOTE: This extra method is needed to avoid classloading issues on servers
        return RenderFlameThrower::new;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        GasStack gasStack = getGas(stack);
        if (gasStack.isEmpty()) {
            tooltip.add(MekanismLang.NO_GAS.translate());
        } else {
            tooltip.add(MekanismLang.STORED.translate(gasStack, gasStack.getAmount()));
        }
        tooltip.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, getMode(stack)));
    }

    public void useGas(ItemStack stack) {
        GasStack gas = getGas(stack);
        if (!gas.isEmpty()) {
            setGas(stack, new GasStack(gas, gas.getAmount() - 1));
        }
    }

    @Override
    public int getMaxGas(@Nonnull ItemStack stack) {
        return MekanismConfig.general.maxFlamethrowerGas.get();
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
        if (stack.getType() != MekanismGases.HYDROGEN.getGas()) {
            return 0;
        }
        int toUse = Math.min(getMaxGas(itemStack) - getStored(itemStack), Math.min(getRate(itemStack), stack.getAmount()));
        setGas(itemStack, new GasStack(stack, getStored(itemStack) + toUse));
        return toUse;
    }

    @Nonnull
    @Override
    public GasStack removeGas(@Nonnull ItemStack stack, int amount) {
        return GasStack.EMPTY;
    }

    public int getStored(ItemStack stack) {
        return getGas(stack).getAmount();
    }

    @Override
    public boolean canReceiveGas(@Nonnull ItemStack stack, @Nonnull Gas type) {
        return type == MekanismGases.HYDROGEN.getGas();
    }

    @Override
    public boolean canProvideGas(@Nonnull ItemStack stack, @Nonnull Gas type) {
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
    public GasStack getGas(@Nonnull ItemStack stack) {
        return GasStack.readFromNBT(ItemDataUtils.getCompound(stack, "stored"));
    }

    @Override
    public void setGas(@Nonnull ItemStack itemStack, @Nonnull GasStack stack) {
        if (stack.isEmpty()) {
            ItemDataUtils.removeData(itemStack, "stored");
        } else {
            int amount = Math.max(0, Math.min(stack.getAmount(), getMaxGas(itemStack)));
            GasStack gasStack = new GasStack(stack, amount);
            ItemDataUtils.setCompound(itemStack, "stored", gasStack.write(new CompoundNBT()));
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
        setMode(stack, getMode(stack).getNext());
    }

    public FlamethrowerMode getMode(ItemStack stack) {
        return FlamethrowerMode.byIndexStatic(ItemDataUtils.getInt(stack, "mode"));
    }

    public void setMode(ItemStack stack, FlamethrowerMode mode) {
        ItemDataUtils.setInt(stack, "mode", mode.ordinal());
    }

    public enum FlamethrowerMode implements IIncrementalEnum<FlamethrowerMode>, IHasTextComponent {
        COMBAT(MekanismLang.FLAMETHROWER_COMBAT, EnumColor.YELLOW),
        HEAT(MekanismLang.FLAMETHROWER_HEAT, EnumColor.ORANGE),
        INFERNO(MekanismLang.FLAMETHROWER_INFERNO, EnumColor.DARK_RED);

        private static final FlamethrowerMode[] MODES = values();
        private final ILangEntry langEntry;
        private final EnumColor color;

        FlamethrowerMode(ILangEntry langEntry, EnumColor color) {
            this.langEntry = langEntry;
            this.color = color;
        }

        @Override
        public ITextComponent getTextComponent() {
            return langEntry.translateColored(color);
        }

        @Nonnull
        @Override
        public FlamethrowerMode byIndex(int index) {
            return byIndexStatic(index);
        }

        public static FlamethrowerMode byIndexStatic(int index) {
            //TODO: Is it more efficient to check if index is negative and then just do the normal mod way?
            return MODES[Math.floorMod(index, MODES.length)];
        }
    }
}