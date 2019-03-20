package mekanism.common.block;

import java.util.Random;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.IMekWrench;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.Mekanism;
import mekanism.common.Tier.BaseTier;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.base.ITierItem;
import mekanism.common.block.states.BlockStateBasic;
import mekanism.common.block.states.BlockStateBasic.BasicBlock;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.integration.wrenches.Wrenches;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.multiblock.IStructuralMultiblock;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionPort;
import mekanism.common.tile.TileEntityInductionProvider;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntitySuperheatingElement;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Block class for handling multiple metal block IDs. 0:0: Osmium Block 0:1: Bronze Block 0:2: Refined Obsidian 0:3:
 * Charcoal Block 0:4: Refined Glowstone 0:5: Steel Block 0:6: Bin 0:7: Teleporter Frame 0:8: Steel Casing 0:9: Dynamic
 * Tank 0:10: Structural Glass 0:11: Dynamic Valve 0:12: Copper Block 0:13: Tin Block 0:14: Thermal Evaporation
 * Controller 0:15: Thermal Evaporation Valve 1:0: Thermal Evaporation Block 1:1: Induction Casing 1:2: Induction Port
 * 1:3: Induction Cell 1:4: Induction Provider 1:5: Superheating Element 1:6: Pressure Disperser 1:7: Boiler Casing 1:8:
 * Boiler Valve 1:9: Security Desk
 *
 * @author AidanBrady
 */
public abstract class BlockBasic extends Block {

    public BlockBasic() {
        super(Material.IRON);
        setHardness(5F);
        setResistance(20F);
        setCreativeTab(Mekanism.tabMekanism);
    }

    public static BlockBasic getBlockBasic(BasicBlock block) {
        return new BlockBasic() {
            @Override
            public BasicBlock getBasicBlock() {
                return block;
            }
        };
    }

    public static boolean manageInventory(EntityPlayer player, TileEntityDynamicTank tileEntity, EnumHand hand,
          ItemStack itemStack) {
        if (tileEntity.structure == null) {
            return false;
        }

        ItemStack copyStack = StackUtils.size(itemStack, 1);

        if (FluidContainerUtils.isFluidContainer(itemStack)) {
            IFluidHandlerItem handler = FluidUtil.getFluidHandler(copyStack);

            if (FluidUtil.getFluidContained(copyStack) == null) {
                if (tileEntity.structure.fluidStored != null) {
                    int filled = handler.fill(tileEntity.structure.fluidStored, !player.capabilities.isCreativeMode);
                    copyStack = handler.getContainer();

                    if (filled > 0) {
                        if (player.capabilities.isCreativeMode) {
                            tileEntity.structure.fluidStored.amount -= filled;
                        } else if (itemStack.getCount() == 1) {
                            tileEntity.structure.fluidStored.amount -= filled;
                            player.setHeldItem(hand, copyStack);
                        } else if (itemStack.getCount() > 1 && player.inventory.addItemStackToInventory(copyStack)) {
                            tileEntity.structure.fluidStored.amount -= filled;
                            itemStack.shrink(1);
                        }

                        if (tileEntity.structure.fluidStored.amount == 0) {
                            tileEntity.structure.fluidStored = null;
                        }

                        return true;
                    }
                }
            } else {
                FluidStack itemFluid = FluidUtil.getFluidContained(copyStack);
                int stored = tileEntity.structure.fluidStored != null ? tileEntity.structure.fluidStored.amount : 0;
                int needed = (tileEntity.structure.volume * TankUpdateProtocol.FLUID_PER_TANK) - stored;

                if (tileEntity.structure.fluidStored != null && !tileEntity.structure.fluidStored
                      .isFluidEqual(itemFluid)) {
                    return false;
                }

                boolean filled = false;
                FluidStack drained = handler.drain(needed, !player.capabilities.isCreativeMode);
                copyStack = handler.getContainer();

                if (copyStack.getCount() == 0) {
                    copyStack = ItemStack.EMPTY;
                }

                if (drained != null) {
                    if (player.capabilities.isCreativeMode) {
                        filled = true;
                    } else {
                        if (!copyStack.isEmpty()) {
                            if (itemStack.getCount() == 1) {
                                player.setHeldItem(hand, copyStack);
                                filled = true;
                            } else {
                                if (player.inventory.addItemStackToInventory(copyStack)) {
                                    itemStack.shrink(1);

                                    filled = true;
                                }
                            }
                        } else {
                            itemStack.shrink(1);

                            if (itemStack.getCount() == 0) {
                                player.setHeldItem(hand, ItemStack.EMPTY);
                            }

                            filled = true;
                        }
                    }

                    if (filled) {
                        if (tileEntity.structure.fluidStored == null) {
                            tileEntity.structure.fluidStored = drained;
                        } else {
                            tileEntity.structure.fluidStored.amount += drained.amount;
                        }

                        return true;
                    }
                }
            }
        }

        return false;
    }

