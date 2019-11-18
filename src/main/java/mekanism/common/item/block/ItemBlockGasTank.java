package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.api.text.EnumColor;
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
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBlockGasTank extends ItemBlockTooltip<BlockGasTank> implements IGasItem, IItemSustainedInventory, ISecurityItem {

    /**
     * The maximum amount of gas this tank can hold.
     */
    public int MAX_GAS = 96000;

    public ItemBlockGasTank(BlockGasTank block) {
        super(block, ItemDeferredRegister.getMekBaseProperties().maxStackSize(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack itemstack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        GasStack gasStack = getGas(itemstack);
        if (gasStack.isEmpty()) {
            tooltip.add(TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.mekanism.empty"), "."));
        } else if (gasStack.getAmount() == Integer.MAX_VALUE) {
            tooltip.add(TextComponentUtil.build(EnumColor.ORANGE, gasStack, ": ", EnumColor.GRAY, Translation.of("gui.mekanism.infinite")));
        } else {
            tooltip.add(TextComponentUtil.build(EnumColor.ORANGE, gasStack, ": ", EnumColor.GRAY, gasStack.getAmount()));
        }
        int cap = getTier(itemstack).getStorage();
        if (cap == Integer.MAX_VALUE) {
            tooltip.add(TextComponentUtil.build(EnumColor.INDIGO, Translation.of("tooltip.mekanism.capacity"), ": ", EnumColor.GRAY,
                  Translation.of("gui.mekanism.infinite")));
        } else {
            tooltip.add(TextComponentUtil.build(EnumColor.INDIGO, Translation.of("tooltip.mekanism.capacity"), ": ", EnumColor.GRAY, cap));
        }
        super.addInformation(itemstack, world, tooltip, flag);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addDescription(@Nonnull ItemStack itemstack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(OwnerDisplay.of(Minecraft.getInstance().player, getOwnerUUID(itemstack)).getTextComponent());
        tooltip.add(TextComponentUtil.build(EnumColor.GRAY, Translation.of("gui.mekanism.security"), ": ", SecurityUtils.getSecurity(itemstack, Dist.CLIENT)));
        if (SecurityUtils.isOverridden(itemstack, Dist.CLIENT)) {
            tooltip.add(TextComponentUtil.build(EnumColor.RED, "(", Translation.of("gui.mekanism.overridden"), ")"));
        }
        ListNBT inventory = getInventory(itemstack);
        tooltip.add(TextComponentUtil.build(EnumColor.AQUA, Translation.of("tooltip.mekanism.inventory"), ": ", EnumColor.GRAY,
              YesNo.of(inventory != null && !inventory.isEmpty())));
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
                if (type.isVisible()) {
                    ItemStack filled = new ItemStack(this);
                    setGas(filled, new GasStack(type, ((IGasItem) filled.getItem()).getMaxGas(filled)));
                    items.add(filled);
                }
            }
        }
    }

    @Override
    public int getMaxGas(@Nonnull ItemStack itemstack) {
        return getTier(itemstack).getStorage();
    }

    @Override
    public int getRate(@Nonnull ItemStack itemstack) {
        return getTier(itemstack).getOutput();
    }

    @Override
    public int addGas(@Nonnull ItemStack itemstack, @Nonnull GasStack stack) {
        GasStack gasInItem = getGas(itemstack);
        if (!gasInItem.isEmpty() && !gasInItem.isTypeEqual(stack)) {
            return 0;
        }
        if (getTier(itemstack) == GasTankTier.CREATIVE) {
            setGas(itemstack, new GasStack(stack, Integer.MAX_VALUE));
            return stack.getAmount();
        }
        int toUse = Math.min(getMaxGas(itemstack) - getStored(itemstack), Math.min(getRate(itemstack), stack.getAmount()));
        setGas(itemstack, new GasStack(stack, getStored(itemstack) + toUse));
        return toUse;
    }

    @Nonnull
    @Override
    public GasStack removeGas(@Nonnull ItemStack itemstack, int amount) {
        if (getGas(itemstack).isEmpty()) {
            return GasStack.EMPTY;
        }
        Gas type = getGas(itemstack).getType();
        int gasToUse = Math.min(getStored(itemstack), Math.min(getRate(itemstack), amount));
        if (getTier(itemstack) != GasTankTier.CREATIVE) {
            setGas(itemstack, new GasStack(type, getStored(itemstack) - gasToUse));
        }
        return new GasStack(type, gasToUse);
    }

    private int getStored(ItemStack itemstack) {
        return getGas(itemstack).getAmount();
    }

    @Override
    public boolean canReceiveGas(@Nonnull ItemStack itemstack, @Nonnull Gas type) {
        GasStack gasInItem = getGas(itemstack);
        return gasInItem.isEmpty() || gasInItem.isTypeEqual(type);
    }

    @Override
    public boolean canProvideGas(@Nonnull ItemStack itemstack, @Nonnull Gas type) {
        GasStack gasInItem = getGas(itemstack);
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