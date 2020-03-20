package mekanism.common.block.attribute;

import java.util.function.Consumer;
import mekanism.api.tier.ITier;
import mekanism.common.block.interfaces.ITypeBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public interface Attribute {

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

    static Direction getFacing(BlockState state) {
        AttributeStateFacing attr = get(state.getBlock(), AttributeStateFacing.class);
        return attr == null ? null : attr.getDirection(state);
    }

    static BlockState setFacing(BlockState state, Direction facing) {
        AttributeStateFacing attr = get(state.getBlock(), AttributeStateFacing.class);
        return attr == null ? null : attr.setDirection(state, facing);
    }

    static boolean isActive(BlockState state) {
        AttributeStateActive attr = get(state.getBlock(), AttributeStateActive.class);
        return attr != null && attr.isActive(state);
    }

    static BlockState setActive(BlockState state, boolean active) {
        AttributeStateActive attr = get(state.getBlock(), AttributeStateActive.class);
        return attr == null ? null : attr.setActive(state, active);
    }

    static <T extends ITier> T getTier(Block block, Class<T> tierClass) {
        AttributeTier<T> attr = get(block, AttributeTier.class);
        return attr == null ? null : attr.getTier();
    }
}
