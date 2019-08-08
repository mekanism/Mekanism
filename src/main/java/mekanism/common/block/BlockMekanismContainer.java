package mekanism.common.block;

import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.Mekanism;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.base.ISustainedTank;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.item.IItemEnergized;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.multiblock.IStructuralMultiblock;
import mekanism.common.security.ISecurityItem;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.INameable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public abstract class BlockMekanismContainer extends ContainerBlock {

    protected BlockMekanismContainer(Material materialIn) {
        super(materialIn);
    }

    @Nonnull
    protected ItemStack getDropItem(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        ItemStack itemStack = new ItemStack(this);
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null) {
            return itemStack;
        }
        if (!(tileEntity instanceof TileEntityMekanism)) {
            //TODO let it do the down below checks anyways
            return itemStack;
        }
        TileEntityMekanism tile = (TileEntityMekanism) tileEntity;
        //TODO: If crashes happen here because of lack of NBT make things use ItemDataUtils

        Item item = itemStack.getItem();

        //Set any data that is block specific rather than tile specific
        itemStack = setItemData(state, world, pos, tile, itemStack);

        //TODO: tile.hasSecurity
        if (item instanceof ISecurityItem && tile.hasSecurity()) {
            ISecurityItem securityItem = (ISecurityItem) item;
            securityItem.setOwnerUUID(itemStack, tile.getSecurity().getOwnerUUID());
            securityItem.setSecurity(itemStack, tile.getSecurity().getMode());
        }
        if (tile instanceof IUpgradeTile) {
            ((IUpgradeTile) tile).getComponent().write(ItemDataUtils.getDataMap(itemStack));
        }
        if (tile instanceof ISideConfiguration) {
            ISideConfiguration config = (ISideConfiguration) tile;
            config.getConfig().write(ItemDataUtils.getDataMap(itemStack));
            config.getEjector().write(ItemDataUtils.getDataMap(itemStack));
        }
        if (tile instanceof ISustainedData) {
            ((ISustainedData) tile).writeSustainedData(itemStack);
        }
        if (tile.supportsRedstone()) {
            ItemDataUtils.setInt(itemStack, "controlType", tile.getControlType().ordinal());
        }
        if (item instanceof ISustainedInventory && tile.hasInventory() && tile.getSizeInventory() > 0) {
            ((ISustainedInventory) item).setInventory(((ISustainedInventory) tile).getInventory(), itemStack);
        }
        if (item instanceof ISustainedTank && tile instanceof ISustainedTank) {
            FluidStack fluidStack = ((ISustainedTank) tile).getFluidStack();
            if (fluidStack != null) {
                ISustainedTank sustainedTank = (ISustainedTank) item;
                if (sustainedTank.hasTank(itemStack)) {
                    sustainedTank.setFluidStack(fluidStack, itemStack);
                }
            }
        }
        if (item instanceof IEnergizedItem && tile.isElectric() && !(tile instanceof TileEntityMultiblock<?>)) {
            ((IEnergizedItem) item).setEnergy(itemStack, tile.getEnergy());
        }
        return itemStack;
    }

    protected ItemStack setItemData(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull TileEntityMekanism tile, @Nonnull ItemStack stack) {
        return stack;
    }

    /**
     * {@inheritDoc} Used together with {@link Block#removedByPlayer(BlockState, World, BlockPos, PlayerEntity, boolean)}.
     * <br>
     * This is like Vanilla's {@link ContainerBlock#harvestBlock(World, PlayerEntity, BlockPos, BlockState, TileEntity, ItemStack)} except that uses the custom {@link
     * ItemStack} from {@link #getDropItem(BlockState, IBlockReader, BlockPos)}
     *
     * @author Forge
     * @see FlowerPotBlock#harvestBlock(World, PlayerEntity, BlockPos, BlockState, TileEntity, ItemStack)
     */
    @Override
    public void harvestBlock(@Nonnull World world, PlayerEntity player, @Nonnull BlockPos pos, @Nonnull BlockState state, TileEntity te, @Nonnull ItemStack stack) {
        Stat blockStats = Stats.getBlockStats(this);
        if (blockStats != null) {
            player.addStat(blockStats);
        }
        player.addExhaustion(0.005F);
        if (!world.isRemote) {
            ItemStack dropItem = getDropItem(state, world, pos);
            if (te instanceof INameable) {
                dropItem.setStackDisplayName(((INameable) te).getName());
            }
            Block.spawnAsEntity(world, pos, dropItem);
        }
        //Set it to air like the flower pot's harvestBlock method
        world.removeBlock(pos, false);
    }

    /**
     * Returns that this "cannot" be silk touched. This is so that {@link Block#getSilkTouchDrop(BlockState)} is not called, because only {@link
     * Block#getDrops(NonNullList, IBlockReader, BlockPos, BlockState, int)} supports tile entities. Our blocks keep their inventory and other behave like they are being
     * silk touched by default anyway.
     *
     * @return false
     */
    @Override
    @Deprecated
    protected boolean canSilkHarvest() {
        return false;
    }

    @Override
    public void getDrops(@Nonnull NonNullList<ItemStack> drops, IBlockReader world, BlockPos pos, @Nonnull BlockState state, int fortune) {
        drops.add(getDropItem(state, world, pos));
    }

    /**
     * {@inheritDoc} Keep tile entity in world until after {@link Block#getDrops(NonNullList, IBlockReader, BlockPos, BlockState, int)}. Used together with {@link
     * Block#harvestBlock(World, PlayerEntity, BlockPos, BlockState, TileEntity, ItemStack)}.
     *
     * @author Forge
     * @see FlowerPotBlock#removedByPlayer(BlockState, World, BlockPos, PlayerEntity, boolean)
     */
    @Override
    public boolean removedByPlayer(@Nonnull BlockState state, World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, boolean willHarvest) {
        return willHarvest || super.removedByPlayer(state, world, pos, player, false);
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull BlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, PlayerEntity player) {
        return getDropItem(state, world, pos);
    }

    @Override
    public abstract TileEntity createTileEntity(@Nonnull World world, @Nonnull BlockState state);

    /**
     * Unused, Use {@link #createTileEntity(World, BlockState)} instead.
     */
    @Override
    public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
        return null;
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockRenderType getRenderType(@Nonnull BlockState state) {
        //This is needed because BlockContainer sets it to invisible, except we are not using TESRs for rendering most implementers of this class
        return BlockRenderType.MODEL;
    }


    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null) {
            return;
        }
        if (!(tileEntity instanceof TileEntityMekanism)) {
            //TODO: Allow TileEntity to check against below things
            return;
        }
        //TODO: Remove most implementations of ItemBlock#placeBlockAt and use this method instead
        //TODO: Should this just be TileEntity and then check instance of and abstract things further
        TileEntityMekanism tile = (TileEntityMekanism) tileEntity;
        if (this instanceof IStateFacing) {
            Direction change = Direction.SOUTH;
            if (tile.canSetFacing(Direction.DOWN) && tile.canSetFacing(Direction.UP)) {
                int height = Math.round(placer.rotationPitch);
                if (height >= 65) {
                    change = Direction.UP;
                } else if (height <= -65) {
                    change = Direction.DOWN;
                }
            }
            if (change != Direction.DOWN && change != Direction.UP) {
                int side = MathHelper.floor((placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
                switch (side) {
                    case 0:
                        change = Direction.NORTH;
                        break;
                    case 1:
                        change = Direction.EAST;
                        break;
                    case 2:
                        change = Direction.SOUTH;
                        break;
                    case 3:
                        change = Direction.WEST;
                        break;
                }
            }
            tile.setFacing(change);
        }
        tile.redstone = world.isBlockPowered(pos);

        if (tile instanceof IBoundingBlock) {
            ((IBoundingBlock) tile).onPlace();
        }

        /*
        //TODO: Block Basic had this, figure out why
        world.markBlockRangeForRenderUpdate(pos, pos.add(1, 1, 1));
        world.checkLightFor(EnumSkyBlock.BLOCK, pos);
        world.checkLightFor(EnumSkyBlock.SKY, pos);
         */
        if (!world.isRemote) {
            if (tile instanceof IMultiblock) {
                ((IMultiblock<?>) tile).doUpdate();
            }
            if (tile instanceof IStructuralMultiblock) {
                ((IStructuralMultiblock) tile).doUpdate();
            }
        }

        //Handle item
        Item item = stack.getItem();
        setTileData(world, pos, state, placer, stack, tile);

        if (item instanceof ISecurityItem && tile.hasSecurity()) {
            ISecurityItem securityItem = (ISecurityItem) item;
            tile.getSecurity().setMode(securityItem.getSecurity(stack));
            UUID ownerUUID = securityItem.getOwnerUUID(stack);
            tile.getSecurity().setOwnerUUID(ownerUUID == null ? placer.getUniqueID() : ownerUUID);
        }
        if (tile instanceof IUpgradeTile) {
            if (ItemDataUtils.hasData(stack, "upgrades")) {
                ((IUpgradeTile) tile).getComponent().read(ItemDataUtils.getDataMap(stack));
            }
        }
        if (tile instanceof ISideConfiguration) {
            ISideConfiguration config = (ISideConfiguration) tile;
            if (ItemDataUtils.hasData(stack, "sideDataStored")) {
                config.getConfig().read(ItemDataUtils.getDataMap(stack));
                config.getEjector().read(ItemDataUtils.getDataMap(stack));
            }
        }
        if (tile instanceof ISustainedData && stack.hasTag()) {
            ((ISustainedData) tile).readSustainedData(stack);
        }
        if (tile.supportsRedstone()) {
            if (ItemDataUtils.hasData(stack, "controlType")) {
                tile.setControlType(RedstoneControl.values()[ItemDataUtils.getInt(stack, "controlType")]);
            }
        }
        if (item instanceof ISustainedTank && tile instanceof ISustainedTank && ((ISustainedTank) item).hasTank(stack)) {
            FluidStack fluid = ((ISustainedTank) item).getFluidStack(stack);
            if (fluid != null) {
                ((ISustainedTank) tile).setFluidStack(fluid);
            }
        }
        if (item instanceof ISustainedInventory && tile instanceof ISustainedInventory) {
            ((ISustainedInventory) tile).setInventory(((ISustainedInventory) item).getInventory(stack));
        }
        /*if (item instanceof IItemEnergized && tile instanceof TileEntityElectricBlock) {
            ((TileEntityElectricBlock) tile).electricityStored = ((IItemEnergized) item).getEnergy(stack);
        }*/
        //The variant of it that was in BlockBasic
        if (item instanceof IItemEnergized && tile instanceof IStrictEnergyStorage && !(tile instanceof TileEntityMultiblock<?>)) {
            ((IStrictEnergyStorage) tile).setEnergy(((IItemEnergized) item).getEnergy(stack));
        }
        //TODO: Figure out if this is actually needed
        if (!world.isRemote) {
            Mekanism.packetHandler.sendUpdatePacket(tile);
        }
    }

    //TODO: Method to override for setting some simple tile specific stuff
    public void setTileData(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, @Nonnull TileEntityMekanism tile) {

    }

    @Override
    public boolean rotateBlock(World world, @Nonnull BlockPos pos, @Nonnull Direction axis) {
        if (this instanceof IStateFacing) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityMekanism) {
                TileEntityMekanism tile = (TileEntityMekanism) tileEntity;
                if (tile.isDirectional() && tile.canSetFacing(axis)) {
                    tile.setFacing(axis);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void breakBlock(World world, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IBoundingBlock) {
            ((IBoundingBlock) tile).onBreak();
        }
        super.breakBlock(world, pos, state);
    }
}