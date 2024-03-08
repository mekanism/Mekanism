package mekanism.common.block.attribute;

import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;
import mekanism.common.block.attribute.Attribute.TileAttribute;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.lib.multiblock.IInternalMultiblock;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockBehaviour.StateArgumentPredicate;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Attributes {

    public static final Attribute ACTIVE = new AttributeStateActive(0);
    public static final Attribute ACTIVE_LIGHT = new AttributeStateActive(8);
    //12 is the minimum light level required to be able to melt ice that is directly adjacent to the block,
    // we need to go slightly higher so that we can melt blocks that are also a distance of one away
    public static final Attribute ACTIVE_MELT_LIGHT = new AttributeStateActive(13);
    public static final Attribute ACTIVE_FULL_LIGHT = new AttributeStateActive(15);
    public static final Attribute COMPARATOR = new AttributeComparator();
    public static final Attribute INVENTORY = new AttributeInventory<>();
    public static final Attribute REDSTONE = new AttributeRedstone();
    public static final Attribute SECURITY = new AttributeSecurity();

    private Attributes() {
    }

    /** If a block supports security. */
    public static class AttributeSecurity implements Attribute {

        private AttributeSecurity() {
        }
    }

    /**
     * If a block has an inventory. Optionally allows for custom loot table providing. DelayedLootItemBuilder generic is due to the builder being in the Datagen source
     * set.
     */
    public static class AttributeInventory<DelayedLootItemBuilder extends ConditionUserBuilder<DelayedLootItemBuilder> & FunctionUserBuilder<DelayedLootItemBuilder>> implements Attribute {

        @Nullable
        private final Predicate<DelayedLootItemBuilder> customLootBuilder;

        /**
         * Create an Inventory attribute with custom loot function handling
         *
         * @param customLootBuilder consumes the Builders and returns `hasContents` for use in
         *                          {@link mekanism.common.loot.table.BaseBlockLootTables#dropSelfWithContents(java.util.List)}
         */
        @SuppressWarnings("JavadocReference")
        public AttributeInventory(@Nullable Predicate<DelayedLootItemBuilder> customLootBuilder) {
            this.customLootBuilder = customLootBuilder;
        }

        private AttributeInventory() {
            this(null);
        }

        public boolean applyLoot(DelayedLootItemBuilder builder) {
            return this.customLootBuilder != null && this.customLootBuilder.test(builder);
        }
    }

    /** If a block supports comparators. */
    public static class AttributeComparator implements Attribute {

        private AttributeComparator() {
        }
    }

    /** If a block supports integration with computers. */
    public record AttributeComputerIntegration(String name) implements Attribute {
    }

    /** If a block has a redstone input configuration. */
    public static class AttributeRedstone implements Attribute {

        private AttributeRedstone() {
        }
    }

    /** If mobs can spawn on the block. */
    public static class AttributeMobSpawn implements Attribute {

        public static final StateArgumentPredicate<EntityType<?>> NEVER_PREDICATE = (state, reader, pos, entityType) -> false;
        public static final AttributeMobSpawn NEVER = new AttributeMobSpawn(NEVER_PREDICATE);
        public static final AttributeMobSpawn WHEN_NOT_FORMED = new AttributeMobSpawn((state, reader, pos, entityType) -> {
            BlockEntity tile = WorldUtils.getTileEntity(reader, pos);
            if (tile instanceof IMultiblock<?> multiblockTile) {
                if (reader instanceof LevelReader levelReader && levelReader.isClientSide()) {
                    //If we are on the client just check if we are formed as we don't sync structure information
                    // to the client. This way the client is at least relatively accurate with what values
                    // it returns for if mobs can spawn
                    if (multiblockTile.getMultiblock().isFormed()) {
                        return false;
                    }
                } else if (multiblockTile.getMultiblock().isPositionInsideBounds(multiblockTile.getStructure(), pos.above())) {
                    //If the multiblock is formed and the position above this block is inside the bounds of the multiblock
                    // don't allow spawning on it.
                    return false;
                }
            } else if (tile instanceof IStructuralMultiblock structuralMultiblock && structuralMultiblock.hasFormedMultiblock()) {
                //Note: This isn't actually used as all our structural multiblocks are transparent and vanilla tends to not let
                // mobs spawn on glass or stuff
                if (reader instanceof LevelReader levelReader && levelReader.isClientSide()) {
                    //If we are on the client return we can't spawn if it is formed. This way the client is at least relatively
                    // accurate with what values it returns for if mobs can spawn
                    return false;
                } else {
                    BlockPos above = pos.above();
                    for (Structure structure : structuralMultiblock.getStructureMap().values()) {
                        //Manually handle the getMultiblockData logic to avoid extra lookups
                        MultiblockData data = structure.getMultiblockData();
                        if (data != null && data.isFormed() && data.isPositionInsideBounds(structure, above)) {
                            //If the multiblock is formed and the position above this block is inside the bounds of the multiblock
                            // don't allow spawning on it.
                            return false;
                        }
                    }
                }
            } else if (tile instanceof IInternalMultiblock internalMultiblock && internalMultiblock.hasFormedMultiblock()) {
                //If it is an internal multiblock don't allow spawning mobs on it if it is formed
                return false;
            }
            //Super implementation
            return state.isFaceSturdy(reader, pos, Direction.UP) && state.getLightEmission(reader, pos) < 14;
        });

        private final StateArgumentPredicate<EntityType<?>> spawningPredicate;

        public AttributeMobSpawn(StateArgumentPredicate<EntityType<?>> spawningPredicate) {
            this.spawningPredicate = spawningPredicate;
        }

        @Override
        public void adjustProperties(Properties props) {
            props.isValidSpawn(spawningPredicate);
        }
    }

    /** If a block can emit redstone. */
    public static class AttributeRedstoneEmitter<TILE extends TileEntityMekanism> implements TileAttribute<TILE> {

        private final ToIntBiFunction<TILE, Direction> redstoneFunction;

        public AttributeRedstoneEmitter(ToIntBiFunction<TILE, Direction> redstoneFunction) {
            this.redstoneFunction = redstoneFunction;
        }

        public int getRedstoneLevel(TILE tile, @NotNull Direction side) {
            return redstoneFunction.applyAsInt(tile, side);
        }
    }

    /** Custom explosion resistance attribute. */
    public record AttributeCustomResistance(float resistance) implements Attribute {//TODO: Adjust properties instead of having the override?
    }

    /** Light value attribute. */
    public static class AttributeLight implements Attribute {

        private final int light;

        public AttributeLight(int light) {
            this.light = light;
        }

        @Override
        public void adjustProperties(BlockBehaviour.Properties props) {
            BlockStateHelper.applyLightLevelAdjustments(props, state -> light);
        }
    }
}