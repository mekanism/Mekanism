package mekanism.common.block;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import mekanism.api.DataHandlerUtils;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeHasBounding;
import mekanism.common.block.attribute.AttributeMultiblock;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.Attributes.AttributeComparator;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.radiation.Meltdown.MeltdownExplosion;
import mekanism.common.network.to_client.PacketSecurityUpdate;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.tile.interfaces.IComparatorSupport;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.interfaces.ISustainedInventory;
import mekanism.common.tile.interfaces.ITileRadioactive;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
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
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BlockMekanism extends Block {

    protected BlockMekanism(BlockBehaviour.Properties properties) {
        super(BlockStateHelper.applyLightLevelAdjustments(properties));
        registerDefaultState(BlockStateHelper.getDefaultState(stateDefinition.any()));
    }

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(RenderPropertiesProvider.particles());
    }

    @NotNull
    @Override
    @Deprecated
    public PushReaction getPistonPushReaction(@NotNull BlockState state) {
        if (state.hasBlockEntity()) {
            //Protect against mods like Quark that allow blocks with TEs to be moved
            //TODO: Eventually it would be nice to go through this and maybe even allow some TEs to be moved if they don't strongly
            // care about the world, but for now it is safer to just block them from being moved
            return PushReaction.BLOCK;
        }
        return super.getPistonPushReaction(state);
    }

    @NotNull
    @Override
    public ItemStack getCloneItemStack(@NotNull BlockState state, HitResult target, @NotNull BlockGetter world, @NotNull BlockPos pos, Player player) {
        ItemStack itemStack = new ItemStack(this);
        TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return itemStack;
        }
        //TODO: Some of the data doesn't get properly "picked", because there are cases such as before opening the GUI where
        // the server doesn't bother syncing the data to the client. For example with what frequencies there are
        Item item = itemStack.getItem();
        Lazy<CompoundTag> lazyDataMap = Lazy.of(() -> ItemDataUtils.getDataMap(itemStack));
        if (tile.getFrequencyComponent().hasCustomFrequencies()) {
            tile.getFrequencyComponent().write(lazyDataMap.get());
        }
        if (tile.hasSecurity()) {
            itemStack.getCapability(Capabilities.OWNER_OBJECT).ifPresent(ownerObject -> {
                ownerObject.setOwnerUUID(tile.getOwnerUUID());
                itemStack.getCapability(Capabilities.SECURITY_OBJECT).ifPresent(securityObject -> securityObject.setSecurityMode(tile.getSecurityMode()));
            });
        }
        if (tile.supportsUpgrades()) {
            tile.getComponent().write(lazyDataMap.get());
        }
        if (tile instanceof ISideConfiguration config) {
            CompoundTag dataMap = lazyDataMap.get();
            config.getConfig().write(dataMap);
            config.getEjector().write(dataMap);
        }
        if (tile instanceof ISustainedData sustainedData) {
            sustainedData.writeSustainedData(lazyDataMap.get());
        }
        if (tile.supportsRedstone()) {
            NBTUtils.writeEnum(lazyDataMap.get(), NBTConstants.CONTROL_TYPE, tile.getControlType());
        }
        for (SubstanceType type : EnumUtils.SUBSTANCES) {
            if (tile.handles(type)) {
                lazyDataMap.get().put(type.getContainerTag(), DataHandlerUtils.writeContainers(type.getContainers(tile)));
            }
        }
        if (item instanceof ISustainedInventory sustainedInventory && tile.persistInventory() && tile.getSlots() > 0) {
            sustainedInventory.setInventory(tile.getInventory(), itemStack);
        }
        return itemStack;
    }

    @NotNull
    @Override
    @Deprecated
    public List<ItemStack> getDrops(@NotNull BlockState state, @NotNull LootContext.Builder builder) {
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
                                //If the list is now empty remove it; otherwise, update the list
                                ItemDataUtils.setListOrRemove(drop, NBTConstants.GAS_TANKS, newGasTankList);
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
    public boolean triggerEvent(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, int id, int param) {
        boolean triggered = super.triggerEvent(state, level, pos, id, param);
        if (this instanceof IHasTileEntity<?> hasTileEntity) {
            return hasTileEntity.triggerBlockEntityEvent(state, level, pos, id, param);
        }
        return triggered;
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        BlockStateHelper.fillBlockStateContainer(this, builder);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return BlockStateHelper.getStateForPlacement(this, super.getStateForPlacement(context), context);
    }

    @NotNull
    @Override
    @Deprecated
    public FluidState getFluidState(BlockState state) {
        if (state.getBlock() instanceof IStateFluidLoggable fluidLoggable) {
            return fluidLoggable.getFluid(state);
        }
        return super.getFluidState(state);
    }

    @NotNull
    @Override
    @Deprecated
    public BlockState updateShape(BlockState state, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor world, @NotNull BlockPos currentPos,
          @NotNull BlockPos facingPos) {
        if (state.getBlock() instanceof IStateFluidLoggable fluidLoggable) {
            fluidLoggable.updateFluids(state, world, currentPos);
        }
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    @Deprecated
    public void onRemove(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            AttributeHasBounding hasBounding = Attribute.get(state, AttributeHasBounding.class);
            if (hasBounding != null) {
                hasBounding.removeBoundingBlocks(world, pos, state);
            }
        }
        if (state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity())) {
            TileEntityUpdateable tile = WorldUtils.getTileEntity(TileEntityUpdateable.class, world, pos);
            if (tile != null) {
                tile.blockRemoved();
            }
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public void setPlacedBy(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        AttributeHasBounding hasBounding = Attribute.get(state, AttributeHasBounding.class);
        if (hasBounding != null) {
            hasBounding.placeBoundingBlocks(world, pos, state);
        }
        TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return;
        }
        if (tile.supportsRedstone()) {
            tile.updatePower();
        }
        // Check if the stack has a custom name, and if the tile supports naming, name it
        if (tile.isNameable() && stack.hasCustomHoverName()) {
            tile.setCustomName(stack.getHoverName());
        }

        //Handle item
        Item item = stack.getItem();
        CompoundTag dataMap = ItemDataUtils.getDataMapIfPresent(stack);
        if (dataMap == null) {
            //Don't bother modifying the stack even though it doesn't matter as it is going away but return an empty compound
            // the same as we would normally do if we had to add the data map
            dataMap = new CompoundTag();
        }
        setTileData(world, pos, state, placer, stack, tile);

        //TODO - 1.18: Re-evaluate the entirety of this method and see what parts potentially should not be getting called at all when on the client side.
        // We previously had issues in readSustainedData regarding frequencies when on the client side so that is why the frequency data has this check
        // but there is a good chance a lot of this stuff has no real reason to need to be set on the client side at all
        if (!world.isClientSide && tile.getFrequencyComponent().hasCustomFrequencies()) {
            tile.getFrequencyComponent().read(dataMap);
        }
        if (tile.hasSecurity()) {
            stack.getCapability(Capabilities.SECURITY_OBJECT).ifPresent(security -> tile.setSecurityMode(security.getSecurityMode()));
            UUID ownerUUID = MekanismAPI.getSecurityUtils().getOwnerUUID(stack);
            if (ownerUUID != null) {
                tile.setOwnerUUID(ownerUUID);
            } else if (placer != null) {
                tile.setOwnerUUID(placer.getUUID());
                if (!world.isClientSide) {
                    //If the machine doesn't already have an owner, make sure we portray this
                    Mekanism.packetHandler().sendToAll(new PacketSecurityUpdate(placer.getUUID()));
                }
            }
        }
        if (tile.supportsUpgrades()) {
            //The read method validates that data is stored
            tile.getComponent().read(dataMap);
        }
        if (tile instanceof ISideConfiguration config) {
            //The read methods validate that data is stored
            config.getConfig().read(dataMap);
            config.getEjector().read(dataMap);
        }
        for (SubstanceType type : EnumUtils.SUBSTANCES) {
            if (type.canHandle(tile)) {
                DataHandlerUtils.readContainers(type.getContainers(tile), dataMap.getList(type.getContainerTag(), Tag.TAG_COMPOUND));
            }
        }
        if (tile instanceof ISustainedData sustainedData && stack.hasTag()) {
            //TODO - 1.18: do we want to be checking it has a tag or not so that we can set things to stuff
            sustainedData.readSustainedData(dataMap);
        }
        if (tile.supportsRedstone()) {
            NBTUtils.setEnumIfPresent(dataMap, NBTConstants.CONTROL_TYPE, RedstoneControl::byIndexStatic, tile::setControlType);
        }
        if (item instanceof ISustainedInventory sustainedInventory && tile.persistInventory()) {
            tile.setInventory(sustainedInventory.getInventory(stack));
        }
    }

    //Method to override for setting some simple tile specific stuff
    public void setTileData(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack, TileEntityMekanism tile) {
    }

    @Override
    public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion) {
        if (!world.isClientSide) {
            AttributeMultiblock multiblockAttribute = Attribute.get(state, AttributeMultiblock.class);
            if (multiblockAttribute != null && explosion instanceof MeltdownExplosion meltdown) {
                MultiblockData multiblock = multiblockAttribute.getMultiblock(world, pos, meltdown.getMultiblockID());
                if (multiblock != null) {
                    multiblock.meltdownHappened(world);
                }
            }
        }
        super.onBlockExploded(state, world, pos, explosion);
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation) {
        return AttributeStateFacing.rotate(state, world, pos, rotation);
    }

    @NotNull
    @Override
    @Deprecated
    public BlockState rotate(@NotNull BlockState state, @NotNull Rotation rotation) {
        return AttributeStateFacing.rotate(state, rotation);
    }

    @NotNull
    @Override
    @Deprecated
    public BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror) {
        return AttributeStateFacing.mirror(state, mirror);
    }

    @Override
    @Deprecated
    public void onPlace(BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
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
    public boolean hasAnalogOutputSignal(@NotNull BlockState blockState) {
        return Attribute.has(this, AttributeComparator.class);
    }

    @Override
    @Deprecated
    public int getAnalogOutputSignal(@NotNull BlockState blockState, @NotNull Level world, @NotNull BlockPos pos) {
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
    public float getDestroyProgress(@NotNull BlockState state, @NotNull Player player, @NotNull BlockGetter world, @NotNull BlockPos pos) {
        return getDestroyProgress(state, player, world, pos, state.hasBlockEntity() ? WorldUtils.getTileEntity(world, pos) : null);
    }

    /**
     * Like {@link BlockBehaviour#getDestroyProgress(BlockState, Player, BlockGetter, BlockPos)} except also passes the tile to only have to get it once.
     */
    protected float getDestroyProgress(@NotNull BlockState state, @NotNull Player player, @NotNull BlockGetter world, @NotNull BlockPos pos,
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
    public void animateTick(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull RandomSource random) {
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

    protected InteractionResult genericClientActivated(@NotNull Player player, @NotNull InteractionHand hand) {
        if (Attribute.has(this, AttributeGui.class) || MekanismUtils.canUseAsWrench(player.getItemInHand(hand))) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}