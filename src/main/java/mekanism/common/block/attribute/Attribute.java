package mekanism.common.block.attribute;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public interface Attribute {

    public interface TileAttribute<TILE extends TileEntityMekanism> extends Attribute {}

    static boolean has(Block block, Class<? extends Attribute> type) {
        return block instanceof ITypeBlock && ((ITypeBlock) block).getType().has(type);
    }

    static <T extends Attribute> T get(Block block, Class<T> type) {
        return block instanceof ITypeBlock ? ((ITypeBlock) block).getType().get(type) : null;
    }

    static boolean has(Block block1, Block block2, Class<? extends Attribute> type) {
        return has(block1, type) && has(block2, type);
    }

    static <T extends Attribute> void ifHas(Block block, Class<T> type, Consumer<T> run) {
        if (block instanceof ITypeBlock) {
            T attribute = ((ITypeBlock) block).getType().get(type);
            if (attribute != null) {
                run.accept(attribute);
            }
        }
    }

    @Nullable
    static Direction getFacing(BlockState state) {
        AttributeStateFacing attr = get(state.getBlock(), AttributeStateFacing.class);
        return attr == null ? null : attr.getDirection(state);
    }

    @Nullable
    static BlockState setFacing(BlockState state, Direction facing) {
        AttributeStateFacing attr = get(state.getBlock(), AttributeStateFacing.class);
        return attr == null ? null : attr.setDirection(state, facing);
    }

    static boolean isActive(BlockState state) {
        AttributeStateActive attr = get(state.getBlock(), AttributeStateActive.class);
        return attr != null && attr.isActive(state);
    }

    @Nonnull
    static BlockState setActive(BlockState state, boolean active) {
        AttributeStateActive attr = get(state.getBlock(), AttributeStateActive.class);
        return attr == null ? state : attr.setActive(state, active);
    }

    @Nullable
    static <TIER extends ITier> TIER getTier(Block block, Class<TIER> tierClass) {
        AttributeTier<TIER> attr = get(block, AttributeTier.class);
        return attr == null ? null : attr.getTier();
    }

    @Nullable
    static BaseTier getBaseTier(Block block) {
        AttributeTier<?> attr = get(block, AttributeTier.class);
        return attr == null ? null : attr.getTier().getBaseTier();
    }
}
