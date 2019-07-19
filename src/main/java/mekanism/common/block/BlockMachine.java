package mekanism.common.block;

import java.util.Locale;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mekanism.api.IMekWrench;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFactory;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.base.ISustainedTank;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.block.states.BlockStateMachine.MachineBlock;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.block.states.BlockStateUtils;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.wrenches.Wrenches;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Block class for handling multiple machine block IDs. 0:0: Enrichment Chamber 0:1: Osmium Compressor 0:2: Combiner 0:3: Crusher 0:4: Digital Miner 0:5: Basic Factory
 * 0:6: Advanced Factory 0:7: Elite Factory 0:8: Metallurgic Infuser 0:9: Purification Chamber 0:10: Energized Smelter 0:11: Teleporter 0:12: Electric Pump 0:13: Electric
 * Chest 0:14: Chargepad 0:15: Logistical Sorter 1:0: Rotary Condensentrator 1:1: Chemical Oxidizer 1:2: Chemical Infuser 1:3: Chemical Injection Chamber 1:4:
 * Electrolytic Separator 1:5: Precision Sawmill 1:6: Chemical Dissolution Chamber 1:7: Chemical Washer 1:8: Chemical Crystallizer 1:9: Seismic Vibrator 1:10: Pressurized
 * Reaction Chamber 1:11: Fluid Tank 1:12: Fluidic Plenisher 1:13: Laser 1:14: Laser Amplifier 1:15: Laser Tractor Beam 2:0: Quantum Entangloporter 2:1: Solar Neutron
 * Activator 2:2: Ambient Accumulator 2:3: Oredictionificator 2:4: Resistive Heater 2:5: Formulaic Assemblicator 2:6: Fuelwood Heater
 *
 * @author AidanBrady
 */
public abstract class BlockMachine extends BlockMekanismContainer implements IBlockMekanism {
    //TODO: Clean this up further, currently only is used for Factories now

    private final Predicate<EnumFacing> facingPredicate;
    private final String name;

    public BlockMachine(String name) {
        this(name, BlockStateUtils.NO_ROTATION);
    }

