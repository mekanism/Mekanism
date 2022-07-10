package mekanism.common.item.block;

import java.util.List;
import java.util.function.BooleanSupplier;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.chemical.item.ChemicalTankContentsHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.TextUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

public class ItemBlockChemicalTank extends ItemBlockTooltip<BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>> implements IItemSustainedInventory {

    public ItemBlockChemicalTank(BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>> block) {
        super(block);
    }

    @Override
    public ChemicalTankTier getTier() {
        return Attribute.getTier(getBlock(), ChemicalTankTier.class);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        ChemicalTankTier tier = getTier();
        StorageUtils.addStoredSubstance(stack, tooltip, tier == ChemicalTankTier.CREATIVE);
        if (tier == ChemicalTankTier.CREATIVE) {
            tooltip.add(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, MekanismLang.INFINITE));
        } else {
            tooltip.add(MekanismLang.CAPACITY_MB.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(tier.getStorage())));
        }
        super.appendHoverText(stack, world, tooltip, flag);
    }

    @Override
    public void fillItemCategory(@NotNull CreativeModeTab group, @NotNull NonNullList<ItemStack> items) {
        super.fillItemCategory(group, items);
        if (allowedIn(group)) {
            ChemicalTankTier tier = Attribute.getTier(getBlock(), ChemicalTankTier.class);
            if (tier == ChemicalTankTier.CREATIVE) {
                long capacity = tier.getStorage();
                fillItemGroup(MekanismConfig.general.prefilledGasTanks, MekanismAPI.gasRegistry(), items, capacity);
                fillItemGroup(MekanismConfig.general.prefilledInfusionTanks, MekanismAPI.infuseTypeRegistry(), items, capacity);
                fillItemGroup(MekanismConfig.general.prefilledPigmentTanks, MekanismAPI.pigmentRegistry(), items, capacity);
                fillItemGroup(MekanismConfig.general.prefilledSlurryTanks, MekanismAPI.slurryRegistry(), items, capacity);
            }
        }
    }

    private <CHEMICAL extends Chemical<CHEMICAL>> void fillItemGroup(BooleanSupplier shouldAdd, IForgeRegistry<CHEMICAL> registry, @NotNull NonNullList<ItemStack> items,
          long capacity) {
        if (shouldAdd.getAsBoolean()) {
            for (CHEMICAL type : registry.getValues()) {
                if (!type.isHidden()) {
                    items.add(ChemicalUtil.getFilledVariant(new ItemStack(this), capacity, type));
                }
            }
        }
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        // No bar for empty containers as bars are drawn on top of stack count number
        return ChemicalUtil.hasGas(stack) ||
               ChemicalUtil.hasChemical(stack, ConstantPredicates.alwaysTrue(), Capabilities.INFUSION_HANDLER) ||
               ChemicalUtil.hasChemical(stack, ConstantPredicates.alwaysTrue(), Capabilities.PIGMENT_HANDLER) ||
               ChemicalUtil.hasChemical(stack, ConstantPredicates.alwaysTrue(), Capabilities.SLURRY_HANDLER);
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return ChemicalUtil.getRGBDurabilityForDisplay(stack);
    }

    @Override
    protected void gatherCapabilities(List<ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
        super.gatherCapabilities(capabilities, stack, nbt);
        capabilities.add(ChemicalTankContentsHandler.create(Attribute.getTier(getBlock(), ChemicalTankTier.class)));
    }
}