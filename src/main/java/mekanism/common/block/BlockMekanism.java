package mekanism.common.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.Attributes.AttributeComparator;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.network.to_client.PacketSecurityUpdate;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IComparatorSupport;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.interfaces.ISustainedInventory;
import mekanism.common.tile.interfaces.ITileRadioactive;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.IBlockRenderProperties;

/**
 * Special handling for block drops that need TileEntity data
 */
public abstract class BlockMekanism extends Block {

    protected BlockMekanism(BlockBehaviour.Properties properties) {
        super(BlockStateHelper.applyLightLevelAdjustments(properties));
        registerDefaultState(BlockStateHelper.getDefaultState(stateDefinition.any()));
    }

    @Override
    public void initializeClient(Consumer<IBlockRenderProperties> consumer) {
        consumer.accept(RenderPropertiesProvider.particles());
    }

    @Nonnull
    @Override
    @Deprecated
    public PushReaction getPistonPushReaction(@Nonnull BlockState state) {
        if (state.hasBlockEntity()) {
            //Protect against mods like Quark that allow blocks with TEs to be moved
            //TODO: Eventually it would be nice to go through this and maybe even allow some TEs to be moved if they don't strongly
            // care about the world, but for now it is safer to just block them from being moved
            return PushReaction.BLOCK;
        }
        return super.getPistonPushReaction(state);
    }

