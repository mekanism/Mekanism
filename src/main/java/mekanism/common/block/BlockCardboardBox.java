package mekanism.common.block;

import javax.annotation.Nonnull;
import mekanism.api.block.IHasModel;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.MekanismBlock;
import mekanism.common.block.states.IStateStorage;
import mekanism.common.item.block.ItemBlockCardboardBox;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockCardboardBox extends BlockMekanismContainer implements IHasModel, IStateStorage, IHasTileEntity<TileEntityCardboardBox> {

    private static boolean testingPlace = false;

    public BlockCardboardBox() {
        super(Block.Properties.create(Material.WOOL).hardnessAndResistance(0.5F, 1F));
        MinecraftForge.EVENT_BUS.register(this);
    }

    //TODO: Test place
    /*@Override
    public boolean isReplaceable(IBlockReader world, @Nonnull BlockPos pos) {
        return testingPlace;
    }*/

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isRemote && player.isSneaking()) {
            TileEntityCardboardBox tileEntity = MekanismUtils.getTileEntity(TileEntityCardboardBox.class, world, pos);

            if (tileEntity != null && tileEntity.storedData != null) {
                BlockData data = tileEntity.storedData;
                //TODO: Test Place
                /*testingPlace = true;
                if (!data.block.canPlaceBlockAt(world, pos)) {
                    testingPlace = false;
                    return true;
                }
                testingPlace = false;*/
                if (data.block != null) {
                    //TODO: State for placement
                    //BlockState newstate = data.block.getStateForPlacement(world, pos, side, hitX, hitY, hitZ, data.meta, player, hand);
                    world.setBlockState(pos, data.block.getDefaultState());
                }
                if (data.tileTag != null) {
                    data.updateLocation(pos);
                    tileEntity.read(data.tileTag);
                }
                if (data.block != null) {
                    data.block.onBlockPlacedBy(world, pos, data.block.getDefaultState(), player, new ItemStack(data.block));
                }
                spawnAsEntity(world, pos, MekanismBlock.CARDBOARD_BOX.getItemStack());
            }
        }
        return player.isSneaking();
    }

    @Nonnull
    @Override
    protected ItemStack getDropItem(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        TileEntityCardboardBox tile = MekanismUtils.getTileEntity(TileEntityCardboardBox.class, world, pos);
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
        if (blockEvent.getPlayer().isSneaking() && blockEvent.getWorld().getBlockState(blockEvent.getPos()).getBlock() == this) {
            blockEvent.setUseBlock(Event.Result.ALLOW);
            blockEvent.setUseItem(Event.Result.DENY);
        }
    }

    @Override
    public TileEntityType<TileEntityCardboardBox> getTileType() {
        return MekanismTileEntityTypes.CARDBOARD_BOX.getTileEntityType();
    }

    public static class BlockData {

        public Block block;
        public CompoundNBT tileTag;

        public BlockData(Block b, CompoundNBT nbtTags) {
            block = b;
            tileTag = nbtTags;
        }

        public BlockData() {
        }

        public static BlockData read(CompoundNBT nbtTags) {
            BlockData data = new BlockData();
            data.block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbtTags.getString("registryName")));
            if (nbtTags.contains("tileTag")) {
                data.tileTag = nbtTags.getCompound("tileTag");
            }
            return data;
        }

        public void updateLocation(BlockPos pos) {
            if (tileTag != null) {
                tileTag.putInt("x", pos.getX());
                tileTag.putInt("y", pos.getY());
                tileTag.putInt("z", pos.getZ());
            }
        }

        public CompoundNBT write(CompoundNBT nbtTags) {
            nbtTags.putString("registryName", block.getRegistryName().toString());
            if (tileTag != null) {
                nbtTags.put("tileTag", tileTag);
            }
            return nbtTags;
        }
    }
}