package buildcraft.api.enums;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.IStringSerializable;

import buildcraft.api.properties.BuildCraftProperties;

public enum EnumSpring implements IStringSerializable {

    WATER(5, -1, Blocks.water.getDefaultState()),
    OIL(6000, 8, null); // Set in BuildCraftEnergy
    public static final EnumSpring[] VALUES = values();
    public final int tickRate, chance;
    public IBlockState liquidBlock;
    public boolean canGen = true;

    private EnumSpring(int tickRate, int chance, IBlockState liquidBlock) {
        this.tickRate = tickRate;
        this.chance = chance;
        this.liquidBlock = liquidBlock;
    }

    public static EnumSpring fromState(IBlockState state) {
        return (EnumSpring) state.getValue(BuildCraftProperties.SPRING_TYPE);
    }

    public String getName() {
        return this.name();
    }
}
