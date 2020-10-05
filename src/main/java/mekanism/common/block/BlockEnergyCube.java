package mekanism.common.block;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * Block class for handling multiple energy cube block IDs. 0: Basic Energy Cube 1: Advanced Energy Cube 2: Elite Energy Cube 3: Ultimate Energy Cube 4: Creative Energy
 * Cube
 *
 * @author AidanBrady
 */
public class BlockEnergyCube extends BlockTileModel<TileEntityEnergyCube, Machine<TileEntityEnergyCube>> {

    private static final VoxelShape[] bounds = new VoxelShape[256];

    static {
        VoxelShape frame = VoxelShapeUtils.combine(
              makeCuboidShape(0, 0, 0, 3, 3, 16),
              makeCuboidShape(0, 3, 0, 3, 16, 3),
              makeCuboidShape(0, 3, 13, 3, 16, 16),
              makeCuboidShape(0, 13, 3, 3, 16, 13),
              makeCuboidShape(3, 0, 0, 16, 3, 3),
              makeCuboidShape(3, 0, 13, 16, 3, 16),
              makeCuboidShape(3, 13, 0, 16, 16, 3),
              makeCuboidShape(3, 13, 13, 16, 16, 16),
              makeCuboidShape(13, 0, 3, 16, 3, 13),
              makeCuboidShape(13, 3, 0, 16, 13, 3),
              makeCuboidShape(13, 3, 13, 16, 13, 16),
              makeCuboidShape(13, 13, 3, 16, 16, 13),
              makeCuboidShape(12.5, 14.9, 7.5, 13.5, 15.9, 8.5),//ledTop1
              makeCuboidShape(2.5, 14.9, 7.5, 3.5, 15.9, 8.5),//ledTop2
              makeCuboidShape(12.5, 7.5, 0.1, 13.5, 8.5, 1.1),//ledBack1
              makeCuboidShape(2.5, 7.5, 0.1, 3.5, 8.5, 1.1),//ledBack2
              makeCuboidShape(2.5, 0.1, 7.5, 3.5, 1.1, 8.5),//ledBottom2
              makeCuboidShape(12.5, 0.1, 7.5, 13.5, 1.1, 8.5),//ledBottom1
              makeCuboidShape(12.5, 7.5, 14.9, 13.5, 8.5, 15.9),//ledFront1
              makeCuboidShape(2.5, 7.5, 14.9, 3.5, 8.5, 15.9),//ledFront2
              makeCuboidShape(0.1, 7.5, 2.5, 1.1, 8.5, 3.5),//ledRight2
              makeCuboidShape(0.1, 7.5, 12.5, 1.1, 8.5, 13.5),//ledRight1
              makeCuboidShape(14.9, 7.5, 2.5, 15.9, 8.5, 3.5),//ledLeft1
              makeCuboidShape(14.9, 7.5, 12.5, 15.9, 8.5, 13.5)//ledLeft2
        );
        VoxelShape frontPanel = VoxelShapeUtils.combine(
              makeCuboidShape(3, 5, 14, 13, 11, 15),//connectorFrontToggle
              makeCuboidShape(4, 4, 15, 12, 12, 16)//portFrontToggle
        );
        VoxelShape rightPanel = VoxelShapeUtils.combine(
              makeCuboidShape(1, 5, 3, 2, 11, 13),//connectorRightToggle
              makeCuboidShape(0, 4, 4, 1, 12, 12)//portRightToggle
        );
        VoxelShape leftPanel = VoxelShapeUtils.combine(
              makeCuboidShape(14, 5, 3, 15, 11, 13),//connectorLeftToggle
              makeCuboidShape(15, 4, 4, 16, 12, 12)//portLeftToggle
        );
        VoxelShape backPanel = VoxelShapeUtils.combine(
              makeCuboidShape(3, 5, 1, 13, 11, 2),//connectorBackToggle
              makeCuboidShape(4, 4, 0, 12, 12, 1)//portBackToggle
        );
        VoxelShape topPanel = VoxelShapeUtils.combine(
              makeCuboidShape(3, 14, 5, 13, 15, 11),//connectorTopToggle
              makeCuboidShape(4, 15, 4, 12, 16, 12)//portTopToggle
        );
        VoxelShape bottomPanel = VoxelShapeUtils.combine(
              makeCuboidShape(3, 1, 5, 13, 2, 11),//connectorBottomToggle
              makeCuboidShape(4, 0, 4, 12, 1, 12)//portBottomToggle
        );
        VoxelShape frameRotated = VoxelShapeUtils.rotate(frame, Rotation.CLOCKWISE_90);
        VoxelShape topRotated = VoxelShapeUtils.rotate(topPanel, Rotation.CLOCKWISE_90);
        VoxelShape bottomRotated = VoxelShapeUtils.rotate(bottomPanel, Rotation.CLOCKWISE_90);
        VoxelShape frameRotatedAlt = VoxelShapeUtils.rotate(frame, Direction.NORTH);
        VoxelShape rightRotated = VoxelShapeUtils.rotate(rightPanel, Direction.NORTH);
        VoxelShape leftRotated = VoxelShapeUtils.rotate(leftPanel, Direction.NORTH);
        for (int rotated = 0; rotated < 3; rotated++) {
            //If we don't need to rotate anything, this is zero
            // If we need to rotate the top and bottom frames, this is one
            // If we need to rotate the left and right frames, this is two
            boolean rotateVertical = rotated == 1;
            boolean rotateHorizontal = rotated == 2;
            VoxelShape baseFrame = rotateVertical ? frameRotated : rotateHorizontal ? frameRotatedAlt : frame;
            for (int top = 0; top < 2; top++) {
                VoxelShape withTop = top == 0 ? baseFrame : VoxelShapes.or(baseFrame, rotateVertical ? topRotated : topPanel);
                for (int bottom = 0; bottom < 2; bottom++) {
                    VoxelShape withBottom = bottom == 0 ? withTop : VoxelShapes.or(withTop, rotateVertical ? bottomRotated : bottomPanel);
                    for (int front = 0; front < 2; front++) {
                        VoxelShape withFront = front == 0 ? withBottom : VoxelShapes.or(withBottom, frontPanel);
                        for (int back = 0; back < 2; back++) {
                            VoxelShape withBack = back == 0 ? withFront : VoxelShapes.or(withFront, backPanel);
                            for (int left = 0; left < 2; left++) {
                                VoxelShape withLeft = left == 0 ? withBack : VoxelShapes.or(withBack, rotateHorizontal ? leftRotated : leftPanel);
                                for (int right = 0; right < 2; right++) {
                                    VoxelShape withRight = right == 0 ? withLeft : VoxelShapes.or(withLeft, rotateHorizontal ? rightRotated : rightPanel);
                                    bounds[getIndex(top, bottom, front, back, left, right, rotateVertical, rotateHorizontal)] = withRight;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 0 for an input is equivalent to false, 1 is equivalent to true
     */
    private static int getIndex(int top, int bottom, int front, int back, int left, int right, boolean rotateVertical, boolean rotateHorizontal) {
        return ((((((top | bottom << 1) | front << 2) | back << 3) | left << 4) | right << 5) | (rotateVertical ? 1 : 0) << 6) | (rotateHorizontal ? 1 : 0) << 7;
    }


    public BlockEnergyCube(Machine<TileEntityEnergyCube> type) {
        //Note: We require setting variable opacity so that the block state does not cache the ability of if blocks can be placed on top of the energy cube
        // this may change based on what sides are enabled. Torches cannot be placed on the sides due to vanilla checking the incorrect shape
        super(type, AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(2F, 4F).setRequiresTool().variableOpacity());
    }

    @Override
    public void setTileData(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack, @Nonnull TileEntityMekanism tile) {
        if (tile instanceof TileEntityEnergyCube) {
            if (Attribute.getTier(this, EnergyCubeTier.class) == EnergyCubeTier.CREATIVE) {
                //TODO: Move this to being set in the variant added to the item group
                ConfigInfo energyConfig = ((TileEntityEnergyCube) tile).configComponent.getConfig(TransmissionType.ENERGY);
                if (energyConfig != null) {
                    Optional<IStrictEnergyHandler> capability = stack.getCapability(Capabilities.STRICT_ENERGY_CAPABILITY).resolve();
                    if (capability.isPresent()) {
                        IStrictEnergyHandler energyHandlerItem = capability.get();
                        if (energyHandlerItem.getEnergyContainerCount() > 0) {
                            //Validate something didn't go terribly wrong and we actually do have the container we expect to have
                            energyConfig.fill(energyHandlerItem.getEnergy(0).isZero() ? DataType.INPUT : DataType.OUTPUT);
                        }
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull ISelectionContext context) {
        TileEntityEnergyCube energyCube = MekanismUtils.getTileEntity(TileEntityEnergyCube.class, world, pos, true);
        int index;
        if (energyCube == null) {
            //Default to facing north all enabled
            index = getIndex(1, 1, 1, 1, 1, 1, false, false);
        } else {
            ConfigInfo energyConfig = energyCube.configComponent.getConfig(TransmissionType.ENERGY);
            if (energyConfig == null) {
                //Default to facing north all enabled
                index = getIndex(1, 1, 1, 1, 1, 1, false, false);
            } else {
                Direction facing = Attribute.get(this, AttributeStateFacing.class).getDirection(state);
                index = getIndex(
                      isSideEnabled(energyConfig, facing, Direction.UP),//top
                      isSideEnabled(energyConfig, facing, Direction.DOWN),//bottom
                      isSideEnabled(energyConfig, facing, Direction.SOUTH),//front
                      isSideEnabled(energyConfig, facing, Direction.NORTH),//back
                      isSideEnabled(energyConfig, facing, Direction.EAST),//left
                      isSideEnabled(energyConfig, facing, Direction.WEST),//right
                      facing == Direction.EAST || facing == Direction.WEST,
                      facing == Direction.DOWN || facing == Direction.UP
                );
            }
        }
        return bounds[index];
    }

    /**
     * @return 1 if the side is enabled, 0 otherwise
     */
    private static int isSideEnabled(ConfigInfo energyConfig, Direction facing, Direction side) {
        ISlotInfo slotInfo = energyConfig.getSlotInfo(RelativeSide.fromDirections(facing, side));
        return slotInfo != null && slotInfo.isEnabled() ? 1 : 0;
    }
}