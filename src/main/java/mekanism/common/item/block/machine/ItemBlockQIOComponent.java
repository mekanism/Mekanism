package mekanism.common.item.block.machine;

import java.util.function.Consumer;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.interfaces.IColoredItem;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.FrequencyTypes;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.MekanismUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemBlockQIOComponent extends ItemBlockTooltip<BlockTile<?, ?>> implements IColoredItem, IFrequencyItem {

    public ItemBlockQIOComponent(BlockTile<?, ?> block, Item.Properties properties) {
        super(block, true, properties);
    }

    @Override
    protected void addStats(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        MekanismUtils.addFrequencyItemTooltip(stack, context, tooltipDisplay, tooltipAdder, flag);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull ServerLevel level, @NotNull Entity entity, @Nullable EquipmentSlot slot) {
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