    public abstract BasicBlock getBasicBlock();

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateBasic(this, getTypeProperty());
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        BlockStateBasic.BasicBlockType type = BlockStateBasic.BasicBlockType.get(getBasicBlock(), meta & 0xF);

        return getDefaultState().withProperty(getTypeProperty(), type);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        BlockStateBasic.BasicBlockType type = state.getValue(getTypeProperty());
        return type.meta;
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity tile = MekanismUtils.getTileEntitySafe(worldIn, pos);

        if (tile instanceof TileEntityBasicBlock && ((TileEntityBasicBlock) tile).facing != null) {
            state = state.withProperty(BlockStateFacing.facingProperty, ((TileEntityBasicBlock) tile).facing);
        }

        if (tile instanceof IActiveState) {
            state = state.withProperty(BlockStateBasic.activeProperty, ((IActiveState) tile).getActive());
        }

        if (tile instanceof TileEntityInductionCell) {
            state = state
                  .withProperty(BlockStateBasic.tierProperty, ((TileEntityInductionCell) tile).tier.getBaseTier());
        }

        if (tile instanceof TileEntityInductionProvider) {
            state = state
                  .withProperty(BlockStateBasic.tierProperty, ((TileEntityInductionProvider) tile).tier.getBaseTier());
        }

        if (tile instanceof TileEntityBin) {
            state = state.withProperty(BlockStateBasic.tierProperty, ((TileEntityBin) tile).tier.getBaseTier());
        }

        if (tile instanceof TileEntityInductionPort) {
            state = state.withProperty(BlockStateBasic.activeProperty, ((TileEntityInductionPort) tile).mode);
        }

        if (tile instanceof TileEntitySuperheatingElement) {
            TileEntitySuperheatingElement element = (TileEntitySuperheatingElement) tile;
            boolean active = false;

            if (element.multiblockUUID != null
                  && SynchronizedBoilerData.clientHotMap.get(element.multiblockUUID) != null) {
                active = SynchronizedBoilerData.clientHotMap.get(element.multiblockUUID);
            }

            state = state.withProperty(BlockStateBasic.activeProperty, active);
        }

