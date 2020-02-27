package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasItem;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockGasTank;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.IItemSustainedInventory;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.security.ISecurityItem;
import mekanism.common.tier.GasTankTier;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.client.Minecraft;
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

public class ItemBlockGasTank extends ItemBlockAdvancedTooltip<BlockGasTank> implements IGasItem, IItemSustainedInventory, ISecurityItem {

    public ItemBlockGasTank(BlockGasTank block) {
        super(block, ItemDeferredRegister.getMekBaseProperties().maxStackSize(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        GasStack gasStack = getGas(stack);
        if (gasStack.isEmpty()) {
            tooltip.add(MekanismLang.EMPTY.translateColored(EnumColor.DARK_RED));
        } else {
            tooltip.add(MekanismLang.GENERIC_STORED.translateColored(EnumColor.ORANGE, gasStack, EnumColor.GRAY,
                  gasStack.getAmount() == Integer.MAX_VALUE ? MekanismLang.INFINITE : gasStack.getAmount()));
        }
        int cap = getTier(stack).getStorage();
        if (cap == Integer.MAX_VALUE) {
            tooltip.add(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, MekanismLang.INFINITE));
        } else {
            tooltip.add(MekanismLang.CAPACITY_MB.translateColored(EnumColor.INDIGO, EnumColor.GRAY, cap));
        }
        super.addInformation(stack, world, tooltip, flag);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addDetails(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(OwnerDisplay.of(Minecraft.getInstance().player, getOwnerUUID(stack)).getTextComponent());
        tooltip.add(MekanismLang.SECURITY.translateColored(EnumColor.GRAY, SecurityUtils.getSecurity(stack, Dist.CLIENT)));
        if (SecurityUtils.isOverridden(stack, Dist.CLIENT)) {
            tooltip.add(MekanismLang.SECURITY_OVERRIDDEN.translateColored(EnumColor.RED));
        }
        tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(hasInventory(stack))));
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

    private GasTankTier getTier(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockGasTank) {
            BlockGasTank gasTank = ((ItemBlockGasTank) item).getBlock();
            return gasTank.getTier();
        }
        return GasTankTier.BASIC;
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
        if (!isInGroup(group)) {
            return;
        }
        BlockGasTank gasTank = getBlock();
        if (gasTank.getTier() == GasTankTier.CREATIVE && MekanismConfig.general.prefilledGasTanks.get()) {
            for (Gas type : MekanismAPI.GAS_REGISTRY.getValues()) {
                if (!type.isHidden()) {
                    ItemStack filled = new ItemStack(this);
                    setGas(filled, new GasStack(type, ((IGasItem) filled.getItem()).getMaxGas(filled)));
                    items.add(filled);
                }
            }
        }
    }

    @Override
    public int getMaxGas(@Nonnull ItemStack stack) {
        return getTier(stack).getStorage();
    }

    @Override
    public int getRate(@Nonnull ItemStack stack) {
        return getTier(stack).getOutput();
    }

    @Override
    public int addGas(@Nonnull ItemStack itemStack, @Nonnull GasStack stack) {
        GasStack gasInItem = getGas(itemStack);
        if (!gasInItem.isEmpty() && !gasInItem.isTypeEqual(stack)) {
            return 0;
        }
        if (getTier(itemStack) == GasTankTier.CREATIVE) {
            setGas(itemStack, new GasStack(stack, Integer.MAX_VALUE));
            return stack.getAmount();
        }
        int toUse = Math.min(getMaxGas(itemStack) - getStored(itemStack), Math.min(getRate(itemStack), stack.getAmount()));
        setGas(itemStack, new GasStack(stack, getStored(itemStack) + toUse));
        return toUse;
    }

    @Nonnull
    @Override
    public GasStack removeGas(@Nonnull ItemStack stack, int amount) {
        if (getGas(stack).isEmpty()) {
            return GasStack.EMPTY;
        }
        Gas type = getGas(stack).getType();
        int gasToUse = Math.min(getStored(stack), Math.min(getRate(stack), amount));
        if (getTier(stack) != GasTankTier.CREATIVE) {
            setGas(stack, new GasStack(type, getStored(stack) - gasToUse));
        }
        return new GasStack(type, gasToUse);
    }

    private int getStored(ItemStack stack) {
        return getGas(stack).getAmount();
    }

    @Override
    public boolean canReceiveGas(@Nonnull ItemStack stack, @Nonnull Gas type) {
        GasStack gasInItem = getGas(stack);
        return gasInItem.isEmpty() || gasInItem.isTypeEqual(type);
    }

    @Override
    public boolean canProvideGas(@Nonnull ItemStack stack, @Nonnull Gas type) {
        GasStack gasInItem = getGas(stack);
        return !gasInItem.isEmpty() && (type.isEmptyType() || gasInItem.isTypeEqual(type));
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return !getGas(stack).isEmpty(); // No bar for empty containers as bars are drawn on top of stack count number
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1D - ((double) getGas(stack).getAmount() / (double) getMaxGas(stack));
    }

    @Override
    public int getRGBDurabilityForDisplay(@Nonnull ItemStack stack) {
        return MathHelper.hsvToRGB(Math.max(0.0F, (float) (1 - getDurabilityForDisplay(stack))) / 3.0F, 1.0F, 1.0F);
    }
}