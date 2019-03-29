package mekanism.common.block;

import java.util.Random;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.IMekWrench;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.Mekanism;
import mekanism.common.Tier.FluidTankTier;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.base.IFactory;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.base.ISustainedTank;
import mekanism.common.base.ITierItem;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.block.states.BlockStateMachine.MachineBlock;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.config.MekanismConfig.client;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.integration.wrenches.Wrenches;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.network.PacketLogisticalSorterGui.LogisticalSorterGuiMessage;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityLaser;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MultipartUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Block class for handling multiple machine block IDs. 0:0: Enrichment Chamber 0:1: Osmium Compressor 0:2: Combiner
 * 0:3: Crusher 0:4: Digital Miner 0:5: Basic Factory 0:6: Advanced Factory 0:7: Elite Factory 0:8: Metallurgic Infuser
 * 0:9: Purification Chamber 0:10: Energized Smelter 0:11: Teleporter 0:12: Electric Pump 0:13: Electric Chest 0:14:
 * Chargepad 0:15: Logistical Sorter 1:0: Rotary Condensentrator 1:1: Chemical Oxidizer 1:2: Chemical Infuser 1:3:
 * Chemical Injection Chamber 1:4: Electrolytic Separator 1:5: Precision Sawmill 1:6: Chemical Dissolution Chamber 1:7:
 * Chemical Washer 1:8: Chemical Crystallizer 1:9: Seismic Vibrator 1:10: Pressurized Reaction Chamber 1:11: Fluid Tank
 * 1:12: Fluidic Plenisher 1:13: Laser 1:14: Laser Amplifier 1:15: Laser Tractor Beam 2:0: Quantum Entangloporter 2:1:
 * Solar Neutron Activator 2:2: Ambient Accumulator 2:3: Oredictionificator 2:4: Resistive Heater 2:5: Formulaic
 * Assemblicator 2:6: Fuelwood Heater
 *
 * @author AidanBrady
 */
public abstract class BlockMachine extends BlockContainer {

