package mekanism.common.block.attribute;

import java.util.Objects;
import java.util.UUID;
import mekanism.common.lib.multiblock.IInternalMultiblock;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/** If this block is a part of a multiblock. */
public class AttributeMultiblock implements Attribute {

    public static final Attribute EXTERNAL = new AttributeMultiblock(MultiblockType.EXTERNAL);
    public static final Attribute STRUCTURAL = new AttributeMultiblock(MultiblockType.STRUCTURAL);
    public static final Attribute INTERNAL = new AttributeMultiblock(MultiblockType.INTERNAL);

    private final MultiblockType type;

    private AttributeMultiblock(MultiblockType type) {
        this.type = type;
    }

    @Nullable
    public MultiblockData getMultiblock(Level level, BlockPos pos, UUID uuid) {
        BlockEntity tile = WorldUtils.getTileEntity(level, pos);
        switch (type) {
            case EXTERNAL -> {
                if (tile instanceof IMultiblock<?> multiblockTile) {
                    MultiblockData multiblock = multiblockTile.getMultiblock();
                    if (Objects.equals(multiblock.inventoryID, uuid)) {
                        return multiblock;
                    }
                }
            }
            case STRUCTURAL -> {
                if (tile instanceof IStructuralMultiblock structuralMultiblock && structuralMultiblock.hasFormedMultiblock()) {
                    for (Structure structure : structuralMultiblock.getStructureMap().values()) {
                        //Manually handle the getMultiblockData logic to avoid extra lookups
                        MultiblockData data = structure.getMultiblockData();
                        if (data != null && data.isFormed() && Objects.equals(data.inventoryID, uuid)) {
                            return data;
                        }
                    }
                }
            }
            case INTERNAL -> {
                if (tile instanceof IInternalMultiblock internal && Objects.equals(internal.getMultiblockUUID(), uuid)) {
                    return internal.getMultiblock();
                }
            }
        }
        return null;
    }

    private enum MultiblockType {
        EXTERNAL,
        STRUCTURAL,
        INTERNAL
    }
}