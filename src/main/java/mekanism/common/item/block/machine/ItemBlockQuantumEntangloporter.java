package mekanism.common.item.block.machine;

import java.util.EnumMap;
import java.util.Map;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.component.component.AttachedEjector;
import mekanism.common.component.component.AttachedSideConfig;
import mekanism.common.component.component.AttachedSideConfig.LightConfigInfo;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.FrequencyTypes;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;

public class ItemBlockQuantumEntangloporter extends ItemBlockTooltip<BlockTileModel<TileEntityQuantumEntangloporter, BlockTypeTile<TileEntityQuantumEntangloporter>>>
      implements IFrequencyItem {

    private static final AttachedSideConfig SIDE_CONFIG = Util.make(() -> {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.FRONT_OUT_NO_EJECT);
        configInfo.put(TransmissionType.FLUID, LightConfigInfo.FRONT_OUT_NO_EJECT);
        configInfo.put(TransmissionType.CHEMICAL, LightConfigInfo.FRONT_OUT_NO_EJECT);
        configInfo.put(TransmissionType.ENERGY, LightConfigInfo.FRONT_OUT_NO_EJECT);
        configInfo.put(TransmissionType.HEAT, LightConfigInfo.INPUT_OUT_ALL);
        return new AttachedSideConfig(configInfo);
    });

    public ItemBlockQuantumEntangloporter(BlockTileModel<TileEntityQuantumEntangloporter, BlockTypeTile<TileEntityQuantumEntangloporter>> block, Item.Properties properties) {
        super(block, properties
              .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
              .component(MekanismDataComponents.SIDE_CONFIG, SIDE_CONFIG)
              .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
        );
    }

    @Override
    public FrequencyType<?> getFrequencyType() {
        return FrequencyTypes.INVENTORY;
    }
}