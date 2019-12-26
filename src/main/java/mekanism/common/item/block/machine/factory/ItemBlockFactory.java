package mekanism.common.item.block.machine.factory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Upgrade;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.base.IFactory;
import mekanism.common.block.machine.factory.BlockFactory;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.integration.forgeenergy.ForgeEnergyItemWrapper;
import mekanism.common.item.IItemEnergized;
import mekanism.common.item.IItemSustainedInventory;
import mekanism.common.item.ITieredItem;
import mekanism.common.item.block.ItemBlockAdvancedTooltip;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.security.ISecurityItem;
import mekanism.common.tier.FactoryTier;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.OwnerDisplay;
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

public class ItemBlockFactory extends ItemBlockAdvancedTooltip<BlockFactory> implements IItemEnergized, IFactory, IItemSustainedInventory, ISecurityItem, ITieredItem<FactoryTier> {

    public ItemBlockFactory(BlockFactory block) {
        super(block, ItemDeferredRegister.getMekBaseProperties().maxStackSize(1));
    }

    @Nullable
    @Override
    public FactoryTier getTier(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockFactory) {
            return ((ItemBlockFactory) item).getBlock().getTier();
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addDetails(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(OwnerDisplay.of(Minecraft.getInstance().player, getOwnerUUID(stack)).getTextComponent());
        tooltip.add(MekanismLang.SECURITY.translateColored(EnumColor.GRAY, SecurityUtils.getSecurity(stack, Dist.CLIENT)));
        if (SecurityUtils.isOverridden(stack, Dist.CLIENT)) {
            tooltip.add(MekanismLang.SECURITY_OVERRIDDEN.translateColored(EnumColor.RED));
        }
        RecipeType recipeType = getRecipeTypeOrNull(stack);
        if (recipeType != null) {
            tooltip.add(MekanismLang.FACTORY_TYPE.translateColored(EnumColor.INDIGO, EnumColor.GRAY, recipeType));
        }
        tooltip.add(MekanismLang.STORED_ENERGY.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY, EnergyDisplay.of(getEnergy(stack), getMaxEnergy(stack))));
        ListNBT inventory = getInventory(stack);
        tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(inventory != null && !inventory.isEmpty())));
        if (ItemDataUtils.hasData(stack, "upgrades")) {
            Map<Upgrade, Integer> upgrades = Upgrade.buildMap(ItemDataUtils.getDataMap(stack));
            for (Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
                tooltip.add(UpgradeDisplay.of(entry.getKey(), entry.getValue()).getTextComponent());
            }
        }
    }

    @Override
    public int getRecipeType(ItemStack itemStack) {
        if (!itemStack.hasTag()) {
            return 0;
        }
        return itemStack.getTag().getInt("recipeType");
    }

    @Nullable
    @Override
    public RecipeType getRecipeTypeOrNull(ItemStack itemStack) {
        int recipeType = getRecipeType(itemStack);
        if (recipeType < RecipeType.values().length) {
            return RecipeType.values()[recipeType];
        }
        return null;
    }

    @Override
    public void setRecipeType(int type, ItemStack itemStack) {
        if (!itemStack.hasTag()) {
            itemStack.setTag(new CompoundNBT());
        }
        itemStack.getTag().putInt("recipeType", type);
    }

    @Override
    public double getMaxEnergy(ItemStack itemStack) {
        FactoryTier tier = getTier(itemStack);
        if (tier != null) {
            RecipeType recipeType = getRecipeTypeOrNull(itemStack);
            return MekanismUtils.getMaxEnergy(itemStack, tier.processes * (recipeType == null ? 1 : Math.max(0.5D * recipeType.getEnergyStorage(), recipeType.getEnergyUsage())));
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