package mekanism.common.item.block;

import java.util.EnumMap;
import java.util.Map;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.component.component.AttachedEjector;
import mekanism.common.component.component.AttachedSideConfig;
import mekanism.common.component.component.AttachedSideConfig.LightConfigInfo;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import mekanism.common.util.StorageUtils;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemBlockChemicalTank extends ItemBlockTooltip<BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>> {

    private static final AttachedSideConfig SIDE_CONFIG = Util.make(() -> {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.FRONT_OUT_NO_EJECT);
        configInfo.put(TransmissionType.CHEMICAL, LightConfigInfo.FRONT_OUT_EJECT);
        return new AttachedSideConfig(configInfo);
    });

    public ItemBlockChemicalTank(BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>> block, Item.Properties properties) {
        super(block, properties
              .component(MekanismDataComponents.CHEMICAL_TANK_TIER, Attribute.getTierNN(block, ChemicalTankTier.class))
              .component(MekanismDataComponents.DUMP_MODE, GasMode.IDLE)
              .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
              .component(MekanismDataComponents.SIDE_CONFIG, SIDE_CONFIG)
        );
    }
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return StorageUtils.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return StorageUtils.getBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ContainerType.CHEMICAL.getRGBDurabilityForDisplay(stack);
    }
}