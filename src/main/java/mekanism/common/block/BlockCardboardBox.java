package mekanism.common.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.block.interfaces.IHasModel;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateStorage;
import mekanism.common.item.block.ItemBlockCardboardBox;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BlockCardboardBox extends BlockMekanismContainer implements IHasModel, IStateStorage, IHasTileEntity<TileEntityCardboardBox> {

    private static boolean testingPlace = false;

    public BlockCardboardBox() {
        super(Material.CLOTH);
        setHardness(0.5F);
        setResistance(1F);
        MinecraftForge.EVENT_BUS.register(this);
        setRegistryName(new ResourceLocation(Mekanism.MODID, "cardboard_box"));
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return BlockStateHelper.getBlockState(this);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        //TODO
        return 0;
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockState getActualState(@Nonnull BlockState state, IBlockAccess world, BlockPos pos) {
        return BlockStateHelper.getActualState(this, state, MekanismUtils.getTileEntitySafe(world, pos));
    }

    @Override
    public boolean isReplaceable(IBlockAccess world, @Nonnull BlockPos pos) {
        return testingPlace;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity entityplayer, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && entityplayer.isSneaking()) {
            TileEntityCardboardBox tileEntity = (TileEntityCardboardBox) world.getTileEntity(pos);

            if (tileEntity != null && tileEntity.storedData != null) {
                BlockData data = tileEntity.storedData;
                testingPlace = true;
                if (!data.block.canPlaceBlockAt(world, pos)) {
                    testingPlace = false;
                    return true;
                }
                testingPlace = false;
                if (data.block != null) {
                    BlockState newstate = data.block.getStateForPlacement(world, pos, side, hitX, hitY, hitZ, data.meta, entityplayer, hand);
                    data.meta = newstate.getBlock().getMetaFromState(newstate);
                }
                world.setBlockState(pos, data.block.getStateFromMeta(data.meta), 3);
                if (data.tileTag != null && world.getTileEntity(pos) != null) {
                    data.updateLocation(pos);
                    world.getTileEntity(pos).readFromNBT(data.tileTag);
                }
                if (data.block != null) {
                    data.block.onBlockPlacedBy(world, pos, data.block.getStateFromMeta(data.meta), entityplayer, new ItemStack(data.block, 1, data.meta));
                }
                spawnAsEntity(world, pos, MekanismBlock.CARDBOARD_BOX.getItemStack());
            }
        }
        return entityplayer.isSneaking();
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull BlockState state) {
        return new TileEntityCardboardBox();
    }

    @Nonnull
    @Override
    protected ItemStack getDropItem(@Nonnull BlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileEntityCardboardBox tile = (TileEntityCardboardBox) world.getTileEntity(pos);
        ItemStack itemStack = new ItemStack(this);
        if (tile == null) {
            return itemStack;
        }
        if (tile.storedData != null) {
            ((ItemBlockCardboardBox) itemStack.getItem()).setBlockData(itemStack, tile.storedData);
        }
        return itemStack;
    }

    /**
     * If the player is sneaking and the dest block is a cardboard box, ensure onBlockActivated is called, and that the item use is not.
     *
     * @param blockEvent event
     */
    @SubscribeEvent
    public void rightClickEvent(RightClickBlock blockEvent) {
        if (blockEvent.getPlayerEntity().isSneaking() && blockEvent.getWorld().getBlockState(blockEvent.getPos()).getBlock() == this) {
            blockEvent.setUseBlock(Event.Result.ALLOW);
            blockEvent.setUseItem(Event.Result.DENY);
        }
    }

    @Nullable
    @Override
    public Class<? extends TileEntityCardboardBox> getTileClass() {
        return TileEntityCardboardBox.class;
    }

    public static class BlockData {

        public Block block;
        public int meta;
        public CompoundNBT tileTag;

        public BlockData(Block b, int j, CompoundNBT nbtTags) {
            block = b;
            meta = j;
            tileTag = nbtTags;
        }

        public BlockData() {
        }

        public static BlockData read(CompoundNBT nbtTags) {
            BlockData data = new BlockData();
            data.block = Block.getBlockById(nbtTags.getInteger("id"));
            data.meta = nbtTags.getInteger("meta");
            if (nbtTags.hasKey("tileTag")) {
                data.tileTag = nbtTags.getCompoundTag("tileTag");
            }
            return data;
        }

        public void updateLocation(BlockPos pos) {
            if (tileTag != null) {
                tileTag.setInteger("x", pos.getX());
                tileTag.setInteger("y", pos.getY());
                tileTag.setInteger("z", pos.getZ());
            }
        }

        public CompoundNBT write(CompoundNBT nbtTags) {
            nbtTags.setInteger("id", Block.getIdFromBlock(block));
            nbtTags.setInteger("meta", meta);
            if (tileTag != null) {
                nbtTags.setTag("tileTag", tileTag);
            }
            return nbtTags;
        }
    }
}