package mekanism.common.content.boiler;

import java.util.Set;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.tile.TileEntitySuperheatingElement;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class BoilerUpdateProtocol extends UpdateProtocol<BoilerMultiblockData> {

    public BoilerUpdateProtocol(TileEntityBoilerCasing tile) {
        super(tile);
    }

    @Override
    protected CasingType getCasingType(BlockPos pos) {
        Block block = pointer.getWorld().getBlockState(pos).getBlock();
        if (BlockTypeTile.is(block, MekanismBlockTypes.BOILER_CASING)) {
            return CasingType.FRAME;
        } else if (BlockTypeTile.is(block, MekanismBlockTypes.BOILER_VALVE)) {
            return CasingType.VALVE;
        }
        return CasingType.INVALID;
    }

    @Override
    protected boolean isValidInnerNode(BlockPos pos) {
        if (super.isValidInnerNode(pos)) {
            return true;
        }
        TileEntity tile = MekanismUtils.getTileEntity(pointer.getWorld(), pos);
        return tile instanceof TileEntityPressureDisperser || tile instanceof TileEntitySuperheatingElement;
    }

    @Override
    protected FormationResult validate(BoilerMultiblockData structure) {
        Set<Coord4D> dispersers = new ObjectOpenHashSet<>();
        Set<Coord4D> elements = new ObjectOpenHashSet<>();
        for (Coord4D coord : innerNodes) {
            TileEntity tile = MekanismUtils.getTileEntity(pointer.getWorld(), coord.getPos());
            if (tile instanceof TileEntityPressureDisperser) {
                dispersers.add(coord);
            } else if (tile instanceof TileEntitySuperheatingElement) {
                structure.internalLocations.add(coord);
                elements.add(coord);
            }
        }
        //Ensure at least one disperser exists
        if (dispersers.isEmpty()) {
            return FormationResult.fail(MekanismLang.BOILER_INVALID_NO_DISPERSER);
        }

        //Find a single disperser contained within this multiblock
        final Coord4D initDisperser = dispersers.iterator().next();

        //Ensure that a full horizontal plane of dispersers exist, surrounding the found disperser
        BlockPos pos = new BlockPos(structure.renderLocation.x, initDisperser.y, structure.renderLocation.z);
        for (int x = 1; x < structure.volLength - 1; x++) {
            for (int z = 1; z < structure.volWidth - 1; z++) {
                BlockPos shifted = pos.add(x, 0, z);
                TileEntityPressureDisperser tile = MekanismUtils.getTileEntity(TileEntityPressureDisperser.class, pointer.getWorld(), shifted);
                if (tile == null) {
                    return FormationResult.fail(MekanismLang.BOILER_INVALID_MISSING_DISPERSER, shifted);
                }
                dispersers.remove(new Coord4D(shifted, pointer.getWorld()));
            }
        }

        //If there are more dispersers than those on the plane found, the structure is invalid
        if (!dispersers.isEmpty()) {
            return FormationResult.fail(MekanismLang.BOILER_INVALID_EXTRA_DISPERSER);
        }

        if (!elements.isEmpty()) {
            structure.superheatingElements = new NodeCounter(new NodeChecker() {
                @Override
                public boolean isValid(Coord4D coord) {
                    return coord.y < initDisperser.y && MekanismUtils.getTileEntity(TileEntitySuperheatingElement.class, pointer.getWorld(), coord.getPos()) != null;
                }
            }).calculate(elements.iterator().next());
        }

        if (elements.size() > structure.superheatingElements) {
            return FormationResult.fail(MekanismLang.BOILER_INVALID_SUPERHEATING);
        }

        Coord4D initAir = null;
        int totalAir = 0;

        //Find the first available block in the structure for water storage (including casings)
        for (int x = structure.renderLocation.x; x < structure.renderLocation.x + structure.volLength; x++) {
            for (int y = structure.renderLocation.y; y < initDisperser.y; y++) {
                for (int z = structure.renderLocation.z; z < structure.renderLocation.z + structure.volWidth; z++) {
                    if (pointer.getWorld().isAirBlock(new BlockPos(x, y, z)) || checkNode(x, y, z)) {
                        initAir = new Coord4D(x, y, z, pointer.getWorld().getDimension().getType());
                        totalAir++;
                    }
                }
            }
        }

        //Gradle build requires these fields to be final
        final Coord4D renderLocation = structure.renderLocation;
        final int volLength = structure.volLength;
        final int volWidth = structure.volWidth;
        structure.setWaterVolume(new NodeCounter(new NodeChecker() {
            @Override
            public final boolean isValid(Coord4D coord) {
                BlockPos coordPos = coord.getPos();
                int x = coordPos.getX();
                int y = coordPos.getY();
                int z = coordPos.getZ();
                return y >= renderLocation.y - 1 && y < initDisperser.y &&
                       x >= renderLocation.x && x < renderLocation.x + volLength &&
                       z >= renderLocation.z && z < renderLocation.z + volWidth &&
                       (pointer.getWorld().isAirBlock(coordPos) || checkNode(coordPos));
            }
        }).calculate(initAir));

        //Make sure all air blocks are connected
        if (totalAir > structure.getWaterVolume()) {
            return FormationResult.fail(MekanismLang.BOILER_INVALID_AIR_POCKETS);
        }

        int steamHeight = (structure.renderLocation.y + structure.volHeight - 2) - initDisperser.y;
        structure.setSteamVolume(structure.volWidth * structure.volLength * steamHeight);
        structure.upperRenderLocation = new Coord4D(structure.renderLocation.x, initDisperser.y + 1, structure.renderLocation.z, pointer.getWorld().getDimension().getType());
        return FormationResult.SUCCESS;
    }

    @Override
    protected MultiblockManager<BoilerMultiblockData> getManager() {
        return Mekanism.boilerManager;
    }
}