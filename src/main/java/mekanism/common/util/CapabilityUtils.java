package mekanism.common.util;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

public final class CapabilityUtils {

    private CapabilityUtils() {
    }

    @Nullable//TODO - 1.20.2: Look at usages as we don't want to limit to only supporting block entities when blocks may also need it
    //@Deprecated(forRemoval = true)
    public static <T> T getCapability(@Nullable BlockEntity blockEntity, @Nullable BlockCapability<T, @Nullable Direction> cap, @Nullable Direction side) {
        if (blockEntity == null || !blockEntity.hasLevel()) {//TODO: Is this case actually useful/necessary?
            return null;
        }
        return blockEntity.getLevel().getCapability(cap, blockEntity.getBlockPos(), null, blockEntity, side);
    }
}