    public BlockMachine(String name, Predicate<EnumFacing> facingPredicate) {
        super(Material.IRON);
        this.facingPredicate = facingPredicate;
        setHardness(3.5F);
        setResistance(16F);
        setCreativeTab(Mekanism.tabMekanism);
        //Ensure the name is lower case as with concatenating with values from enums it may not be
        this.name = name.toLowerCase(Locale.ROOT);
        setTranslationKey(this.name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.name));
    }

    @Override
    public String getDescription() {
        //TODO: Should name just be gotten from registry name
        return LangUtils.localize("tooltip.mekanism." + this.name);
    }

    @Override
    public boolean canRotateTo(EnumFacing side) {
        return facingPredicate.test(side);
    }

    @Override
    public boolean hasRotations() {
        return !facingPredicate.equals(BlockStateUtils.NO_ROTATION);
    }

    public abstract MachineBlock getMachineBlock();

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateMachine(this);
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
        if (tile instanceof TileEntityFactory) {
            state = state.withProperty(BlockStateMachine.recipeProperty, ((TileEntityFactory) tile).getRecipeType());
        }
        return state;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntityBasicBlock tileEntity = (TileEntityBasicBlock) world.getTileEntity(pos);
        if (tileEntity == null) {
            return;
        }

        EnumFacing change = EnumFacing.SOUTH;
        if (tileEntity.canSetFacing(EnumFacing.DOWN) && tileEntity.canSetFacing(EnumFacing.UP)) {
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

        tileEntity.setFacing(change);
        tileEntity.redstone = world.getRedstonePowerFromNeighbors(pos) > 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        TileEntityBasicBlock tileEntity = (TileEntityBasicBlock) world.getTileEntity(pos);
        if (MekanismUtils.isActive(world, pos) && ((IActiveState) tileEntity).renderUpdate() && MekanismConfig.current().client.machineEffects.val()) {
            float xRandom = (float) pos.getX() + 0.5F;
            float yRandom = (float) pos.getY() + 0.0F + random.nextFloat() * 6.0F / 16.0F;
            float zRandom = (float) pos.getZ() + 0.5F;
            float iRandom = 0.52F;
            float jRandom = random.nextFloat() * 0.6F - 0.3F;
            EnumFacing side = tileEntity.facing;

            switch (side) {
                case WEST:
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, xRandom - iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, xRandom - iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    break;
                case EAST:
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, xRandom + iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, xRandom + iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    break;
                case NORTH:
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, xRandom + jRandom, yRandom, zRandom - iRandom, 0.0D, 0.0D, 0.0D);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, xRandom + jRandom, yRandom, zRandom - iRandom, 0.0D, 0.0D, 0.0D);
                    break;
                case SOUTH:
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, xRandom + jRandom, yRandom, zRandom + iRandom, 0.0D, 0.0D, 0.0D);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, xRandom + jRandom, yRandom, zRandom + iRandom, 0.0D, 0.0D, 0.0D);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (MekanismConfig.current().client.enableAmbientLighting.val()) {
            TileEntity tileEntity = MekanismUtils.getTileEntitySafe(world, pos);
            if (tileEntity instanceof IActiveState && ((IActiveState) tileEntity).lightUpdate() && ((IActiveState) tileEntity).wasActiveRecently()) {
                return MekanismConfig.current().client.ambientLightingLevel.val();
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
                    default:
                        list.add(new ItemStack(this, 1, type.meta));
                }
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
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
                            MekanismUtils.dismantleBlock(this, state, world, pos);
                            return true;
                        }
                        if (tileEntity != null) {
                            EnumFacing change = tileEntity.facing.rotateY();
                            tileEntity.setFacing(change);
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
            if (!entityplayer.isSneaking() && type.guiId != -1) {
                if (SecurityUtils.canAccess(entityplayer, tileEntity)) {
                    entityplayer.openGui(Mekanism.instance, type.guiId, world, pos.getX(), pos.getY(), pos.getZ());
                } else {
                    SecurityUtils.displayNoAccess(entityplayer);
                }
                return true;
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

    @SideOnly(Side.CLIENT)
    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(IBlockState state, @Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return SecurityUtils.canAccess(player, tile) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        IBlockState state = world.getBlockState(pos);
        if (MachineType.get(getMachineBlock(), state.getBlock().getMetaFromState(state)) != MachineType.PERSONAL_CHEST) {
            //TODO: Should this be divided by 5 like in original block class
            return blockResistance;
        }
        return -1;
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
        if (tileEntity instanceof IComparatorSupport) {
            return ((IComparatorSupport) tileEntity).getRedstoneLevel();
        }
        return 0;
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityBasicBlock) {
                ((TileEntityBasicBlock) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Nonnull
    @Override
    protected ItemStack getDropItem(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileEntityBasicBlock tileEntity = (TileEntityBasicBlock) world.getTileEntity(pos);
        ItemStack itemStack = new ItemStack(this, 1, state.getBlock().getMetaFromState(state));
        if (itemStack.getTagCompound() == null) {
            itemStack.setTagCompound(new NBTTagCompound());
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
        if (tileEntity instanceof TileEntityContainerBlock && ((TileEntityContainerBlock) tileEntity).inventory.size() > 0) {
            ISustainedInventory inventory = (ISustainedInventory) itemStack.getItem();
            inventory.setInventory(((ISustainedInventory) tileEntity).getInventory(), itemStack);
        }
        if (((ISustainedTank) itemStack.getItem()).hasTank(itemStack)) {
            if (tileEntity instanceof ISustainedTank) {
                if (((ISustainedTank) tileEntity).getFluidStack() != null) {
                    ((ISustainedTank) itemStack.getItem()).setFluidStack(((ISustainedTank) tileEntity).getFluidStack(), itemStack);
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
        return itemStack;
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return false;
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
                if (basicTile.canSetFacing(dir)) {
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
            if (basicTile.canSetFacing(axis)) {
                basicTile.setFacing(axis);
                return true;
            }
        }
        return false;
    }
}