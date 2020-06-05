package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.chemical.item.RateLimitGasHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.util.GasUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemBlockGasTank extends ItemBlockTooltip<BlockTileModel<TileEntityGasTank, Machine<TileEntityGasTank>>> implements IItemSustainedInventory, ISecurityItem {

    public ItemBlockGasTank(BlockTileModel<TileEntityGasTank, Machine<TileEntityGasTank>> block) {
        super(block, true, ItemDeferredRegister.getMekBaseProperties().maxStackSize(1));
    }

    @Override
    public ChemicalTankTier getTier() {
        return Attribute.getTier(getBlock(), ChemicalTankTier.class);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        ChemicalTankTier tier = getTier();
        StorageUtils.addStoredGas(stack, tooltip, true, true, MekanismLang.EMPTY, stored -> {
            if (stored.isEmpty()) {
                return MekanismLang.EMPTY.translate();
            } else if (tier == ChemicalTankTier.CREATIVE) {
                return MekanismLang.GENERIC_STORED.translateColored(EnumColor.ORANGE, stored, EnumColor.GRAY, MekanismLang.INFINITE);
            }
            return MekanismLang.GENERIC_STORED.translateColored(EnumColor.ORANGE, stored, EnumColor.GRAY, stored.getAmount());
        });
        if (tier == ChemicalTankTier.CREATIVE) {
            tooltip.add(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, MekanismLang.INFINITE));
        } else {
            tooltip.add(MekanismLang.CAPACITY_MB.translateColored(EnumColor.INDIGO, EnumColor.GRAY, tier.getStorage()));
        }
        super.addInformation(stack, world, tooltip, flag);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addDetails(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, boolean advanced) {
        tooltip.add(OwnerDisplay.of(Minecraft.getInstance().player, getOwnerUUID(stack)).getTextComponent());
        tooltip.add(MekanismLang.SECURITY.translateColored(EnumColor.GRAY, SecurityUtils.getSecurity(stack, Dist.CLIENT)));
        if (SecurityUtils.isOverridden(stack, Dist.CLIENT)) {
            tooltip.add(MekanismLang.SECURITY_OVERRIDDEN.translateColored(EnumColor.RED));
        }
        tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(hasInventory(stack))));
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
        if (isInGroup(group)) {
            ChemicalTankTier tier = Attribute.getTier(getBlock(), ChemicalTankTier.class);
            if (tier == ChemicalTankTier.CREATIVE && MekanismConfig.general.prefilledGasTanks.get()) {
                for (Gas type : MekanismAPI.GAS_REGISTRY.getValues()) {
                    if (!type.isHidden()) {
                        items.add(GasUtils.getFilledVariant(new ItemStack(this), tier.getStorage(), type));
                    }
                }
            }
        }
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return GasUtils.hasGas(stack); // No bar for empty containers as bars are drawn on top of stack count number
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return StorageUtils.getDurabilityForDisplay(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        GasStack stored = StorageUtils.getStoredGasFromNBT(stack);
        return stored.isEmpty() ? 0 : stored.getChemicalTint();
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemCapabilityWrapper(stack, RateLimitGasHandler.create(Attribute.getTier(getBlock(), ChemicalTankTier.class)));
    }
}