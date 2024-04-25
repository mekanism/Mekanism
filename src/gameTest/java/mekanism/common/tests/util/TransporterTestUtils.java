package mekanism.common.tests.util;

import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.common.content.network.transmitter.DiversionTransporter.DiversionControl;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

//TODO - 1.20.5: PR support to make custom StructureTemplateBuilders to Neo?
@NothingNullByDefault
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
            //TODO - 1.20.5: Re-evaluate this
            CompoundTag item = (CompoundTag) stacks[i].save(ServerLifecycleHooks.getCurrentServer().registryAccess(), new CompoundTag());
            item.putByte(NBTConstants.SLOT, (byte) i);
            items.add(item);
        }
        tag.put(NBTConstants.ITEMS, items);
        return tag;
    }

    public static CompoundTag containing(ItemStack stack, int slots) {
        CompoundTag tag = new CompoundTag();
        ListTag items = new ListTag();
        for (int i = 0; i < slots; i++) {
            //TODO - 1.20.5: Re-evaluate this
            CompoundTag item = (CompoundTag) stack.save(ServerLifecycleHooks.getCurrentServer().registryAccess(), new CompoundTag());
            item.putByte(NBTConstants.SLOT, (byte) i);
            items.add(item);
        }
        tag.put(NBTConstants.ITEMS, items);
        return tag;
    }

    @Nullable
    public static CompoundTag configured(EnumColor color) {
        return configured(color, null);
    }

    @Nullable
    public static CompoundTag configured(@Nullable EnumColor color, @Nullable Direction pull) {
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

    public static CompoundTag diversionMode(Direction side, DiversionControl mode) {
        CompoundTag tag = new CompoundTag();
        int[] modes = new int[EnumUtils.DIRECTIONS.length];
        modes[side.ordinal()] = mode.ordinal();
        tag.putIntArray(NBTConstants.MODE, modes);
        return tag;
    }

    public static CompoundTag diversionModes(DiversionControl down, DiversionControl up, DiversionControl north, DiversionControl south, DiversionControl west, DiversionControl east) {
        return diversionModes(null, down, up, north, south, west, east);
    }

    public static CompoundTag diversionModes(@Nullable CompoundTag tag, DiversionControl down, DiversionControl up, DiversionControl north, DiversionControl south,
          DiversionControl west, DiversionControl east) {
        if (tag == null) {
            tag = new CompoundTag();
        }
        tag.putIntArray(NBTConstants.MODE, new int[] {
              down.ordinal(),
              up.ordinal(),
              north.ordinal(),
              south.ordinal(),
              west.ordinal(),
              east.ordinal()
        });
        return tag;
    }
}