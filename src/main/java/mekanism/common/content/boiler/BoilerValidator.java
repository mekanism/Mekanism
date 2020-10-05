package mekanism.common.content.boiler;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import mekanism.common.MekanismLang;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.FormationProtocol.CasingType;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.tile.multiblock.TileEntitySuperheatingElement;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class BoilerValidator extends CuboidStructureValidator<BoilerMultiblockData> {

    @Override
    protected CasingType getCasingType(BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (BlockType.is(block, MekanismBlockTypes.BOILER_CASING)) {
            return CasingType.FRAME;
        } else if (BlockType.is(block, MekanismBlockTypes.BOILER_VALVE)) {
            return CasingType.VALVE;
        }
        return CasingType.INVALID;
    }

    @Override
    protected boolean validateInner(BlockPos pos) {
        if (super.validateInner(pos)) {
            return true;
        }
        return BlockType.is(world.getBlockState(pos).getBlock(), MekanismBlockTypes.PRESSURE_DISPERSER, MekanismBlockTypes.SUPERHEATING_ELEMENT);
    }

    @Override
    public FormationResult postcheck(BoilerMultiblockData structure, Set<BlockPos> innerNodes) {
        Set<BlockPos> dispersers = new ObjectOpenHashSet<>();
        Set<BlockPos> elements = new ObjectOpenHashSet<>();
        for (BlockPos pos : innerNodes) {
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);
            if (tile instanceof TileEntityPressureDisperser) {
                dispersers.add(pos);
            } else if (tile instanceof TileEntitySuperheatingElement) {
                structure.internalLocations.add(pos);
                elements.add(pos);
            }
        }
        //Ensure at least one disperser exists
        if (dispersers.isEmpty()) {
            return FormationResult.fail(MekanismLang.BOILER_INVALID_NO_DISPERSER);
        }
        //Ensure that at least one superheating element exists
        if (elements.isEmpty()) {
            return FormationResult.fail(MekanismLang.BOILER_INVALID_SUPERHEATING);
        }

        //Find a single disperser contained within this multiblock
        final BlockPos initDisperser = dispersers.iterator().next();

        //Ensure that a full horizontal plane of dispersers exist, surrounding the found disperser
        BlockPos pos = new BlockPos(structure.renderLocation.getX(), initDisperser.getY(), structure.renderLocation.getZ());
        for (int x = 1; x < structure.length() - 1; x++) {
            for (int z = 1; z < structure.width() - 1; z++) {
                BlockPos shifted = pos.add(x, 0, z);
                TileEntityPressureDisperser tile = MekanismUtils.getTileEntity(TileEntityPressureDisperser.class, world, shifted);
                if (tile == null) {
                    return FormationResult.fail(MekanismLang.BOILER_INVALID_MISSING_DISPERSER, shifted);
                }
                dispersers.remove(shifted);
            }
        }

        //If there are more dispersers than those on the plane found, the structure is invalid
        if (!dispersers.isEmpty()) {
            return FormationResult.fail(MekanismLang.BOILER_INVALID_EXTRA_DISPERSER);
        }

        if (!elements.isEmpty()) {
            structure.superheatingElements = FormationProtocol.explore(elements.iterator().next(), coord ->
                  coord.getY() < initDisperser.getY() && MekanismUtils.getTileEntity(TileEntitySuperheatingElement.class, world, coord) != null);
        }

        if (elements.size() > structure.superheatingElements) {
            return FormationResult.fail(MekanismLang.BOILER_INVALID_SUPERHEATING);
        }

        BlockPos initAir = null;
        int totalAir = 0;

        //Find the first available block in the structure for water storage (including casings)
        for (int x = structure.renderLocation.getX(); x < structure.renderLocation.getX() + structure.length(); x++) {
            for (int y = structure.renderLocation.getY(); y < initDisperser.getY(); y++) {
                for (int z = structure.renderLocation.getZ(); z < structure.renderLocation.getZ() + structure.width(); z++) {
                    BlockPos airPos = new BlockPos(x, y, z);
                    if (world.isAirBlock(airPos) || isFrameCompatible(MekanismUtils.getTileEntity(world, airPos))) {
                        initAir = airPos;
                        totalAir++;
                    }
                }
            }
        }

        //Gradle build requires these fields to be final
        final BlockPos renderLocation = structure.renderLocation;
        final int volLength = structure.length();
        final int volWidth = structure.width();
        structure.setWaterVolume(FormationProtocol.explore(initAir, coord ->
              coord.getY() >= renderLocation.getY() - 1 && coord.getY() < initDisperser.getY() &&
              coord.getX() >= renderLocation.getX() && coord.getX() < renderLocation.getX() + volLength &&
              coord.getZ() >= renderLocation.getZ() && coord.getZ() < renderLocation.getZ() + volWidth &&
              (world.isAirBlock(coord) || isFrameCompatible(MekanismUtils.getTileEntity(world, coord)))));

        //Make sure all air blocks are connected
        if (totalAir > structure.getWaterVolume()) {
            return FormationResult.fail(MekanismLang.BOILER_INVALID_AIR_POCKETS);
        }

        int steamHeight = (structure.renderLocation.getY() + structure.height() - 2) - initDisperser.getY();
        structure.setSteamVolume(structure.width() * structure.length() * steamHeight);
        structure.upperRenderLocation = new BlockPos(structure.renderLocation.getX(), initDisperser.getY() + 1, structure.renderLocation.getZ());
        return FormationResult.SUCCESS;
    }
}