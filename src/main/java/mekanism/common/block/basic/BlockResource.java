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
import net.minecraft.block.BlockFire;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

//TODO: Make this not extend BlockBasic
public class BlockResource extends BlockTileDrops implements IHasModel, IBlockOreDict {

    @Nonnull
    private final BlockResourceInfo resource;

    //TODO: Isn't as "generic"? So make it be from one BlockType thing?
    public BlockResource(@Nonnull BlockResourceInfo resource) {
        super(Material.IRON);
        this.resource = resource;
        setHardness(this.resource.getHardness());
        setResistance(this.resource.getResistance());
        //It gets multiplied by 15 when being set
        setLightLevel(this.resource.getLightValue() / 15.0F);
        setCreativeTab(Mekanism.tabMekanism);
        //Ensure the name is lower case as with concatenating with values from enums it may not be
        String name = "block_" + resource.getRegistrySuffix().toLowerCase(Locale.ROOT);
        setTranslationKey(name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, name));
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
    public boolean isBeaconBase(IBlockAccess world, BlockPos pos, BlockPos beacon) {
        return resource.isBeaconBase();
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos) {
        if (!world.isRemote) {
            Block newBlock = world.getBlockState(fromPos).getBlock();
            if (resource == BlockResourceInfo.REFINED_OBSIDIAN && newBlock instanceof BlockFire) {
                BlockPortalOverride.instance.trySpawnPortal(world, fromPos);
            }
        }
    }

    @Override
    @Deprecated
    public boolean isSideSolid(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side) {
        //TODO: Figure out if this short circuit is good
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        //TODO: Is this needed at all, was in BlockBasic
        world.markBlockRangeForRenderUpdate(pos, pos.add(1, 1, 1));
        world.checkLightFor(EnumSkyBlock.BLOCK, pos);
        world.checkLightFor(EnumSkyBlock.SKY, pos);
    }

    @Nonnull
    @Override
    protected ItemStack getDropItem(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        //TODO: Should we even be extending BlockTileDrops for BlockResource (probably not), as it doesn't have a tile
        return new ItemStack(this);
    }
}