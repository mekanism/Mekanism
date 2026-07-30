package mekanism.common.item.block.machine;

import mekanism.common.block.prefab.BlockTile;
import mekanism.common.component.FrequencyAware;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.interfaces.IColoredItem;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.FrequencyTypes;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ItemBlockQIOComponent extends ItemBlockTooltip<BlockTile<?, ?>> implements IColoredItem, IFrequencyItem {

    public ItemBlockQIOComponent(BlockTile<?, ?> block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!level.isClientSide() && level.getGameTime() % (5 * SharedConstants.TICKS_PER_SECOND) == 0) {
            syncColorWithFrequency(stack);
        }
    }

    @Override
    public FrequencyType<?> getFrequencyType() {
        return FrequencyTypes.QIO;
    }

    @Override
    public DataComponentType<FrequencyAware<QIOFrequency>> getFrequencyComponent() {
        return MekanismDataComponents.QIO_FREQUENCY.get();
    }
}