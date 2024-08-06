package mekanism.common.item.block;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.component.AttachedEjector;
import mekanism.common.attachments.component.AttachedSideConfig;
import mekanism.common.attachments.component.AttachedSideConfig.LightConfigInfo;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.TextUtils;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

public class ItemBlockChemicalTank extends ItemBlockTooltip<BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>> {

    private static final AttachedSideConfig SIDE_CONFIG = Util.make(() -> {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.FRONT_OUT_NO_EJECT);
        configInfo.put(TransmissionType.CHEMICAL, LightConfigInfo.FRONT_OUT_EJECT);
        return new AttachedSideConfig(configInfo);
    });

    public ItemBlockChemicalTank(BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>> block, Item.Properties properties) {
        super(block, true, properties
              .component(MekanismDataComponents.DUMP_MODE, GasMode.IDLE)
              .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
              .component(MekanismDataComponents.SIDE_CONFIG, SIDE_CONFIG)
        );
    }

    @Override
    public ChemicalTankTier getTier() {
        return Attribute.getTier(getBlock(), ChemicalTankTier.class);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        ChemicalTankTier tier = getTier();
        StorageUtils.addStoredSubstance(stack, tooltip, tier == ChemicalTankTier.CREATIVE);
        if (tier == ChemicalTankTier.CREATIVE) {
            tooltip.add(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, MekanismLang.INFINITE));
        } else {
            tooltip.add(MekanismLang.CAPACITY_MB.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(tier.getStorage())));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        // No bar for empty or stacked containers as bars are drawn on top of stack count number
        if (stack.getCount() > 1) {
            //Note: Technically this is handled by the below checks as the capability isn't exposed,
            // but we may as well short circuit it here
            return false;
        }
        return ChemicalUtil.hasAnyChemical(stack);
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return ChemicalUtil.getRGBDurabilityForDisplay(stack);
    }
}