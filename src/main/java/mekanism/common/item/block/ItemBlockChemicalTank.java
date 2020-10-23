package mekanism.common.item.block;

import java.util.List;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.chemical.item.ChemicalTankContentsHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.TextUtils;
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
import net.minecraftforge.registries.IForgeRegistry;

public class ItemBlockChemicalTank extends ItemBlockTooltip<BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>> implements IItemSustainedInventory, ISecurityItem {

    public ItemBlockChemicalTank(BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>> block) {
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
        StorageUtils.addStoredSubstance(stack, tooltip, tier == ChemicalTankTier.CREATIVE);
        if (tier == ChemicalTankTier.CREATIVE) {
            tooltip.add(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, MekanismLang.INFINITE));
        } else {
            tooltip.add(MekanismLang.CAPACITY_MB.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(tier.getStorage())));
        }
        super.addInformation(stack, world, tooltip, flag);
    }

    @Override
    public void addDetails(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, boolean advanced) {
        SecurityUtils.addSecurityTooltip(stack, tooltip);
        tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(hasInventory(stack))));
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
        if (isInGroup(group)) {
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

    private <CHEMICAL extends Chemical<CHEMICAL>> void fillItemGroup(BooleanSupplier shouldAdd, IForgeRegistry<CHEMICAL> registry, @Nonnull NonNullList<ItemStack> items,
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
    public boolean showDurabilityBar(ItemStack stack) {
        // No bar for empty containers as bars are drawn on top of stack count number
        return ChemicalUtil.hasGas(stack) || ChemicalUtil.hasChemical(stack, s -> true, Capabilities.INFUSION_HANDLER_CAPABILITY) ||
               ChemicalUtil.hasChemical(stack, s -> true, Capabilities.PIGMENT_HANDLER_CAPABILITY) ||
               ChemicalUtil.hasChemical(stack, s -> true, Capabilities.SLURRY_HANDLER_CAPABILITY);
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return StorageUtils.getDurabilityForDisplay(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return ChemicalUtil.getRGBDurabilityForDisplay(stack);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemCapabilityWrapper(stack, ChemicalTankContentsHandler.create(Attribute.getTier(getBlock(), ChemicalTankTier.class)));
    }
}