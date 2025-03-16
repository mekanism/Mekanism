package mekanism.common.block.attribute;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Predicate;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.tile.base.TileEntityUpdateable;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Attribute {

    interface TileAttribute<TILE extends TileEntityUpdateable> extends Attribute {}

    default void adjustProperties(BlockBehaviour.Properties props) {
    }

    static boolean has(BlockState state, Class<? extends Attribute> type) {
        return has(state.getBlockHolder(), type);
    }

    static boolean has(Holder<Block> block, Class<? extends Attribute> type) {
        return has(block.value(), type);
    }

    static boolean has(Block block, Class<? extends Attribute> type) {
        return block instanceof ITypeBlock typeBlock && typeBlock.getType().has(type);
    }

    @Nullable
    static <ATTRIBUTE extends Attribute> ATTRIBUTE get(BlockState state, Class<ATTRIBUTE> type) {
        return get(state.getBlockHolder(), type);
    }

    @Nullable
    static <ATTRIBUTE extends Attribute> ATTRIBUTE get(Holder<Block> block, Class<ATTRIBUTE> type) {
        return get(block.value(), type);
    }

    @Nullable
    static <ATTRIBUTE extends Attribute> ATTRIBUTE get(Block block, Class<ATTRIBUTE> type) {
        return block instanceof ITypeBlock typeBlock ? typeBlock.getType().get(type) : null;
    }

    static <ATTRIBUTE extends Attribute> ATTRIBUTE getOrThrow(BlockState state, Class<ATTRIBUTE> type) {
        return getOrThrow(state.getBlockHolder(), type);
    }

    static <ATTRIBUTE extends Attribute> ATTRIBUTE getOrThrow(Holder<Block> block, Class<ATTRIBUTE> type) {
        ATTRIBUTE attribute = get(block, type);
        if (attribute == null) {
            throw new IllegalStateException("Expected " + block.value() + " to have an attribute of type " + type.getSimpleName());
        }
        return attribute;
    }

    static Collection<Attribute> getAll(Holder<Block> block) {
        return block.value() instanceof ITypeBlock typeBlock ? typeBlock.getType().getAll() : Collections.emptyList();
    }

    static <ATTRIBUTE extends Attribute> boolean matches(Holder<Block> block, Class<ATTRIBUTE> type, Predicate<? super ATTRIBUTE> checker) {
        return matches(block.value(), type, checker);
    }

    static <ATTRIBUTE extends Attribute> boolean matches(Block block, Class<ATTRIBUTE> type, Predicate<? super ATTRIBUTE> checker) {
        ATTRIBUTE attribute = get(block, type);
        return attribute != null && checker.test(attribute);
    }

    static <ATTRIBUTE extends Attribute> void ifPresent(Holder<Block> block, Class<ATTRIBUTE> type, Consumer<? super ATTRIBUTE> action) {
        ATTRIBUTE attribute = get(block, type);
        if (attribute != null) {
            action.accept(attribute);
        }
    }

    @Nullable
    static Direction getFacing(BlockState state) {
        AttributeStateFacing attr = get(state, AttributeStateFacing.class);
        return attr == null ? null : attr.getDirection(state);
    }

    @Nullable
    static BlockState setFacing(BlockState state, Direction facing) {
        AttributeStateFacing attr = get(state, AttributeStateFacing.class);
        return attr == null ? null : attr.setDirection(state, facing);
    }

    static boolean isActive(BlockState state) {
        AttributeStateActive attr = get(state, AttributeStateActive.class);
        return attr != null && attr.isActive(state);
    }

    @NotNull
    static BlockState setActive(BlockState state, boolean active) {
        AttributeStateActive attr = get(state, AttributeStateActive.class);
        return attr == null ? state : attr.setActive(state, active);
    }

    @Nullable
    static <TIER extends ITier> TIER getTier(Holder<Block> block, Class<TIER> tierClass) {
        return getTier(block.value(), tierClass);
    }

    @Nullable
    static <TIER extends ITier> TIER getTier(Block block, Class<TIER> tierClass) {
        AttributeTier<?> attr = get(block, AttributeTier.class);
        return attr == null ? null : tierClass.cast(attr.tier());
    }

    @Nullable
    static BaseTier getBaseTier(Holder<Block> block) {
        AttributeTier<?> attr = get(block, AttributeTier.class);
        return attr == null ? null : attr.tier().getBaseTier();
    }
}