    @Nonnull
    @Override
    public ItemStack getCloneItemStack(@Nonnull BlockState state, HitResult target, @Nonnull BlockGetter world, @Nonnull BlockPos pos, Player player) {
        ItemStack itemStack = new ItemStack(this);
        TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return itemStack;
        }
        //TODO: Some of the data doesn't get properly "picked", because there are cases such as before opening the GUI where
        // the server doesn't bother syncing the data to the client. For example with what frequencies there are
        Item item = itemStack.getItem();
        if (tile.getFrequencyComponent().hasCustomFrequencies()) {
            tile.getFrequencyComponent().write(ItemDataUtils.getDataMap(itemStack));
        }
        if (item instanceof ISecurityItem securityItem && tile.hasSecurity()) {
            securityItem.setOwnerUUID(itemStack, tile.getOwnerUUID());
            securityItem.setSecurity(itemStack, tile.getSecurityMode());
        }
        if (tile.supportsUpgrades()) {
            tile.getComponent().write(ItemDataUtils.getDataMap(itemStack));
        }
        if (tile instanceof ISideConfiguration config) {
            config.getConfig().write(ItemDataUtils.getDataMap(itemStack));
            config.getEjector().write(ItemDataUtils.getDataMap(itemStack));
        }
        if (tile instanceof ISustainedData sustainedData) {
            sustainedData.writeSustainedData(itemStack);
        }
        if (tile.supportsRedstone()) {
            ItemDataUtils.setInt(itemStack, NBTConstants.CONTROL_TYPE, tile.getControlType().ordinal());
        }
        for (SubstanceType type : EnumUtils.SUBSTANCES) {
            if (tile.handles(type)) {
                ItemDataUtils.setList(itemStack, type.getContainerTag(), DataHandlerUtils.writeContainers(type.getContainers(tile)));
            }
        }
        if (item instanceof ISustainedInventory sustainedInventory && tile.persistInventory() && tile.getSlots() > 0) {
            sustainedInventory.setInventory(tile.getInventory(), itemStack);
        }
        return itemStack;
    }

    @Nonnull
    @Override
    @Deprecated
    public List<ItemStack> getDrops(@Nonnull BlockState state, @Nonnull LootContext.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        //Check if we need to clear any radioactive materials from the stored tanks as those will be dumped via the tile being removed
        if (state.getBlock() instanceof IHasTileEntity<?> hasTileEntity) {
            BlockEntity tile = hasTileEntity.createDummyBlockEntity(state);
            if (tile instanceof TileEntityMekanism mekTile) {
                //Skip tiles that have no tanks and skip chemical creative tanks
                if (!mekTile.getGasTanks(null).isEmpty() && (!(mekTile instanceof TileEntityChemicalTank chemicalTank) ||
                                                             chemicalTank.getTier() != ChemicalTankTier.CREATIVE)) {
                    for (ItemStack drop : drops) {
                        ListTag gasTankList = ItemDataUtils.getList(drop, NBTConstants.GAS_TANKS);
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
                                ListTag newGasTankList = DataHandlerUtils.writeContainers(tanks);
                                if (newGasTankList.isEmpty()) {
                                    //If the list is now empty remove it
                                    ItemDataUtils.removeData(drop, NBTConstants.GAS_TANKS);
                                } else {
                                    //Otherwise, update the list
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
    @Deprecated
    public boolean triggerEvent(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, int id, int param) {
        boolean triggered = super.triggerEvent(state, level, pos, id, param);
        if (this instanceof IHasTileEntity<?> hasTileEntity) {
            return hasTileEntity.triggerBlockEntityEvent(state, level, pos, id, param);
        }
        return triggered;
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        BlockStateHelper.fillBlockStateContainer(this, builder);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
        return BlockStateHelper.getStateForPlacement(this, super.getStateForPlacement(context), context);
    }

    @Nonnull
    @Override
    @Deprecated
    public FluidState getFluidState(BlockState state) {
        if (state.getBlock() instanceof IStateFluidLoggable fluidLoggable) {
            return fluidLoggable.getFluid(state);
        }
        return super.getFluidState(state);
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockState updateShape(BlockState state, @Nonnull Direction facing, @Nonnull BlockState facingState, @Nonnull LevelAccessor world, @Nonnull BlockPos currentPos,
          @Nonnull BlockPos facingPos) {
        if (state.getBlock() instanceof IStateFluidLoggable fluidLoggable) {
            fluidLoggable.updateFluids(state, world, currentPos);
        }
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public void setPlacedBy(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return;
        }
        if (tile.supportsRedstone()) {
            tile.redstone = world.hasNeighborSignal(pos);
        }

        tile.onPlace();

        //Handle item
        Item item = stack.getItem();
        setTileData(world, pos, state, placer, stack, tile);

        //TODO - 1.18: Re-evaluate the entirety of this method and see what parts potentially should not be getting called at all when on the client side.
        // We previously had issues in readSustainedData regarding frequencies when on the client side so that is why the frequency data has this check
        // but there is a good chance a lot of this stuff has no real reason to need to be set on the client side at all
        if (!world.isClientSide && tile.getFrequencyComponent().hasCustomFrequencies()) {
            tile.getFrequencyComponent().read(ItemDataUtils.getDataMap(stack));
        }
        if (tile instanceof TileEntitySecurityDesk && placer != null) {
            tile.getSecurity().setOwnerUUID(placer.getUUID());
        }
        if (item instanceof ISecurityItem securityItem && tile.hasSecurity()) {
            tile.setSecurityMode(securityItem.getSecurity(stack));
            UUID ownerUUID = securityItem.getOwnerUUID(stack);
            if (ownerUUID != null) {
                tile.getSecurity().setOwnerUUID(ownerUUID);
            } else if (placer != null) {
                tile.getSecurity().setOwnerUUID(placer.getUUID());
                if (!world.isClientSide) {
                    //If the machine doesn't already have an owner, make sure we portray this
                    Mekanism.packetHandler().sendToAll(new PacketSecurityUpdate(placer.getUUID(), null));
                }
            }
        }
        if (tile.supportsUpgrades()) {
            //The read method validates that data is stored
            tile.getComponent().read(ItemDataUtils.getDataMap(stack));
        }
        if (tile instanceof ISideConfiguration config) {
            //The read methods validate that data is stored
            config.getConfig().read(ItemDataUtils.getDataMap(stack));
            config.getEjector().read(ItemDataUtils.getDataMap(stack));
        }
        for (SubstanceType type : EnumUtils.SUBSTANCES) {
            if (type.canHandle(tile)) {
                DataHandlerUtils.readContainers(type.getContainers(tile), ItemDataUtils.getList(stack, type.getContainerTag()));
            }
        }
        if (tile instanceof ISustainedData sustainedData && stack.hasTag()) {
            sustainedData.readSustainedData(stack);
        }
        if (tile.supportsRedstone() && ItemDataUtils.hasData(stack, NBTConstants.CONTROL_TYPE, Tag.TAG_INT)) {
            tile.setControlType(RedstoneControl.byIndexStatic(ItemDataUtils.getInt(stack, NBTConstants.CONTROL_TYPE)));
        }
        if (item instanceof ISustainedInventory sustainedInventory && tile.persistInventory()) {
            tile.setInventory(sustainedInventory.getInventory(stack));
        }
    }

    //Method to override for setting some simple tile specific stuff
    public void setTileData(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack, TileEntityMekanism tile) {
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation) {
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
    public void onPlace(BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState oldState, boolean isMoving) {
        if (state.hasBlockEntity() && oldState.getBlock() != state.getBlock()) {
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            if (tile != null) {
                tile.onAdded();
            }
        }
        super.onPlace(state, world, pos, oldState, isMoving);
    }

    @Override
    @Deprecated
    public boolean hasAnalogOutputSignal(@Nonnull BlockState blockState) {
        return Attribute.has(this, AttributeComparator.class);
    }

    @Override
    @Deprecated
    public int getAnalogOutputSignal(@Nonnull BlockState blockState, @Nonnull Level world, @Nonnull BlockPos pos) {
        if (hasAnalogOutputSignal(blockState)) {
            BlockEntity tile = WorldUtils.getTileEntity(world, pos);
            //Double-check the tile actually has comparator support
            if (tile instanceof IComparatorSupport comparatorTile && comparatorTile.supportsComparator()) {
                return comparatorTile.getCurrentRedstoneLevel();
            }
        }
        return 0;
    }

    @Override
    @Deprecated
    public float getDestroyProgress(@Nonnull BlockState state, @Nonnull Player player, @Nonnull BlockGetter world, @Nonnull BlockPos pos) {
        return getDestroyProgress(state, player, world, pos, state.hasBlockEntity() ? WorldUtils.getTileEntity(world, pos) : null);
    }

    /**
     * Like {@link BlockBehaviour#getDestroyProgress(BlockState, Player, BlockGetter, BlockPos)} except also passes the tile to only have to get it once.
     */
    protected float getDestroyProgress(@Nonnull BlockState state, @Nonnull Player player, @Nonnull BlockGetter world, @Nonnull BlockPos pos,
          @Nullable BlockEntity tile) {
        //Call super variant of player relative hardness to get default
        float speed = super.getDestroyProgress(state, player, world, pos);
        if (tile instanceof ITileRadioactive radioactiveTile && radioactiveTile.getRadiationScale() > 0) {
            //Our tile has some radioactive substance in it; slow down breaking it
            return speed / 5F;
        }
        return speed;
    }

    @Override
    public void animateTick(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Random random) {
        super.animateTick(state, world, pos, random);
        BlockEntity tile = WorldUtils.getTileEntity(world, pos);
        if (tile instanceof ITileRadioactive radioactiveTile) {
            int count = radioactiveTile.getRadiationParticleCount();
            if (count > 0) {
                //Update count to be randomized but store it instead of calculating our max number each time we loop
                count = random.nextInt(count);
                for (int i = 0; i < count; i++) {
                    double randX = pos.getX() - 0.1 + random.nextDouble() * 1.2;
                    double randY = pos.getY() - 0.1 + random.nextDouble() * 1.2;
                    double randZ = pos.getZ() - 0.1 + random.nextDouble() * 1.2;
                    world.addParticle(MekanismParticleTypes.RADIATION.get(), randX, randY, randZ, 0, 0, 0);
                }
            }
        }
    }

    protected InteractionResult genericClientActivated(@Nonnull Player player, @Nonnull InteractionHand hand) {
        if (Attribute.has(this, AttributeGui.class) || MekanismUtils.canUseAsWrench(player.getItemInHand(hand))) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}