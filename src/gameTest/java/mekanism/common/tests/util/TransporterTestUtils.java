package mekanism.common.tests.util;

import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.common.content.network.transmitter.DiversionTransporter.DiversionControl;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

//TODO: PR support to make custom StructureTemplateBuilders to Neo?
@NothingNullByDefault
public class TransporterTestUtils {

    private TransporterTestUtils() {
    }

    public static CompoundTag containing(Item item) {
        return containing(new ItemStack(item));
    }

    public static CompoundTag containing(Item item, int amount) {
        return containing(new ItemStack(item, amount));
    }

    public static CompoundTag containing(ItemStack... stacks) {
        return ContainerHelper.saveAllItems(new CompoundTag(), NonNullList.of(ItemStack.EMPTY, stacks), registryAccess());
    }

    public static CompoundTag containing(ItemStack stack, int slots) {
        return ContainerHelper.saveAllItems(new CompoundTag(), NonNullList.withSize(slots, stack), registryAccess());
    }

    private static HolderLookup.Provider registryAccess() {
        return ServerLifecycleHooks.getCurrentServer().registryAccess();
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
            NBTUtils.writeEnum(tag, SerializationConstants.COLOR, color);
        }
        if (side != null) {
            int[] raw = new int[EnumUtils.DIRECTIONS.length];
            raw[side.ordinal()] = connectionType.ordinal();
            tag.putIntArray(SerializationConstants.CONNECTION, raw);
        }
        return tag;
    }

    public static CompoundTag diversionMode(Direction side, DiversionControl mode) {
        CompoundTag tag = new CompoundTag();
        int[] modes = new int[EnumUtils.DIRECTIONS.length];
        modes[side.ordinal()] = mode.ordinal();
        tag.putIntArray(SerializationConstants.MODE, modes);
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
        tag.putIntArray(SerializationConstants.MODE, new int[] {
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