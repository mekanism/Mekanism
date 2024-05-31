package mekanism.common.block.attribute;

import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;
import mekanism.common.block.attribute.Attribute.TileAttribute;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockBehaviour.StateArgumentPredicate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
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
            if (WorldUtils.isInsideFormedMultiblock(reader, pos, null)) {
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

    @FunctionalInterface
    public interface PathCheck {

        @Nullable
        PathType getBlockPathType(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @Nullable Mob mob);
    }

    public record AttributeCustomPathType(Attributes.PathCheck pathCheck) implements Attribute {

        public static final AttributeCustomPathType WHEN_NOT_FORMED = new AttributeCustomPathType((state, level, pos, mob) ->
              WorldUtils.isInsideFormedMultiblock(level, pos, mob) ? PathType.DAMAGE_OTHER : null);
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