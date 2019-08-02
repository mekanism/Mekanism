package mekanism.common.block;

import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.Mekanism;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.base.IRedstoneControl;
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
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

/**
 * Special handling for block drops that need TileEntity data
 */
public abstract class BlockTileDrops extends Block {

    protected BlockTileDrops(Material materialIn) {
        super(materialIn);
    }

    @Nonnull
    protected ItemStack getDropItem(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        ItemStack itemStack = new ItemStack(this);
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null) {
            return itemStack;
        }
        if (!(tileEntity instanceof TileEntityBasicBlock)) {
            //TODO let it do the down below checks anyways
            return itemStack;
        }
        TileEntityBasicBlock tile = (TileEntityBasicBlock) tileEntity;
        //TODO: If crashes happen here because of lack of NBT make things use ItemDataUtils
        Item item = itemStack.getItem();

        //Set any data that is block specific rather than tile specific
        itemStack = setItemData(state, world, pos, tile, itemStack);

        if (item instanceof ISecurityItem && tile instanceof ISecurityTile) {
            ISecurityItem securityItem = (ISecurityItem) item;
            ISecurityTile securityTile = (ISecurityTile) tile;
            securityItem.setOwnerUUID(itemStack, securityTile.getSecurity().getOwnerUUID());
            securityItem.setSecurity(itemStack, securityTile.getSecurity().getMode());
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
        if (tile instanceof IRedstoneControl) {
            ItemDataUtils.setInt(itemStack, "controlType", ((IRedstoneControl) tile).getControlType().ordinal());
        }
        if (item instanceof ISustainedInventory && tile instanceof TileEntityContainerBlock && ((TileEntityContainerBlock) tile).inventory.size() > 0) {
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
        if (item instanceof IEnergizedItem && tile instanceof IStrictEnergyStorage && !(tile instanceof TileEntityMultiblock<?>)) {
            ((IEnergizedItem) item).setEnergy(itemStack, ((IStrictEnergyStorage) tile).getEnergy());
        }
        return itemStack;
    }

    protected ItemStack setItemData(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull TileEntityBasicBlock tile, @Nonnull ItemStack stack) {
        return stack;
    }

    /**
     * {@inheritDoc} Used together with {@link Block#removedByPlayer(IBlockState, World, BlockPos, EntityPlayer, boolean)}.
     *
     * @author Forge
     * @see BlockFlowerPot#harvestBlock(World, EntityPlayer, BlockPos, IBlockState, TileEntity, ItemStack)
     */
    @Override
    public void harvestBlock(@Nonnull World world, EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, TileEntity te, ItemStack stack) {
        super.harvestBlock(world, player, pos, state, te, stack);
        world.setBlockToAir(pos);
    }

    /**
     * Returns that this "cannot" be silk touched. This is so that {@link Block#getSilkTouchDrop(IBlockState)} is not called, because only {@link
     * Block#getDrops(NonNullList, IBlockAccess, BlockPos, IBlockState, int)} supports tile entities. Our blocks keep their inventory and other behave like they are being
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
    public void getDrops(@Nonnull NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, @Nonnull IBlockState state, int fortune) {
        drops.add(getDropItem(state, world, pos));
    }

    /**
     * {@inheritDoc} Keep tile entity in world until after {@link Block#getDrops(NonNullList, IBlockAccess, BlockPos, IBlockState, int)}. Used together with {@link
     * Block#harvestBlock(World, EntityPlayer, BlockPos, IBlockState, TileEntity, ItemStack)}.
     *
     * @author Forge
     * @see BlockFlowerPot#removedByPlayer(IBlockState, World, BlockPos, EntityPlayer, boolean)
     */
    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) {
        return willHarvest || super.removedByPlayer(state, world, pos, player, false);
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player) {
        return getDropItem(state, world, pos);
    }

    //TODO: Try to merge BlockMekanismContainer and this class

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null) {
            return;
        }
        if (!(tileEntity instanceof TileEntityBasicBlock)) {
            //TODO: Allow TileEntity to check against below things
            return;
        }
        //TODO: Remove most implementations of ItemBlock#placeBlockAt and use this method instead
        //TODO: Should this just be TileEntity and then check instance of and abstract things further
        TileEntityBasicBlock tile = (TileEntityBasicBlock) tileEntity;
        if (this instanceof IStateFacing) {
            EnumFacing change = EnumFacing.SOUTH;
            if (tile.canSetFacing(EnumFacing.DOWN) && tile.canSetFacing(EnumFacing.UP)) {
                int height = Math.round(placer.rotationPitch);
                if (height >= 65) {
                    change = EnumFacing.UP;
                } else if (height <= -65) {
                    change = EnumFacing.DOWN;
                }
            }
            if (change != EnumFacing.DOWN && change != EnumFacing.UP) {
                int side = MathHelper.floor((placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
                switch (side) {
                    case 0:
                        change = EnumFacing.NORTH;
                        break;
                    case 1:
                        change = EnumFacing.EAST;
                        break;
                    case 2:
                        change = EnumFacing.SOUTH;
                        break;
                    case 3:
                        change = EnumFacing.WEST;
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

        if (item instanceof ISecurityItem && tile instanceof ISecurityTile) {
            ISecurityItem securityItem = (ISecurityItem) item;
            ISecurityTile security = (ISecurityTile) tile;
            security.getSecurity().setMode(securityItem.getSecurity(stack));
            UUID ownerUUID = securityItem.getOwnerUUID(stack);
            security.getSecurity().setOwnerUUID(ownerUUID == null ? placer.getUniqueID() : ownerUUID);
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
        if (tile instanceof ISustainedData && stack.hasTagCompound()) {
            ((ISustainedData) tile).readSustainedData(stack);
        }
        if (tile instanceof IRedstoneControl) {
            if (ItemDataUtils.hasData(stack, "controlType")) {
                ((IRedstoneControl) tile).setControlType(RedstoneControl.values()[ItemDataUtils.getInt(stack, "controlType")]);
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
    public void setTileData(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack, TileEntityBasicBlock tile) {

    }
}