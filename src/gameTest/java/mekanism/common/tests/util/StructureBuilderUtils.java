package mekanism.common.tests.util;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.content.network.transmitter.DiversionTransporter.DiversionControl;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.inventory.slot.QIODriveSlot;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

//TODO: PR support to make custom StructureTemplateBuilders to Neo?
@NothingNullByDefault
public class StructureBuilderUtils {

    private StructureBuilderUtils() {
    }

    public static CompoundTag containing(Item item) {
        return containing(new ItemStack(item));
    }

    public static CompoundTag containing(Item item, int amount) {
        return containing(new ItemStack(item, amount));
    }

    public static CompoundTag containing(ItemStack... stacks) {
        return containing(NonNullList.of(ItemStack.EMPTY, stacks));
    }

    public static CompoundTag containing(ItemStack stack, int slots) {
        return containing(NonNullList.withSize(slots, stack));
    }

    private static CompoundTag containing(NonNullList<ItemStack> items) {
        //TODO - 26.1: Should we pass a path to the scoped collector?
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(Mekanism.logger)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registryAccess());
            ContainerHelper.saveAllItems(output, items);
            return output.buildResult();
        }
    }

    public static CompoundTag withDrive(DeferredHolder<Item, ? extends IQIODriveItem> drive) {
        List<IInventorySlot> driveSlots = new ArrayList<>();
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 6; x++) {
                //Note: this is unsafe to pass null for the drive slot, but it isn't used for serialization
                driveSlots.add(new QIODriveSlot(null, y * 6 + x, () -> null, null, 0, 0));
            }
        }
        driveSlots.getFirst().setContents(ItemResource.of(drive), 1);

        //TODO - 26.1: Should we pass a path to the scoped collector?
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(Mekanism.logger)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registryAccess());
            ContainerType.ITEM.saveTo(output, driveSlots);
            return output.buildResult();
        }
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
            tag.putInt(SerializationConstants.COLOR, color.ordinal());
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