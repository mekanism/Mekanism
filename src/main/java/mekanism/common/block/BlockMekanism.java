package mekanism.common.block;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.Attributes.AttributeComparator;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.network.PacketSecurityUpdate;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.tile.interfaces.IComparatorSupport;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.interfaces.ISustainedInventory;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Special handling for block drops that need TileEntity data
 */
public abstract class BlockMekanism extends Block {

    protected BlockMekanism(AbstractBlock.Properties properties) {
        super(BlockStateHelper.applyLightLevelAdjustments(properties));
        setDefaultState(BlockStateHelper.getDefaultState(stateContainer.getBaseState()));
    }

    @Nonnull
    @Override
    @Deprecated
    public PushReaction getPushReaction(@Nonnull BlockState state) {
        if (hasTileEntity(state)) {
            //Protect against mods like Quark that allow blocks with TEs to be moved
            //TODO: Eventually it would be nice to go through this and maybe even allow some TEs to be moved if they don't strongly
            // care about the world, but for now it is safer to just block them from being moved
            return PushReaction.BLOCK;
        }
        return super.getPushReaction(state);
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull BlockState state, RayTraceResult target, @Nonnull IBlockReader world, @Nonnull BlockPos pos, PlayerEntity player) {
        ItemStack itemStack = new ItemStack(this);
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return itemStack;
        }
        Item item = itemStack.getItem();
        if (item instanceof ISecurityItem && tile.hasSecurity()) {
            ISecurityItem securityItem = (ISecurityItem) item;
            securityItem.setOwnerUUID(itemStack, tile.getSecurity().getOwnerUUID());
            securityItem.setSecurity(itemStack, tile.getSecurity().getMode());
        }
        if (tile.supportsUpgrades()) {
            tile.getComponent().write(ItemDataUtils.getDataMap(itemStack));
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
            ItemDataUtils.setInt(itemStack, NBTConstants.CONTROL_TYPE, tile.getControlType().ordinal());
        }
        for (SubstanceType type : EnumUtils.SUBSTANCES) {
            if (tile.handles(type)) {
                ItemDataUtils.setList(itemStack, type.getContainerTag(), DataHandlerUtils.writeContainers(type.getContainers(tile)));
            }
        }
        if (item instanceof ISustainedInventory && tile.persistInventory() && tile.getSlots() > 0) {
            ((ISustainedInventory) item).setInventory(tile.getInventory(), itemStack);
        }
        return itemStack;
    }

    @Nonnull
    @Override
    @Deprecated
    public List<ItemStack> getDrops(@Nonnull BlockState state, @Nonnull LootContext.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        //Check if we need to clear any radioactive materials from the stored tanks as those will be dumped via the tile being removed
        if (state.getBlock() instanceof IHasTileEntity) {
            TileEntity tile = ((IHasTileEntity<?>) state.getBlock()).getTileType().create();
            if (tile instanceof TileEntityMekanism) {
                TileEntityMekanism mekTile = (TileEntityMekanism) tile;
                //Skip tiles that have no tanks and skip chemical creative tanks
                if (!mekTile.getGasTanks(null).isEmpty() && (!(mekTile instanceof TileEntityChemicalTank) ||
                                                             ((TileEntityChemicalTank) mekTile).getTier() != ChemicalTankTier.CREATIVE)) {
                    for (ItemStack drop : drops) {
                        ListNBT gasTankList = ItemDataUtils.getList(drop, NBTConstants.GAS_TANKS);
                        if (!gasTankList.isEmpty()) {
                            int count = DataHandlerUtils.getMaxId(gasTankList, NBTConstants.TANK);
                            List<IGasTank> tanks = new ArrayList<>(count);
                            for (int i = 0; i < count; i++) {
                                tanks.add(ChemicalTankBuilder.GAS.createDummy(Long.MAX_VALUE));
                            }
                            DataHandlerUtils.readContainers(tanks, gasTankList);
                            boolean hasRadioactive = false;
                            for (IGasTank tank : tanks) {
                                if (!tank.isEmpty() && tank.getStack().has(GasAttributes.Radiation.class)) {
                                    //If the tank isn't empty and has a radioactive gas in it, clear the tank and mark we need to update the item
                                    hasRadioactive = true;
                                    tank.setEmpty();
                                }
                            }
                            if (hasRadioactive) {
                                //If the item has any gas tanks stored, check if any have radioactive substances in them
                                // and if so clear them out
                                ListNBT newGasTankList = DataHandlerUtils.writeContainers(tanks);
                                if (newGasTankList.isEmpty()) {
                                    //If the list is now empty remove it
                                    ItemDataUtils.removeData(drop, NBTConstants.GAS_TANKS);
                                } else {
                                    //Otherwise update the list
                                    ItemDataUtils.setList(drop, NBTConstants.GAS_TANKS, newGasTankList);
                                }
                            }
                        }
                    }
                }
            }
        }
        return drops;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return this instanceof IHasTileEntity;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        if (this instanceof IHasTileEntity) {
            return ((IHasTileEntity<?>) this).getTileType().create();
        }
        return null;
    }

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        BlockStateHelper.fillBlockStateContainer(this, builder);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@Nonnull BlockItemUseContext context) {
        return BlockStateHelper.getStateForPlacement(this, super.getStateForPlacement(context), context);
    }

    @Nonnull
    @Override
    @Deprecated
    public FluidState getFluidState(BlockState state) {
        if (state.getBlock() instanceof IStateFluidLoggable) {
            return ((IStateFluidLoggable) state.getBlock()).getFluid(state);
        }
        return super.getFluidState(state);
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockState updatePostPlacement(BlockState state, @Nonnull Direction facing, @Nonnull BlockState facingState, @Nonnull IWorld world, @Nonnull BlockPos currentPos,
          @Nonnull BlockPos facingPos) {
        if (state.getBlock() instanceof IStateFluidLoggable) {
            ((IStateFluidLoggable) state.getBlock()).updateFluids(state, world, currentPos);
        }
        return super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return;
        }
        if (tile.supportsRedstone()) {
            tile.redstone = world.isBlockPowered(pos);
        }

        tile.onPlace();

        //Handle item
        Item item = stack.getItem();
        setTileData(world, pos, state, placer, stack, tile);

        if (tile instanceof TileEntitySecurityDesk && placer != null) {
            tile.getSecurity().setOwnerUUID(placer.getUniqueID());
        }
        if (item instanceof ISecurityItem && tile.hasSecurity()) {
            ISecurityItem securityItem = (ISecurityItem) item;
            tile.getSecurity().setMode(securityItem.getSecurity(stack));
            UUID ownerUUID = securityItem.getOwnerUUID(stack);
            if (ownerUUID != null) {
                tile.getSecurity().setOwnerUUID(ownerUUID);
            } else if (placer != null) {
                tile.getSecurity().setOwnerUUID(placer.getUniqueID());
            }
            if (!world.isRemote && placer != null) {
                Mekanism.packetHandler.sendToAll(new PacketSecurityUpdate(placer.getUniqueID(), null));
            }
        }
        if (tile.supportsUpgrades()) {
            //The read method validates that data is stored
            tile.getComponent().read(ItemDataUtils.getDataMap(stack));
        }
        if (tile instanceof ISideConfiguration) {
            ISideConfiguration config = (ISideConfiguration) tile;
            //The read methods validate that data is stored
            config.getConfig().read(ItemDataUtils.getDataMap(stack));
            config.getEjector().read(ItemDataUtils.getDataMap(stack));
        }
        for (SubstanceType type : EnumUtils.SUBSTANCES) {
            if (type.canHandle(tile)) {
                DataHandlerUtils.readContainers(type.getContainers(tile), ItemDataUtils.getList(stack, type.getContainerTag()));
            }
        }
        if (tile instanceof ISustainedData && stack.hasTag()) {
            ((ISustainedData) tile).readSustainedData(stack);
        }
        if (tile.supportsRedstone() && ItemDataUtils.hasData(stack, NBTConstants.CONTROL_TYPE, NBT.TAG_INT)) {
            tile.setControlType(RedstoneControl.byIndexStatic(ItemDataUtils.getInt(stack, NBTConstants.CONTROL_TYPE)));
        }
        if (item instanceof ISustainedInventory && tile.persistInventory()) {
            tile.setInventory(((ISustainedInventory) item).getInventory(stack));
        }
    }

    //Method to override for setting some simple tile specific stuff
    public void setTileData(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack, TileEntityMekanism tile) {
    }

    @Override
    public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation rotation) {
        return AttributeStateFacing.rotate(state, world, pos, rotation);
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockState rotate(@Nonnull BlockState state, @Nonnull Rotation rotation) {
        return AttributeStateFacing.rotate(state, rotation);
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockState mirror(@Nonnull BlockState state, @Nonnull Mirror mirror) {
        return AttributeStateFacing.mirror(state, mirror);
    }

    @Override
    @Deprecated
    public void onBlockAdded(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState oldState, boolean isMoving) {
        if (state.hasTileEntity() && oldState.getBlock() != state.getBlock()) {
            TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            if (tile != null) {
                tile.onAdded();
            }
        }
        super.onBlockAdded(state, world, pos, oldState, isMoving);
    }

    @Override
    @Deprecated
    public void onReplaced(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (state.hasTileEntity() && (!state.isIn(newState.getBlock()) || !newState.hasTileEntity())) {
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);
            if (tile instanceof IBoundingBlock) {
                ((IBoundingBlock) tile).onBreak(state);
            }
        }
        super.onReplaced(state, world, pos, newState, isMoving);
    }

    @Override
    @Deprecated
    public boolean hasComparatorInputOverride(@Nonnull BlockState blockState) {
        return Attribute.has(this, AttributeComparator.class);
    }

    @Override
    @Deprecated
    public int getComparatorInputOverride(@Nonnull BlockState blockState, @Nonnull World world, @Nonnull BlockPos pos) {
        if (hasComparatorInputOverride(blockState)) {
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);
            //Double check the tile actually has comparator support
            if (tile instanceof IComparatorSupport) {
                IComparatorSupport comparatorTile = (IComparatorSupport) tile;
                if (comparatorTile.supportsComparator()) {
                    return comparatorTile.getCurrentRedstoneLevel();
                }
            }
        }
        return 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
        //Copy of ParticleManager#addBlockDestroyEffects, but removes the minimum number of particles each voxel shape produces
        state.getShape(world, pos).forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double xDif = Math.min(1, maxX - minX);
            double yDif = Math.min(1, maxY - minY);
            double zDif = Math.min(1, maxZ - minZ);
            //Don't force the counts to be at least two
            int xCount = MathHelper.ceil(xDif / 0.25);
            int yCount = MathHelper.ceil(yDif / 0.25);
            int zCount = MathHelper.ceil(zDif / 0.25);
            if (xCount > 0 && yCount > 0 && zCount > 0) {
                for (int x = 0; x < xCount; x++) {
                    for (int y = 0; y < yCount; y++) {
                        for (int z = 0; z < zCount; z++) {
                            double d4 = (x + 0.5) / xCount;
                            double d5 = (y + 0.5) / yCount;
                            double d6 = (z + 0.5) / zCount;
                            double d7 = d4 * xDif + minX;
                            double d8 = d5 * yDif + minY;
                            double d9 = d6 * zDif + minZ;
                            manager.addEffect(new DiggingParticle((ClientWorld) world, pos.getX() + d7, pos.getY() + d8,
                                  pos.getZ() + d9, d4 - 0.5, d5 - 0.5, d6 - 0.5, state).setBlockPos(pos));
                        }
                    }
                }
            }
        });
        return true;
    }

    protected ActionResultType genericClientActivated(@Nonnull PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult hit) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() instanceof BlockItem && new BlockItemUseContext(player, hand, stack, hit).canPlace() && !Attribute.has(this, AttributeGui.class)) {
            return ActionResultType.PASS;
        }
        return ActionResultType.SUCCESS;
    }
}