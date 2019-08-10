package mekanism.common.block.basic;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.PortalHelper.BlockPortalOverride;
import mekanism.common.block.interfaces.IBlockOreDict;
import mekanism.common.block.interfaces.IHasModel;
import mekanism.common.resource.BlockResourceInfo;
import net.minecraft.block.Block;
import net.minecraft.block.FireBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

//TODO: Should we even be extending BlockTileDrops for BlockResource (probably not), as it doesn't have a tile
public class BlockResource extends BlockTileDrops implements IHasModel, IBlockOreDict {

    @Nonnull
    private final BlockResourceInfo resource;

    //TODO: Isn't as "generic"? So make it be from one BlockType thing?
    public BlockResource(@Nonnull BlockResourceInfo resource) {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(resource.getHardness(), resource.getResistance()).lightValue(resource.getLightValue()));
        this.resource = resource;
        //Ensure the name is lower case as with concatenating with values from enums it may not be
        setRegistryName(new ResourceLocation(Mekanism.MODID, "block_" + resource.getRegistrySuffix().toLowerCase(Locale.ROOT)));
    }

    @Override
    public List<String> getOredictEntries() {
        return Collections.singletonList("block" + resource.getOreSuffix());
    }

    @Nonnull
    public BlockResourceInfo getResourceInfo() {
        return resource;
    }

    @Override
    public boolean isBeaconBase(IBlockReader world, BlockPos pos, BlockPos beacon) {
        return resource.isBeaconBase();
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            Block newBlock = world.getBlockState(neighborPos).getBlock();
            if (resource == BlockResourceInfo.REFINED_OBSIDIAN && newBlock instanceof FireBlock) {
                BlockPortalOverride.instance.trySpawnPortal(world, neighborPos);
            }
        }
    }
}