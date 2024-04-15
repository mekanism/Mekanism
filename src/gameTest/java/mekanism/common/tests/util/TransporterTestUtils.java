package mekanism.common.tests.util;

import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public class TransporterTestUtils {

    private TransporterTestUtils() {
    }

    public static CompoundTag containing(ItemLike itemLike) {
        return containing(new ItemStack(itemLike));
    }

    public static CompoundTag containing(ItemLike itemLike, int amount) {
        return containing(new ItemStack(itemLike, amount));
    }

    public static CompoundTag containing(ItemStack... stacks) {
        CompoundTag tag = new CompoundTag();
        ListTag items = new ListTag();
        for (int i = 0; i < stacks.length; i++) {
            CompoundTag item = stacks[i].save(new CompoundTag());
            item.putByte(NBTConstants.SLOT, (byte) i);
            items.add(item);
        }
        tag.put(NBTConstants.ITEMS, items);
        return tag;
    }

    @Nullable
    public static CompoundTag colored(EnumColor color) {
        return colored(color, null);
    }

    @Nullable
    public static CompoundTag colored(@Nullable EnumColor color, @Nullable Direction pull) {
        return configured(color, pull, ConnectionType.PULL);
    }

    @Nullable
    public static CompoundTag configured(Direction side) {
        return configured(side, ConnectionType.PULL);
    }

    @Nullable
    public static CompoundTag configured(Direction side, ConnectionType connectionType) {
        return configured(null, side, connectionType);
    }

    @Nullable
    public static CompoundTag configured(@Nullable EnumColor color, @Nullable Direction side, ConnectionType connectionType) {
        if (color == null && side == null) {
            return null;
        }
        CompoundTag tag = new CompoundTag();
        if (color != null) {
            NBTUtils.writeEnum(tag, NBTConstants.COLOR, color);
        }
        if (side != null) {
            int[] raw = new int[EnumUtils.DIRECTIONS.length];
            raw[side.ordinal()] = connectionType.ordinal();
            tag.putIntArray(NBTConstants.CONNECTION, raw);
        }
        return tag;
    }
}