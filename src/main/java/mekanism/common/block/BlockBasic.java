package mekanism.common.block;

import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.IMekWrench;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.base.ITierItem;
import mekanism.common.block.PortalHelper.BlockPortalOverride;
import mekanism.common.block.states.BlockStateBasic;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.integration.wrenches.Wrenches;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.multiblock.IStructuralMultiblock;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionPort;
import mekanism.common.tile.TileEntityInductionProvider;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntitySuperheatingElement;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Block class for handling multiple metal block IDs. 0:0: Osmium Block 0:1: Bronze Block 0:2: Refined Obsidian 0:3: Charcoal Block 0:4: Refined Glowstone 0:5: Steel
 * Block 0:6: Bin 0:7: Teleporter Frame 0:8: Steel Casing 0:9: Dynamic Tank 0:10: Structural Glass 0:11: Dynamic Valve 0:12: Copper Block 0:13: Tin Block 0:14: Thermal
 * Evaporation Controller 0:15: Thermal Evaporation Valve 1:0: Thermal Evaporation Block 1:1: Induction Casing 1:2: Induction Port 1:3: Induction Cell 1:4: Induction
 * Provider 1:5: Superheating Element 1:6: Pressure Disperser 1:7: Boiler Casing 1:8: Boiler Valve 1:9: Security Desk
 *
 * @author AidanBrady
 */
public class BlockBasic extends BlockTileDrops implements IBlockMekanism {

    public BlockBasic(String name) {
        super(Material.IRON);
        setHardness(5F);
        setResistance(10F);
        setCreativeTab(Mekanism.tabMekanism);
        //Ensure the name is lower case as with concatenating with values from enums it may not be
        name = name.toLowerCase(Locale.ROOT);
        setTranslationKey(name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, name));
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
        if (tile instanceof TileEntityInductionPort) {
            state = state.withProperty(BlockStateBasic.activeProperty, ((TileEntityInductionPort) tile).mode);
        }
        if (tile instanceof TileEntitySuperheatingElement) {
            TileEntitySuperheatingElement element = (TileEntitySuperheatingElement) tile;
            boolean active = false;
            if (element.multiblockUUID != null && SynchronizedBoilerData.clientHotMap.get(element.multiblockUUID) != null) {
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
            Block newBlock = world.getBlockState(fromPos).getBlock();
            if (BasicBlockType.get(state) == BasicBlockType.REFINED_OBSIDIAN && newBlock instanceof BlockFire) {
                BlockPortalOverride.instance.trySpawnPortal(world, fromPos);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
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
                            MekanismUtils.dismantleBlock(this, state, world, pos);
                            return true;
                        }
                        bin.setFacing(bin.facing.rotateY());
                        world.notifyNeighborsOfStateChange(pos, this, true);
                    }
                    return true;
                }
            }
            if (!world.isRemote) {
                if (bin.getItemCount() < bin.tier.getStorage()) {
                    if (bin.addTicks == 0) {
                        if (!stack.isEmpty()) {
                            ItemStack remain = bin.add(stack);
                            entityplayer.setHeldItem(hand, remain);
                            bin.addTicks = 5;
                        }
                    } else if (bin.addTicks > 0 && bin.getItemCount() > 0) {
                        NonNullList<ItemStack> inv = entityplayer.inventory.mainInventory;

                        for (int i = 0; i < inv.size(); i++) {
                            if (bin.getItemCount() == bin.tier.getStorage()) {
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
        //TODO: Figure out if this short circuit is good
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        BasicBlockType type = BasicBlockType.get(state);
        return type != null && type.isFullBlock;
    }

    @Override
    @Deprecated
    public boolean isFullBlock(IBlockState state) {
        BasicBlockType type = BasicBlockType.get(state);
        return type != null && type.isFullBlock;
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        BasicBlockType type = BasicBlockType.get(state);
        return type != null && type.isOpaqueCube;
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        BasicBlockType type = BasicBlockType.get(state);
        return type != null && type.isOpaqueCube ? 255 : 0;
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
        if (tileEntity instanceof IActiveState) {
            if (((IActiveState) tileEntity).getActive() && ((IActiveState) tileEntity).lightUpdate()) {
                return 15;
            }
        }
        return 0;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityBasicBlock) {
            TileEntityBasicBlock tileEntity = (TileEntityBasicBlock) te;
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

        if (!world.isRemote && te != null) {
            if (te instanceof IMultiblock) {
                ((IMultiblock<?>) te).doUpdate();
            }
            if (te instanceof IStructuralMultiblock) {
                ((IStructuralMultiblock) te).doUpdate();
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
    protected ItemStack getDropItem(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
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

    public PropertyEnum<BasicBlockType> getTypeProperty() {
        return getBasicBlock().getProperty();
    }
}
