package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.client.render.item.block.RenderEnergyCubeItem;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.integration.forgeenergy.ForgeEnergyItemWrapper;
import mekanism.common.item.IItemEnergized;
import mekanism.common.item.IItemSustainedInventory;
import mekanism.common.item.ITieredItem;
import mekanism.common.security.ISecurityItem;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.OwnerDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemBlockEnergyCube extends ItemBlockTooltip<BlockEnergyCube> implements IItemEnergized, IItemSustainedInventory, ISecurityItem, ITieredItem<EnergyCubeTier> {

    public ItemBlockEnergyCube(BlockEnergyCube block) {
        //TODO: Figure out if this and the other TEISR's cause issues server side due to the fact this technically will also be called server side
        // If there is an issue, the easiest way to fix it would be to basically have a proxy class getting the callable of the supplier
        // Also test () -> () -> new RenderEnergyCubeItem(), because that may not evaluate as early
        super(block, new Item.Properties().maxStackSize(1).setNoRepair().setTEISR(() -> RenderEnergyCubeItem::new));
    }

    @Nullable
    @Override
    public EnergyCubeTier getTier(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockEnergyCube) {
            return ((ItemBlockEnergyCube) item).getBlock().getTier();
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack itemstack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(TextComponentUtil.build(EnumColor.BRIGHT_GREEN, Translation.of("tooltip.mekanism.stored_energy"), ": ", EnumColor.GRAY,
              EnergyDisplay.of(getEnergy(itemstack))));
        EnergyCubeTier tier = getTier(itemstack);
        if (tier != null) {
            tooltip.add(TextComponentUtil.build(EnumColor.INDIGO, Translation.of("tooltip.mekanism.capacity"), ": ", EnumColor.GRAY,
                  EnergyDisplay.of(tier.getMaxEnergy())));
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

    @Override
    public void setEnergy(ItemStack itemStack, double amount) {
        if (getTier(itemStack) == EnergyCubeTier.CREATIVE && amount != Double.MAX_VALUE) {
            return;
        }
        ItemDataUtils.setDouble(itemStack, "energyStored", Math.max(Math.min(amount, getMaxEnergy(itemStack)), 0));
    }

    @Override
    public double getMaxEnergy(ItemStack itemStack) {
        EnergyCubeTier tier = getTier(itemStack);
        return tier == null ? 0 : tier.getMaxEnergy();
    }

    @Override
    public double getMaxTransfer(ItemStack itemStack) {
        return getMaxEnergy(itemStack) * 0.005;
    }

    @Override
    public boolean canReceive(ItemStack itemStack) {
        return true;
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return true;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1D - (getEnergy(stack) / getMaxEnergy(stack));
    }

    @Override
    public int getRGBDurabilityForDisplay(@Nonnull ItemStack stack) {
        return MathHelper.hsvToRGB(Math.max(0.0F, (float) (1 - getDurabilityForDisplay(stack))) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemCapabilityWrapper(stack, new ForgeEnergyItemWrapper());
    }
}