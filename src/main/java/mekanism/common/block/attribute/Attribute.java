package mekanism.common.block.attribute;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.function.Consumer;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Attribute {

    interface TileAttribute<TILE extends TileEntityMekanism> extends Attribute {}

    default void adjustProperties(BlockBehaviour.Properties props) {
    }

    static boolean has(BlockState state, Class<? extends Attribute> type) {
        return has(state.getBlock(), type);
    }

    static boolean has(Block block, Class<? extends Attribute> type) {
        return block instanceof ITypeBlock typeBlock && typeBlock.getType().has(type);
    }

    static <T extends Attribute> T get(BlockState state, Class<T> type) {
        return get(state.getBlock(), type);
    }

    static <T extends Attribute> T get(IBlockProvider blockProvider, Class<T> type) {
        return get(blockProvider.getBlock(), type);
    }

    static <T extends Attribute> T get(Block block, Class<T> type) {
        return block instanceof ITypeBlock typeBlock ? typeBlock.getType().get(type) : null;
    }

    static boolean has(Block block1, Block block2, Class<? extends Attribute> type) {
        return has(block1, type) && has(block2, type);
    }

    static Collection<Attribute> getAll(Block block) {
        return block instanceof ITypeBlock typeBlock ? typeBlock.getType().getAll() : Lists.newArrayList();
    }

    static <T extends Attribute> void ifHas(Block block, Class<T> type, Consumer<T> run) {
        if (block instanceof ITypeBlock typeBlock) {
            T attribute = typeBlock.getType().get(type);
            if (attribute != null) {
                run.accept(attribute);
            }
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
    static <TIER extends ITier> TIER getTier(IBlockProvider blockProvider, Class<TIER> tierClass) {
        return getTier(blockProvider.getBlock(), tierClass);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    static <TIER extends ITier> TIER getTier(Block block, Class<TIER> tierClass) {
        AttributeTier<TIER> attr = get(block, AttributeTier.class);
        return attr == null ? null : attr.tier();
    }

    @Nullable
    static BaseTier getBaseTier(Block block) {
        AttributeTier<?> attr = get(block, AttributeTier.class);
        return attr == null ? null : attr.tier().getBaseTier();
    }
}
