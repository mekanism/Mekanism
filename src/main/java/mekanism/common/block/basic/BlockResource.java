package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.common.block.BlockBasic;
import mekanism.common.resource.BlockResourceInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

//TODO: Make this not extend BlockBasic
public class BlockResource extends BlockBasic {

    @Nonnull
    private final BlockResourceInfo resource;

    //TODO: Isn't as "generic"? So make it be from one BlockType thing?
    public BlockResource(@Nonnull BlockResourceInfo resource) {
        super("block_" + resource.getRegistrySuffix());
        this.resource = resource;
        setHardness(this.resource.getHardness());
        setResistance(this.resource.getResistance());
        //It gets multiplied by 15 when being set
        setLightLevel(this.resource.getLightValue() / 15.0F);
    }

    @Nonnull
    public BlockResourceInfo getResourceInfo() {
        return resource;
    }

    @Override
    public boolean isBeaconBase(IBlockAccess world, BlockPos pos, BlockPos beacon) {
        return resource.isBeaconBase();
    }
}