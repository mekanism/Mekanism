package mekanism.common.block.attribute;

import java.util.function.ToIntFunction;
import mekanism.common.block.attribute.Attribute.TileAttribute;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.block.AbstractBlock;

public class Attributes {

    private Attributes() {
    }

    /** If a block supports security. */
    public static class AttributeSecurity implements Attribute {}

    /** If a block has an inventory. */
    public static class AttributeInventory implements Attribute {}

    /** If a block supports comparators. */
    public static class AttributeComparator implements Attribute {}

    /** If a block has a redstone input configuration. */
    public static class AttributeRedstone implements Attribute {}

    /** If mobs can spawn on the block. */
    public static class AttributeNoMobSpawn implements Attribute {}

    /** If this block is a part of a multiblock. */
    public static class AttributeMultiblock implements Attribute {}

    /** If a block can emit redstone. */
    public static class AttributeRedstoneEmitter<TILE extends TileEntityMekanism> implements TileAttribute<TILE> {

        private final ToIntFunction<TILE> redstoneFunction;

        public AttributeRedstoneEmitter(ToIntFunction<TILE> redstoneFunction) {
            this.redstoneFunction = redstoneFunction;
        }

        public int getRedstoneLevel(TILE tile) {
            return redstoneFunction.applyAsInt(tile);
        }
    }

    /** Custom explosion resistance attribute. */
    public static class AttributeCustomResistance implements Attribute {

        private final float resistance;

        public AttributeCustomResistance(float resistance) {
            this.resistance = resistance;
        }

        public float getResistance() {
            return resistance;
        }
    }

    /** Light value attribute. */
    public static class AttributeLight implements Attribute {

        private final int light;

        public AttributeLight(int light) {
            this.light = light;
        }

        @Override
        public void adjustProperties(AbstractBlock.Properties props) {
            props.setLightLevel(state -> light);
        }
    }
}
