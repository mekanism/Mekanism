package mekanism.common.block.states;

import java.util.Locale;
import mekanism.common.block.BlockOre;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.util.IStringSerializable;

/**
 * Created by ben on 23/12/14.
 */
public class BlockStateOre extends BlockStateContainer {

    public static final PropertyEnum<EnumOreType> typeProperty = PropertyEnum.create("type", EnumOreType.class);

    public BlockStateOre(BlockOre block) {
        super(block, typeProperty);
    }

    public enum EnumOreType implements IStringSerializable {
        OSMIUM,
        COPPER,
        TIN;

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