        return state;
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos) {
        if (!world.isRemote) {
            TileEntity tileEntity = new Coord4D(pos, world).getTileEntity(world);

            if (tileEntity instanceof IMultiblock) {
                ((IMultiblock<?>) tileEntity).doUpdate();
            }

            if (tileEntity instanceof TileEntityBasicBlock) {
                ((TileEntityBasicBlock) tileEntity).onNeighborChange(neighborBlock);
            }

            if (tileEntity instanceof IStructuralMultiblock) {
                ((IStructuralMultiblock) tileEntity).doUpdate();
            }
        }
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        IBlockState state = world.getBlockState(pos);
        BasicBlockType type = BasicBlockType.get(getBasicBlock(), state.getBlock().getMetaFromState(state));

        if (type == BasicBlockType.REFINED_OBSIDIAN) {
            return 4000F;
        }

        return blockResistance;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getBlock().getMetaFromState(state);
    }

    @Override
    public void getSubBlocks(CreativeTabs creativetabs, NonNullList<ItemStack> list) {
        for (BasicBlockType type : BasicBlockType.values()) {
            if (type.blockType == getBasicBlock()) {
                switch (type) {
                    case INDUCTION_CELL:
                    case INDUCTION_PROVIDER:
                    case BIN:
                        for (BaseTier tier : BaseTier.values()) {
                            if (type == BasicBlockType.BIN || tier.isObtainable()) {
                                ItemStack stack = new ItemStack(this, 1, type.meta);
                                ((ItemBlockBasic) stack.getItem()).setBaseTier(stack, tier);
                                list.add(stack);
                            }
                        }

                        break;
                    default:
                        list.add(new ItemStack(this, 1, type.meta));
                }
            }
        }
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
          SpawnPlacementType type) {
        int meta = state.getBlock().getMetaFromState(state);

        switch (getBasicBlock()) {
            case BASIC_BLOCK_1:
                switch (meta) {
                    case 10:
                        return false;
                    case 9:
                    case 11:
                        TileEntityDynamicTank tileEntity = (TileEntityDynamicTank) MekanismUtils
                              .getTileEntitySafe(world, pos);

                        if (tileEntity != null) {
                            if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
                                if (tileEntity.structure != null) {
                                    return false;
                                }
                            } else {
                                if (tileEntity.clientHasStructure) {
                                    return false;
                                }
                            }
                        }
                    default:
                        return super.canCreatureSpawn(state, world, pos, type);
                }
            case BASIC_BLOCK_2:
                switch (meta) {
                    case 1:
                    case 2:
                    case 7:
                    case 8:
                        TileEntityMultiblock<?> tileEntity = (TileEntityMultiblock<?>) MekanismUtils
                              .getTileEntitySafe(world, pos);

                        if (tileEntity != null) {
                            if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
                                if (tileEntity.structure != null) {
                                    return false;
                                }
                            } else {
                                if (tileEntity.clientHasStructure) {
                                    return false;
                                }
                            }
                        }
                    default:
                        return super.canCreatureSpawn(state, world, pos, type);
                }
            default:
                return super.canCreatureSpawn(state, world, pos, type);
        }
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        BasicBlockType type = BasicBlockType.get(world.getBlockState(pos));

        if (!world.isRemote && type == BasicBlockType.BIN) {
            TileEntityBin bin = (TileEntityBin) world.getTileEntity(pos);
            RayTraceResult mop = MekanismUtils.rayTrace(world, player);

            if (mop != null && mop.sideHit == bin.facing) {
                if (!bin.bottomStack.isEmpty()) {
                    ItemStack stack;
                    if (player.isSneaking()) {
                        stack = bin.remove(1).copy();
                    } else {
                        stack = bin.removeStack().copy();
                    }

                    if (!player.inventory.addItemStackToInventory(stack)) {
                        BlockPos dropPos = pos.offset(bin.facing);
                        Entity item = new EntityItem(world, dropPos.getX() + .5f, dropPos.getY() + .3f,
                              dropPos.getZ() + .5f, stack);
                        item.addVelocity(-item.motionX, -item.motionY, -item.motionZ);
                        world.spawnEntity(item);
                    } else {
                        world.playSound(null, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f,
                              SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F,
                              ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    }
                }
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer,
          EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        BasicBlockType type = BasicBlockType.get(state);
        TileEntity tile = world.getTileEntity(pos);
        ItemStack stack = entityplayer.getHeldItem(hand);

        if (type == BasicBlockType.REFINED_OBSIDIAN) {
            if (entityplayer.isSneaking()) {
                entityplayer.openGui(Mekanism.instance, 1, world, pos.getX(), pos.getY(), pos.getZ());
                return true;
            }
        }

        if (tile instanceof TileEntityThermalEvaporationController) {
            if (!entityplayer.isSneaking()) {
                if (!world.isRemote) {
                    entityplayer.openGui(Mekanism.instance, 33, world, pos.getX(), pos.getY(), pos.getZ());
                }

                return true;
            }
        } else if (tile instanceof TileEntitySecurityDesk) {
            UUID ownerUUID = ((TileEntitySecurityDesk) tile).ownerUUID;

            if (!entityplayer.isSneaking()) {
                if (!world.isRemote) {
                    if (ownerUUID == null || entityplayer.getUniqueID().equals(ownerUUID)) {
                        entityplayer.openGui(Mekanism.instance, 57, world, pos.getX(), pos.getY(), pos.getZ());
                    } else {
                        SecurityUtils.displayNoAccess(entityplayer);
                    }
                }

                return true;
            }
        } else if (tile instanceof TileEntityBin) {
            TileEntityBin bin = (TileEntityBin) tile;

            IMekWrench wrenchHandler;

            if (!stack.isEmpty() && (wrenchHandler = Wrenches.getHandler(stack)) != null) {
                RayTraceResult raytrace = new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos);
                if (wrenchHandler.canUseWrench(entityplayer, hand, stack, raytrace)) {
                    if (!world.isRemote) {
                        wrenchHandler.wrenchUsed(entityplayer, hand, stack, raytrace);

                        if (entityplayer.isSneaking()) {
                            dismantleBlock(state, world, pos, false);
                            return true;
                        }

                        int change = bin.facing.rotateY().ordinal();

                        bin.setFacing((short) change);
                        world.notifyNeighborsOfStateChange(pos, this, true);
                    }

                    return true;
                }
            }

            if (!world.isRemote) {
                if (bin.getItemCount() < bin.tier.storage) {
                    if (bin.addTicks == 0) {
                        if (!stack.isEmpty()) {
                            ItemStack remain = bin.add(stack);
                            entityplayer.setHeldItem(hand, remain);
                            bin.addTicks = 5;
                        }
                    } else if (bin.addTicks > 0 && bin.getItemCount() > 0) {
                        NonNullList<ItemStack> inv = entityplayer.inventory.mainInventory;

                        for (int i = 0; i < inv.size(); i++) {
                            if (bin.getItemCount() == bin.tier.storage) {
                                break;
                            }

                            if (!inv.get(i).isEmpty()) {
                                ItemStack remain = bin.add(inv.get(i));
                                inv.set(i, remain);
                                bin.addTicks = 5;
                            }

                            ((EntityPlayerMP) entityplayer).sendContainerToPlayer(entityplayer.openContainer);
                        }
                    }
                }
            }

            return true;
        } else if (tile instanceof IMultiblock) {
            if (world.isRemote) {
                return true;
            }

            return ((IMultiblock<?>) world.getTileEntity(pos)).onActivate(entityplayer, hand, stack);
        } else if (tile instanceof IStructuralMultiblock) {
            if (world.isRemote) {
                return true;
            }

            return ((IStructuralMultiblock) world.getTileEntity(pos)).onActivate(entityplayer, hand, stack);
        }

        return false;
    }

    @Override
    @Deprecated
    public boolean isSideSolid(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side) {
        return BasicBlockType.get(state) != BasicBlockType.STRUCTURAL_GLASS;
    }

    @SideOnly(Side.CLIENT)
    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Nonnull
    @Override
    @Deprecated
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tileEntity = MekanismUtils.getTileEntitySafe(world, pos);
        int metadata = state.getBlock().getMetaFromState(state);

        if (tileEntity instanceof IActiveState) {
            if (((IActiveState) tileEntity).getActive() && ((IActiveState) tileEntity).lightUpdate()) {
                return 15;
            }
        }

        if (getBasicBlock() == BasicBlock.BASIC_BLOCK_1) {
            switch (metadata) {
                case 2:
                    return 8;
                case 4:
                    return 15;
                case 7:
                    return 12;
            }
        } else if (getBasicBlock() == BasicBlock.BASIC_BLOCK_2) {
            if (metadata == 5 && tileEntity instanceof TileEntitySuperheatingElement) {
                TileEntitySuperheatingElement element = (TileEntitySuperheatingElement) tileEntity;

                if (element.multiblockUUID != null
                      && SynchronizedBoilerData.clientHotMap.get(element.multiblockUUID) != null) {
                    return SynchronizedBoilerData.clientHotMap.get(element.multiblockUUID) ? 15 : 0;
                }

                return 0;
            }
        }

        return 0;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        BasicBlockType type = BasicBlockType.get(state);

        return type != null && type.tileEntityClass != null;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        if (BasicBlockType.get(state) == null) {
            return null;
        }

        return BasicBlockType.get(state).create();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer,
          ItemStack stack) {
        if (world.getTileEntity(pos) instanceof TileEntityBasicBlock) {
            TileEntityBasicBlock tileEntity = (TileEntityBasicBlock) world.getTileEntity(pos);
            int side = MathHelper.floor((placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            int height = Math.round(placer.rotationPitch);
            int change = 3;

            if (tileEntity.canSetFacing(0) && tileEntity.canSetFacing(1)) {
                if (height >= 65) {
                    change = 1;
                } else if (height <= -65) {
                    change = 0;
                }
            }

            if (change != 0 && change != 1) {
                switch (side) {
                    case 0:
                        change = 2;
                        break;
                    case 1:
                        change = 5;
                        break;
                    case 2:
                        change = 3;
                        break;
                    case 3:
                        change = 4;
                        break;
                }
            }

            tileEntity.setFacing((short) change);
            tileEntity.redstone = world.getRedstonePowerFromNeighbors(pos) > 0;

            if (tileEntity instanceof TileEntitySecurityDesk) {
                ((TileEntitySecurityDesk) tileEntity).ownerUUID = placer.getUniqueID();
            }

            if (tileEntity instanceof IBoundingBlock) {
                ((IBoundingBlock) tileEntity).onPlace();
            }
        }

        world.markBlockRangeForRenderUpdate(pos, pos.add(1, 1, 1));
        world.checkLightFor(EnumSkyBlock.BLOCK, pos);
        world.checkLightFor(EnumSkyBlock.SKY, pos);

        if (!world.isRemote && world.getTileEntity(pos) != null) {
            TileEntity tileEntity = world.getTileEntity(pos);

            if (tileEntity instanceof IMultiblock) {
                ((IMultiblock<?>) tileEntity).doUpdate();
            }

            if (tileEntity instanceof IStructuralMultiblock) {
                ((IStructuralMultiblock) tileEntity).doUpdate();
            }
        }
    }

    @Override
    public void breakBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntity tileEntity = world.getTileEntity(pos);

        if (tileEntity instanceof IBoundingBlock) {
            ((IBoundingBlock) tileEntity).onBreak();
        }

        super.breakBlock(world, pos, state);
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world,
          @Nonnull BlockPos pos, EntityPlayer player) {
        BasicBlockType type = BasicBlockType.get(state);
        ItemStack ret = new ItemStack(this, 1, state.getBlock().getMetaFromState(state));

        if (type == BasicBlockType.BIN) {
            TileEntityBin tileEntity = (TileEntityBin) world.getTileEntity(pos);
            InventoryBin inv = new InventoryBin(ret);

            ((ITierItem) ret.getItem()).setBaseTier(ret, tileEntity.tier.getBaseTier());
            inv.setItemCount(tileEntity.getItemCount());

            if (tileEntity.getItemCount() > 0) {
                inv.setItemType(tileEntity.itemType);
            }
        } else if (type == BasicBlockType.INDUCTION_CELL) {
            TileEntityInductionCell tileEntity = (TileEntityInductionCell) world.getTileEntity(pos);
            ((ItemBlockBasic) ret.getItem()).setBaseTier(ret, tileEntity.tier.getBaseTier());
        } else if (type == BasicBlockType.INDUCTION_PROVIDER) {
            TileEntityInductionProvider tileEntity = (TileEntityInductionProvider) world.getTileEntity(pos);
            ((ItemBlockBasic) ret.getItem()).setBaseTier(ret, tileEntity.tier.getBaseTier());
        }

        TileEntity tileEntity = world.getTileEntity(pos);

        if (tileEntity instanceof IStrictEnergyStorage) {
            IEnergizedItem energizedItem = (IEnergizedItem) ret.getItem();
            energizedItem.setEnergy(ret, ((IStrictEnergyStorage) tileEntity).getEnergy());
        }

        return ret;
    }

    @Nonnull
    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Items.AIR;
    }

    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, World world, @Nonnull BlockPos pos,
          @Nonnull EntityPlayer player, boolean willHarvest) {
        if (!player.capabilities.isCreativeMode && !world.isRemote && willHarvest) {
            float motion = 0.7F;
            double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

            EntityItem entityItem = new EntityItem(world, pos.getX() + motionX, pos.getY() + motionY,
                  pos.getZ() + motionZ, getPickBlock(state, null, world, pos, player));

            world.spawnEntity(entityItem);
        }

        return world.setBlockToAir(pos);
    }

    public ItemStack dismantleBlock(IBlockState state, World world, BlockPos pos, boolean returnBlock) {
        ItemStack itemStack = getPickBlock(state, null, world, pos, null);

        world.setBlockToAir(pos);

        if (!returnBlock) {
            float motion = 0.7F;
            double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

            EntityItem entityItem = new EntityItem(world, pos.getX() + motionX, pos.getY() + motionY,
                  pos.getZ() + motionZ, itemStack);

            world.spawnEntity(entityItem);
        }

        return itemStack;
    }

    @Override
    @Deprecated
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
          EnumFacing side) {
        if (BasicBlockType.get(state) == BasicBlockType.STRUCTURAL_GLASS
              && BasicBlockType.get(world.getBlockState(pos.offset(side))) == BasicBlockType.STRUCTURAL_GLASS) {
            return false;
        }

        return super.shouldSideBeRendered(state, world, pos, side);
    }

    @Override
    public EnumFacing[] getValidRotations(World world, @Nonnull BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        EnumFacing[] valid = new EnumFacing[6];

        if (tile instanceof TileEntityBasicBlock) {
            TileEntityBasicBlock basicTile = (TileEntityBasicBlock) tile;

            for (EnumFacing dir : EnumFacing.VALUES) {
                if (basicTile.canSetFacing(dir.ordinal())) {
                    valid[dir.ordinal()] = dir;
                }
            }
        }

        return valid;
    }

    @Override
    public boolean rotateBlock(World world, @Nonnull BlockPos pos, @Nonnull EnumFacing axis) {
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileEntityBasicBlock) {
            TileEntityBasicBlock basicTile = (TileEntityBasicBlock) tile;

            if (basicTile.canSetFacing(axis.ordinal())) {
                basicTile.setFacing((short) axis.ordinal());
                return true;
            }
        }

        return false;
    }

    public PropertyEnum<BasicBlockType> getTypeProperty() {
        return getBasicBlock().getProperty();
    }

    @Override
    public boolean isBeaconBase(IBlockAccess world, BlockPos pos, BlockPos beacon) {
        BasicBlockType basicBlockType = BasicBlockType.get(world.getBlockState(pos));
        return basicBlockType != null && basicBlockType.isBeaconBase;
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState blockState) {
        BasicBlockType basicBlockType = BasicBlockType.get(blockState);
        return basicBlockType != null && basicBlockType.hasRedstoneOutput;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
        BasicBlockType basicBlockType = BasicBlockType.get(blockState);
        if (basicBlockType != null && basicBlockType.hasRedstoneOutput) {
            TileEntity tile = worldIn.getTileEntity(pos);
            if (tile instanceof TileEntityBin) {
                return ((TileEntityBin) tile).getRedstoneLevel();
            }
        }
        return 0;
    }
}