    private static final AxisAlignedBB CHARGEPAD_BOUNDS = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.06F, 1.0F);
    private static final AxisAlignedBB TANK_BOUNDS = new AxisAlignedBB(0.125F, 0.0F, 0.125F, 0.875F, 1.0F, 0.875F);
    private static final AxisAlignedBB LASER_BOUNDS = new AxisAlignedBB(0.25F, 0.0F, 0.25F, 0.75F, 1.0F, 0.75F);
    private static final AxisAlignedBB LOGISTICAL_SORTER_BOUNDS = new AxisAlignedBB(0.125F, 0.0F, 0.125F, 0.875F, 1.0F,
          0.875F);

    public BlockMachine() {
        super(Material.IRON);
        setHardness(3.5F);
        setResistance(16F);
        setCreativeTab(Mekanism.tabMekanism);
    }

    public static BlockMachine getBlockMachine(MachineBlock block) {
        return new BlockMachine() {
            @Override
            public MachineBlock getMachineBlock() {
                return block;
            }
        };
    }

    public abstract MachineBlock getMachineBlock();

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateMachine(this, getTypeProperty());
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        MachineType type = MachineType.get(getMachineBlock(), meta & 0xF);

        return getDefaultState().withProperty(getTypeProperty(), type);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        MachineType type = state.getValue(getTypeProperty());
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
            state = state.withProperty(BlockStateMachine.activeProperty, ((IActiveState) tile).getActive());
        }

        if (tile instanceof TileEntityFluidTank) {
            state = state.withProperty(BlockStateMachine.tierProperty, ((TileEntityFluidTank) tile).tier.getBaseTier());
        }

        if (tile instanceof TileEntityFactory) {
            state = state.withProperty(BlockStateMachine.recipeProperty, ((TileEntityFactory) tile).getRecipeType());
        }

        return state;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer,
          ItemStack stack) {
        TileEntityBasicBlock tileEntity = (TileEntityBasicBlock) world.getTileEntity(pos);
        int side = MathHelper.floor((placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int height = Math.round(placer.rotationPitch);
        int change = 3;

        if (tileEntity == null) {
            return;
        }

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

        if (tileEntity instanceof TileEntityLogisticalSorter) {
            TileEntityLogisticalSorter transporter = (TileEntityLogisticalSorter) tileEntity;

            if (!transporter.hasInventory()) {
                for (EnumFacing dir : EnumFacing.VALUES) {
                    TileEntity tile = Coord4D.get(transporter).offset(dir).getTileEntity(world);

                    if (InventoryUtils.isItemHandler(tile, dir)) {
                        tileEntity.setFacing((short) dir.getOpposite().ordinal());
                        break;
                    }
                }
            }
        }

        if (tileEntity instanceof IBoundingBlock) {
            ((IBoundingBlock) tileEntity).onPlace();
        }
    }

    @Override
    public void breakBlock(World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntityBasicBlock tileEntity = (TileEntityBasicBlock) world.getTileEntity(pos);

        if (tileEntity instanceof IBoundingBlock) {
            ((IBoundingBlock) tileEntity).onBreak();
        }

        super.breakBlock(world, pos, state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        TileEntityBasicBlock tileEntity = (TileEntityBasicBlock) world.getTileEntity(pos);

        if (tileEntity instanceof TileEntityFluidTank) {
            return;
        }

        if (MekanismUtils.isActive(world, pos) && ((IActiveState) tileEntity).renderUpdate() && client.machineEffects) {
            float xRandom = (float) pos.getX() + 0.5F;
            float yRandom = (float) pos.getY() + 0.0F + random.nextFloat() * 6.0F / 16.0F;
            float zRandom = (float) pos.getZ() + 0.5F;
            float iRandom = 0.52F;
            float jRandom = random.nextFloat() * 0.6F - 0.3F;

            EnumFacing side = tileEntity.facing;

            if (tileEntity instanceof TileEntityMetallurgicInfuser) {
                side = side.getOpposite();
            }

            switch (side) {
                case WEST:
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (xRandom - iRandom), yRandom,
                          (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, (xRandom - iRandom), yRandom, (zRandom + jRandom),
                          0.0D, 0.0D, 0.0D);
                    break;
                case EAST:
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (xRandom + iRandom), yRandom,
                          (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, (xRandom + iRandom), yRandom, (zRandom + jRandom),
                          0.0D, 0.0D, 0.0D);
                    break;
                case NORTH:
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (xRandom + jRandom), yRandom,
                          (zRandom - iRandom), 0.0D, 0.0D, 0.0D);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, (xRandom + jRandom), yRandom, (zRandom - iRandom),
                          0.0D, 0.0D, 0.0D);
                    break;
                case SOUTH:
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (xRandom + jRandom), yRandom,
                          (zRandom + iRandom), 0.0D, 0.0D, 0.0D);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, (xRandom + jRandom), yRandom, (zRandom + iRandom),
                          0.0D, 0.0D, 0.0D);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (client.enableAmbientLighting) {
            TileEntity tileEntity = MekanismUtils.getTileEntitySafe(world, pos);
            if (tileEntity instanceof IActiveState &&
                  ((IActiveState) tileEntity).lightUpdate() &&
                  ((IActiveState) tileEntity).wasActiveRecently()) {
                return client.ambientLightingLevel;
            }
        }

        return 0;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getBlock().getMetaFromState(state);
    }

    @Override
    public void getSubBlocks(CreativeTabs creativetabs, NonNullList<ItemStack> list) {
        for (MachineType type : MachineType.getValidMachines()) {
            if (type.typeBlock == getMachineBlock() && type.isEnabled()) {
                switch (type) {
                    case BASIC_FACTORY:
                    case ADVANCED_FACTORY:
                    case ELITE_FACTORY:
                        for (RecipeType recipe : RecipeType.values()) {
                            if (recipe.getType().isEnabled()) {
                                ItemStack stack = new ItemStack(this, 1, type.meta);
                                ((IFactory) stack.getItem()).setRecipeType(recipe.ordinal(), stack);
                                list.add(stack);
                            }
                        }

                        break;
                    case FLUID_TANK:
                        ItemBlockMachine itemMachine = (ItemBlockMachine) Item.getItemFromBlock(this);

                        for (FluidTankTier tier : FluidTankTier.values()) {
                            ItemStack stack = new ItemStack(this, 1, type.meta);
                            itemMachine.setBaseTier(stack, tier.getBaseTier());
                            list.add(stack);
                        }

                        break;
                    default:
                        list.add(new ItemStack(this, 1, type.meta));
                }
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer,
          EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }

        TileEntityBasicBlock tileEntity = (TileEntityBasicBlock) world.getTileEntity(pos);
        int metadata = state.getBlock().getMetaFromState(state);
        ItemStack stack = entityplayer.getHeldItem(hand);

        if (!stack.isEmpty()) {
            IMekWrench wrenchHandler = Wrenches.getHandler(stack);
            if (wrenchHandler != null) {
                RayTraceResult raytrace = new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos);
                if (wrenchHandler.canUseWrench(entityplayer, hand, stack, raytrace)) {
                    if (SecurityUtils.canAccess(entityplayer, tileEntity)) {
                        wrenchHandler.wrenchUsed(entityplayer, hand, stack, raytrace);

                        if (entityplayer.isSneaking()) {
                            dismantleBlock(state, world, pos, false);

                            return true;
                        }

                        if (tileEntity != null) {
                            int change = tileEntity.facing.rotateY().ordinal();

                            if (tileEntity instanceof TileEntityLogisticalSorter) {
                                if (!((TileEntityLogisticalSorter) tileEntity).hasInventory()) {
                                    for (EnumFacing dir : EnumFacing.VALUES) {
                                        TileEntity tile = Coord4D.get(tileEntity).offset(dir).getTileEntity(world);

                                        if (InventoryUtils.isItemHandler(tile, dir)) {
                                            change = dir.getOpposite().ordinal();
                                            break;
                                        }
                                    }
                                }
                            }

                            tileEntity.setFacing((short) change);
                            world.notifyNeighborsOfStateChange(pos, this, true);
                        }
                    } else {
                        SecurityUtils.displayNoAccess(entityplayer);
                    }

                    return true;
                }
            }
        }

        if (tileEntity != null) {
            MachineType type = MachineType.get(getMachineBlock(), metadata);

            switch (type) {
                case PERSONAL_CHEST:
                    if (!entityplayer.isSneaking() && !world.isSideSolid(pos.up(), EnumFacing.DOWN)) {
                        TileEntityPersonalChest chest = (TileEntityPersonalChest) tileEntity;

                        if (SecurityUtils.canAccess(entityplayer, tileEntity)) {
                            MekanismUtils.openPersonalChestGui((EntityPlayerMP) entityplayer, chest, null, true);
                        } else {
                            SecurityUtils.displayNoAccess(entityplayer);
                        }

                        return true;
                    }

                    break;
                case FLUID_TANK:
                    if (!entityplayer.isSneaking()) {
                        if (SecurityUtils.canAccess(entityplayer, tileEntity)) {
                            if (!stack.isEmpty() && FluidContainerUtils.isFluidContainer(stack)) {
                                if (manageInventory(entityplayer, (TileEntityFluidTank) tileEntity, hand, stack)) {
                                    entityplayer.inventory.markDirty();
                                    return true;
                                }
                            } else {
                                entityplayer.openGui(Mekanism.instance, type.guiId, world, pos.getX(), pos.getY(),
                                      pos.getZ());
                            }
                        } else {
                            SecurityUtils.displayNoAccess(entityplayer);
                        }

                        return true;
                    }

                    break;
                case LOGISTICAL_SORTER:
                    if (!entityplayer.isSneaking()) {
                        if (SecurityUtils.canAccess(entityplayer, tileEntity)) {
                            LogisticalSorterGuiMessage
                                  .openServerGui(SorterGuiPacket.SERVER, 0, world, (EntityPlayerMP) entityplayer,
                                        Coord4D.get(tileEntity), -1);
                        } else {
                            SecurityUtils.displayNoAccess(entityplayer);
                        }

                        return true;
                    }

                    break;
                case TELEPORTER:
                case QUANTUM_ENTANGLOPORTER:
                    if (!entityplayer.isSneaking()) {
                        UUID owner = ((ISecurityTile) tileEntity).getSecurity().getOwnerUUID();

                        if (MekanismUtils.isOp(entityplayer) || owner == null || entityplayer.getUniqueID()
                              .equals(owner)) {
                            entityplayer
                                  .openGui(Mekanism.instance, type.guiId, world, pos.getX(), pos.getY(), pos.getZ());
                        } else {
                            SecurityUtils.displayNoAccess(entityplayer);
                        }

                        return true;
                    }

                    break;
                default:
                    if (!entityplayer.isSneaking() && type.guiId != -1) {
                        if (SecurityUtils.canAccess(entityplayer, tileEntity)) {
                            entityplayer
                                  .openGui(Mekanism.instance, type.guiId, world, pos.getX(), pos.getY(), pos.getZ());
                        } else {
                            SecurityUtils.displayNoAccess(entityplayer);
                        }

                        return true;
                    }

                    break;
            }
        }

        return false;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        int metadata = state.getBlock().getMetaFromState(state);

        if (MachineType.get(getMachineBlock(), metadata) == null) {
            return null;
        }

        return MachineType.get(getMachineBlock(), metadata).create();
    }

    @Override
    public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
        return null;
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Nonnull
    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Items.AIR;
    }

    @Nonnull
    @Override
    @Deprecated
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @SideOnly(Side.CLIENT)
    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(IBlockState state, @Nonnull EntityPlayer player, @Nonnull World world,
          @Nonnull BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);

        return SecurityUtils.canAccess(player, tile) ? super.getPlayerRelativeBlockHardness(state, player, world, pos)
              : 0.0F;
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        IBlockState state = world.getBlockState(pos);
        if (MachineType.get(getMachineBlock(), state.getBlock().getMetaFromState(state))
              != MachineType.PERSONAL_CHEST) {
            return blockResistance;
        } else {
            return -1;
        }
    }

    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, World world, @Nonnull BlockPos pos,
          @Nonnull EntityPlayer player, boolean willHarvest) {
        if (!player.capabilities.isCreativeMode && !world.isRemote && willHarvest) {
            TileEntityBasicBlock tileEntity = (TileEntityBasicBlock) world.getTileEntity(pos);

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

    @Override
    @Deprecated
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    @Deprecated
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);

        if (tileEntity instanceof TileEntityFluidTank) {
            return ((TileEntityFluidTank) tileEntity).getRedstoneLevel();
        }

        if (tileEntity instanceof TileEntityLaserAmplifier) {
            TileEntityLaserAmplifier amplifier = (TileEntityLaserAmplifier) tileEntity;

            if (amplifier.outputMode == TileEntityLaserAmplifier.RedstoneOutput.ENERGY_CONTENTS) {
                return amplifier.getRedstoneLevel();
            } else {
                return getWeakPower(state, world, pos, null);
            }
        }

        return 0;
    }

    private boolean manageInventory(EntityPlayer player, TileEntityFluidTank tileEntity, EnumHand hand,
          ItemStack itemStack) {
        ItemStack copyStack = StackUtils.size(itemStack.copy(), 1);

        if (FluidContainerUtils.isFluidContainer(itemStack)) {
            IFluidHandlerItem handler = FluidUtil.getFluidHandler(copyStack);

            if (FluidUtil.getFluidContained(copyStack) == null) {
                if (tileEntity.fluidTank.getFluid() != null) {
                    int filled = handler.fill(tileEntity.fluidTank.getFluid(), !player.capabilities.isCreativeMode);
                    copyStack = handler.getContainer();

                    if (filled > 0) {
                        if (itemStack.getCount() == 1) {
                            player.setHeldItem(hand, copyStack);
                        } else if (itemStack.getCount() > 1 && player.inventory.addItemStackToInventory(copyStack)) {
                            itemStack.shrink(1);
                        } else {
                            player.dropItem(copyStack, false, true);
                            itemStack.shrink(1);
                        }

                        if (tileEntity.tier != FluidTankTier.CREATIVE) {
                            tileEntity.fluidTank.drain(filled, true);
                        }

                        return true;
                    }
                }
            } else {
                FluidStack itemFluid = FluidUtil.getFluidContained(copyStack);
                int needed = tileEntity.getCurrentNeeded();

                if (tileEntity.fluidTank.getFluid() != null && !tileEntity.fluidTank.getFluid()
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
                        int toFill = tileEntity.fluidTank.getCapacity() - tileEntity.fluidTank.getFluidAmount();

                        if (tileEntity.tier != FluidTankTier.CREATIVE) {
                            toFill = Math.min(toFill, drained.amount);
                        }

                        tileEntity.fluidTank.fill(PipeUtils.copy(drained, toFill), true);

                        if (drained.amount - toFill > 0) {
                            tileEntity.pushUp(PipeUtils.copy(itemFluid, drained.amount - toFill), true);
                        }

                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock,
          BlockPos neighborPos) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);

            if (tileEntity instanceof TileEntityBasicBlock) {
                ((TileEntityBasicBlock) tileEntity).onNeighborChange(neighborBlock);
            }

            if (tileEntity instanceof TileEntityLogisticalSorter) {
                TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter) tileEntity;

                if (!sorter.hasInventory()) {
                    for (EnumFacing dir : EnumFacing.VALUES) {
                        TileEntity tile = Coord4D.get(tileEntity).offset(dir).getTileEntity(world);

                        if (InventoryUtils.isItemHandler(tile, dir)) {
                            sorter.setFacing((short) dir.getOpposite().ordinal());
                            return;
                        }
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world,
          @Nonnull BlockPos pos, EntityPlayer player) {
        TileEntityBasicBlock tileEntity = (TileEntityBasicBlock) world.getTileEntity(pos);
        ItemStack itemStack = new ItemStack(this, 1, state.getBlock().getMetaFromState(state));

        if (itemStack.getTagCompound() == null) {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        if (tileEntity instanceof TileEntityFluidTank) {
            ITierItem tierItem = (ITierItem) itemStack.getItem();
            tierItem.setBaseTier(itemStack, ((TileEntityFluidTank) tileEntity).tier.getBaseTier());
        }

        if (tileEntity instanceof ISecurityTile) {
            ISecurityItem securityItem = (ISecurityItem) itemStack.getItem();

            if (securityItem.hasSecurity(itemStack)) {
                securityItem.setOwnerUUID(itemStack, ((ISecurityTile) tileEntity).getSecurity().getOwnerUUID());
                securityItem.setSecurity(itemStack, ((ISecurityTile) tileEntity).getSecurity().getMode());
            }
        }

        if (tileEntity instanceof IUpgradeTile) {
            ((IUpgradeTile) tileEntity).getComponent().write(ItemDataUtils.getDataMap(itemStack));
        }

        if (tileEntity instanceof ISideConfiguration) {
            ISideConfiguration config = (ISideConfiguration) tileEntity;

            config.getConfig().write(ItemDataUtils.getDataMap(itemStack));
            config.getEjector().write(ItemDataUtils.getDataMap(itemStack));
        }

        if (tileEntity instanceof ISustainedData) {
            ((ISustainedData) tileEntity).writeSustainedData(itemStack);
        }

        if (tileEntity instanceof IRedstoneControl) {
            IRedstoneControl control = (IRedstoneControl) tileEntity;
            ItemDataUtils.setInt(itemStack, "controlType", control.getControlType().ordinal());
        }

        if (tileEntity instanceof TileEntityContainerBlock
              && ((TileEntityContainerBlock) tileEntity).inventory.size() > 0) {
            ISustainedInventory inventory = (ISustainedInventory) itemStack.getItem();
            inventory.setInventory(((ISustainedInventory) tileEntity).getInventory(), itemStack);
        }

        if (((ISustainedTank) itemStack.getItem()).hasTank(itemStack)) {
            if (tileEntity instanceof ISustainedTank) {
                if (((ISustainedTank) tileEntity).getFluidStack() != null) {
                    ((ISustainedTank) itemStack.getItem())
                          .setFluidStack(((ISustainedTank) tileEntity).getFluidStack(), itemStack);
                }
            }
        }

        if (tileEntity instanceof TileEntityFactory) {
            IFactory factoryItem = (IFactory) itemStack.getItem();
            factoryItem.setRecipeType(((TileEntityFactory) tileEntity).getRecipeType().ordinal(), itemStack);
        }

        //this MUST be done after the factory info is saved, as it caps the energy to max, which is based on the recipe type
        if (tileEntity instanceof IStrictEnergyStorage) {
            IEnergizedItem energizedItem = (IEnergizedItem) itemStack.getItem();
            energizedItem.setEnergy(itemStack, ((IStrictEnergyStorage) tileEntity).getEnergy());
        }

        if (tileEntity instanceof TileEntityQuantumEntangloporter) {
            InventoryFrequency frequency = ((TileEntityQuantumEntangloporter) tileEntity).frequency;

            if (frequency != null) {
                ItemDataUtils.setCompound(itemStack, "entangleporter_frequency", frequency.getIdentity().serialize());
            }
        }

        return itemStack;
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        MachineType type = MachineType.get(getMachineBlock(), state.getBlock().getMetaFromState(state));

        return type == MachineType.LASER_AMPLIFIER;
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

    @Nonnull
    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        MachineType type = MachineType.get(getMachineBlock(), state.getBlock().getMetaFromState(state));
        TileEntity tile = MekanismUtils.getTileEntitySafe(world, pos);

        if (type == null) {
            return super.getBoundingBox(state, world, pos);
        }

        switch (type) {
            case CHARGEPAD:
                return CHARGEPAD_BOUNDS;
            case FLUID_TANK:
                return TANK_BOUNDS;
            case LASER:
                if (tile instanceof TileEntityLaser) {
                    return MultipartUtils.rotate(LASER_BOUNDS.offset(-0.5, -0.5, -0.5), ((TileEntityLaser) tile).facing)
                          .offset(0.5, 0.5, 0.5);
                }
            case LOGISTICAL_SORTER:
                if (tile instanceof TileEntityLogisticalSorter) {
                    return MultipartUtils.rotate(LOGISTICAL_SORTER_BOUNDS.offset(-0.5, -0.5, -0.5),
                          ((TileEntityLogisticalSorter) tile).facing).offset(0.5, 0.5, 0.5);
                }
            default:
                return super.getBoundingBox(state, world, pos);
        }
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isSideSolid(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side) {
        MachineType type = MachineType.get(getMachineBlock(), state.getBlock().getMetaFromState(state));

        switch (type) {
            case CHARGEPAD:
            case PERSONAL_CHEST:
                return false;
            case FLUID_TANK:
                return side == EnumFacing.UP || side == EnumFacing.DOWN;
            default:
                return true;
        }
    }

    public PropertyEnum<MachineType> getTypeProperty() {
        return getMachineBlock().getProperty();
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

    @Override
    @Deprecated
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity tile = MekanismUtils.getTileEntitySafe(world, pos);

        if (tile instanceof TileEntityLaserAmplifier) {
            return ((TileEntityLaserAmplifier) tile).emittingRedstone ? 15 : 0;
        }

        return 0;
    }
}
