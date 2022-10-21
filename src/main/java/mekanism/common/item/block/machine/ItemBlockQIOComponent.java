package mekanism.common.item.block.machine;

import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.interfaces.IColoredItem;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockQIOComponent extends ItemBlockTooltip<BlockTile<?, ?>> implements IColoredItem {

    public ItemBlockQIOComponent(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    protected void addStats(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        MekanismUtils.addFrequencyToTileTooltip(stack, FrequencyType.QIO, tooltip);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide && level.getGameTime() % 100 == 0) {
            EnumColor frequencyColor = getFrequency(stack) instanceof QIOFrequency frequency ? frequency.getColor() : null;
            EnumColor color = getColor(stack);
            if (color != frequencyColor) {
                setColor(stack, frequencyColor);
            }
        }
    }

    private Frequency getFrequency(ItemStack stack) {
        if (ItemDataUtils.hasData(stack, NBTConstants.COMPONENT_FREQUENCY, Tag.TAG_COMPOUND)) {
            CompoundTag frequencyComponent = ItemDataUtils.getCompound(stack, NBTConstants.COMPONENT_FREQUENCY);
            if (frequencyComponent.contains(FrequencyType.QIO.getName(), Tag.TAG_COMPOUND)) {
                CompoundTag frequencyCompound = frequencyComponent.getCompound(FrequencyType.QIO.getName());
                FrequencyIdentity identity = FrequencyIdentity.load(FrequencyType.QIO, frequencyCompound);
                if (identity != null && frequencyCompound.hasUUID(NBTConstants.OWNER_UUID)) {
                    return FrequencyType.QIO.getManager(identity, frequencyCompound.getUUID(NBTConstants.OWNER_UUID)).getFrequency(identity.key());
                }
            }
        }
        return null;
    }

    public static class ItemBlockQIOInventoryComponent extends ItemBlockQIOComponent implements IItemSustainedInventory {

        public ItemBlockQIOInventoryComponent(BlockTile<?, ?> block) {
            super(block);
        }
    }
}