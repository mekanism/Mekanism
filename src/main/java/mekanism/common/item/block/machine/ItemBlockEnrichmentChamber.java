package mekanism.common.item.block.machine;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import mekanism.api.Upgrade;
import mekanism.api.text.EnumColor;
import mekanism.common.block.machine.BlockEnrichmentChamber;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.integration.forgeenergy.ForgeEnergyItemWrapper;
import mekanism.common.item.IItemEnergized;
import mekanism.common.item.IItemSustainedInventory;
import mekanism.common.item.block.ItemBlockAdvancedTooltip;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.security.ISecurityItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.OwnerDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mekanism.common.util.text.UpgradeDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemBlockEnrichmentChamber extends ItemBlockAdvancedTooltip<BlockEnrichmentChamber> implements IItemEnergized, IItemSustainedInventory, ISecurityItem {

    public ItemBlockEnrichmentChamber(BlockEnrichmentChamber block) {
        super(block, ItemDeferredRegister.getMekBaseProperties().maxStackSize(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addDetails(@Nonnull ItemStack itemstack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(OwnerDisplay.of(Minecraft.getInstance().player, getOwnerUUID(itemstack)).getTextComponent());
        tooltip.add(TextComponentUtil.build(EnumColor.GRAY, Translation.of("gui.mekanism.security"), ": ", SecurityUtils.getSecurity(itemstack, Dist.CLIENT)));
        if (SecurityUtils.isOverridden(itemstack, Dist.CLIENT)) {
            tooltip.add(TextComponentUtil.build(EnumColor.RED, "(", Translation.of("gui.mekanism.overridden"), ")"));
        }
        tooltip.add(TextComponentUtil.build(EnumColor.BRIGHT_GREEN, Translation.of("tooltip.mekanism.stored_energy"), ": ", EnumColor.GRAY,
              EnergyDisplay.of(getEnergy(itemstack), getMaxEnergy(itemstack))));
        ListNBT inventory = getInventory(itemstack);
        tooltip.add(TextComponentUtil.build(EnumColor.AQUA, Translation.of("tooltip.mekanism.inventory"), ": ", EnumColor.GRAY,
              YesNo.of(inventory != null && !inventory.isEmpty())));
        if (ItemDataUtils.hasData(itemstack, "upgrades")) {
            Map<Upgrade, Integer> upgrades = Upgrade.buildMap(ItemDataUtils.getDataMap(itemstack));
            for (Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
                tooltip.add(UpgradeDisplay.of(entry.getKey(), entry.getValue()).getTextComponent());
            }
        }
    }

    @Override
    public double getMaxEnergy(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item instanceof ItemBlockEnrichmentChamber) {
            return MekanismUtils.getMaxEnergy(itemStack, ((ItemBlockEnrichmentChamber) item).getBlock().getStorage());
        }
        return 0;
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
        return false;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemCapabilityWrapper(stack, new ForgeEnergyItemWrapper());
    }
}