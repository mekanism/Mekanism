package mekanism.common.item.block.machine.factory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.common.Upgrade;
import mekanism.common.base.IFactory;
import mekanism.common.block.interfaces.ISupportsUpgrades;
import mekanism.common.block.machine.factory.BlockFactory;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.integration.forgeenergy.ForgeEnergyItemWrapper;
import mekanism.common.item.IItemEnergized;
import mekanism.common.item.IItemSustainedInventory;
import mekanism.common.item.ITieredItem;
import mekanism.common.item.block.ItemBlockAdvancedTooltip;
import mekanism.common.security.ISecurityItem;
import mekanism.common.tier.FactoryTier;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockFactory extends ItemBlockAdvancedTooltip implements IItemEnergized, IFactory, IItemSustainedInventory, ISecurityItem, ITieredItem<FactoryTier> {

    public ItemBlockFactory(BlockFactory block) {
        super(block);
        setMaxStackSize(1);
    }

    @Nullable
    @Override
    public FactoryTier getTier(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockFactory) {
            return ((BlockFactory) ((ItemBlockFactory) item).block).getTier();
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addDetails(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list, @Nonnull ITooltipFlag flag) {
        list.add(SecurityUtils.getOwnerDisplay(Minecraft.getMinecraft().player, MekanismClient.clientUUIDMap.get(getOwnerUUID(itemstack))));
        list.add(EnumColor.GREY + LangUtils.localize("gui.security") + ": " + SecurityUtils.getSecurityDisplay(itemstack, Side.CLIENT));
        if (SecurityUtils.isOverridden(itemstack, Side.CLIENT)) {
            list.add(EnumColor.RED + "(" + LangUtils.localize("gui.overridden") + ")");
        }
        RecipeType recipeType = getRecipeTypeOrNull(itemstack);
        if (recipeType != null) {
            list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.recipeType") + ": " + EnumColor.GREY + recipeType.getLocalizedName());
        }
        list.add(EnumColor.BRIGHT_GREEN + LangUtils.localize("tooltip.storedEnergy") + ": " + EnumColor.GREY
                 + MekanismUtils.getEnergyDisplay(getEnergy(itemstack), getMaxEnergy(itemstack)));
        list.add(EnumColor.AQUA + LangUtils.localize("tooltip.inventory") + ": " + EnumColor.GREY +
                 LangUtils.transYesNo(getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
        if (block instanceof ISupportsUpgrades && ItemDataUtils.hasData(itemstack, "upgrades")) {
            Map<Upgrade, Integer> upgrades = Upgrade.buildMap(ItemDataUtils.getDataMap(itemstack));
            for (Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
                list.add(entry.getKey().getColor() + "- " + entry.getKey().getName() + (entry.getKey().canMultiply() ? ": " + EnumColor.GREY + "x" + entry.getValue() : ""));
            }
        }
    }

    @Override
    public int getRecipeType(ItemStack itemStack) {
        if (itemStack.getTagCompound() == null) {
            return 0;
        }
        return itemStack.getTagCompound().getInteger("recipeType");
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
        if (itemStack.getTagCompound() == null) {
            itemStack.setTagCompound(new CompoundNBT());
        }
        itemStack.getTagCompound().setInteger("recipeType", type);
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