package mekanism.generators.common.content.turbine;

import java.util.Set;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsBlockTypes;
import mekanism.generators.common.tile.turbine.TileEntityElectromagneticCoil;
import mekanism.generators.common.tile.turbine.TileEntityRotationalComplex;
import mekanism.generators.common.tile.turbine.TileEntitySaturatingCondenser;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import mekanism.generators.common.tile.turbine.TileEntityTurbineVent;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class TurbineUpdateProtocol extends FormationProtocol<TurbineMultiblockData> {

    public static final long GAS_PER_TANK = TankUpdateProtocol.FLUID_PER_TANK;
    public static final int MAX_BLADES = 28;

    public TurbineUpdateProtocol(TileEntityTurbineCasing tile) {
        super(tile);
    }

    @Override
    protected CasingType getCasingType(BlockPos pos) {
        Block block = pointer.getTileWorld().getBlockState(pos).getBlock();
        if (BlockTypeTile.is(block, GeneratorsBlockTypes.TURBINE_CASING)) {
            return CasingType.FRAME;
        } else if (BlockTypeTile.is(block, GeneratorsBlockTypes.TURBINE_VALVE)) {
            return CasingType.VALVE;
        } else if (BlockTypeTile.is(block, GeneratorsBlockTypes.TURBINE_VENT)) {
            return CasingType.OTHER;
        }
        return CasingType.INVALID;
    }

    @Override
    protected boolean isValidInnerNode(BlockPos pos) {
        if (super.isValidInnerNode(pos)) {
            return true;
        }
        return BlockType.is(pointer.getTileWorld().getBlockState(pos).getBlock(), MekanismBlockTypes.PRESSURE_DISPERSER, GeneratorsBlockTypes.TURBINE_ROTOR,
              GeneratorsBlockTypes.ROTATIONAL_COMPLEX, GeneratorsBlockTypes.ELECTROMAGNETIC_COIL, GeneratorsBlockTypes.SATURATING_CONDENSER);
    }

    @Override
    protected FormationResult validate(TurbineMultiblockData structure, Set<BlockPos> innerNodes) {
        if (structure.length % 2 != 1 || structure.width % 2 != 1) {
            return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_EVEN_LENGTH);
        }
        int centerX = structure.minLocation.getX() + (structure.length - 1) / 2;
        int centerZ = structure.minLocation.getZ() + (structure.width - 1) / 2;

        BlockPos complex = null;

        Set<BlockPos> turbines = new ObjectOpenHashSet<>();
        Set<BlockPos> dispersers = new ObjectOpenHashSet<>();
        Set<BlockPos> coils = new ObjectOpenHashSet<>();
        Set<BlockPos> condensers = new ObjectOpenHashSet<>();

        //Scan for complex
        for (BlockPos pos : innerNodes) {
            TileEntity tile = MekanismUtils.getTileEntity(pointer.getTileWorld(), pos);
            if (tile instanceof TileEntityRotationalComplex) {
                if (complex != null || pos.getX() != centerX || pos.getZ() != centerZ) {
                    return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_BAD_COMPLEX, pos);
                }
                structure.internalLocations.add(pos);
                complex = pos;
            } else if (tile instanceof TileEntityTurbineRotor) {
                if (pos.getX() != centerX || pos.getZ() != centerZ) {
                    return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_BAD_ROTOR, pos);
                }
                turbines.add(pos);
            } else if (tile instanceof TileEntityPressureDisperser) {
                dispersers.add(pos);
            } else if (tile instanceof TileEntityElectromagneticCoil) {
                coils.add(pos);
            } else if (tile instanceof TileEntitySaturatingCondenser) {
                condensers.add(pos);
            }
        }

        //Terminate if complex doesn't exist
        if (complex == null) {
            return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_MISSING_COMPLEX);
        }

        int rotors = complex.getY() - structure.minLocation.getY() + 1;
        int innerRadius = (Math.min(structure.length, structure.width) - 3) / 2;
        if (innerRadius < rotors / 4) {
            return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_TOO_NARROW);
        }

        //Make sure a flat, horizontal plane of dispersers exists within the multiblock around the complex
        for (int x = complex.getX() - innerRadius; x <= complex.getX() + innerRadius; x++) {
            for (int z = complex.getZ() - innerRadius; z <= complex.getZ() + innerRadius; z++) {
                if (x != centerX || z != centerZ) {
                    TileEntityPressureDisperser tile = MekanismUtils.getTileEntity(TileEntityPressureDisperser.class, pointer.getTileWorld(), new BlockPos(x, complex.getY(), z));
                    if (tile == null) {
                        return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_MISSING_DISPERSER, new BlockPos(x, complex.getY(), z));
                    }
                    dispersers.remove(new BlockPos(x, complex.getY(), z));
                }
            }
        }

        //If any dispersers were not processed, they're in the wrong place
        if (!dispersers.isEmpty()) {
            return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_MALFORMED_DISPERSERS);
        }

        //Make sure all condensers are in proper locations
        for (BlockPos coord : condensers) {
            if (coord.getY() <= complex.getY()) {
                return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_CONDENSER_BELOW_COMPLEX, coord);
            }
        }

        structure.condensers = condensers.size();
        int turbineHeight = 0;
        int blades = 0;

        // Starting from the complex, walk down and count the number of rotors/blades in the structure
        for (int y = complex.getY() - 1; y > structure.minLocation.getY(); y--) {
            TileEntityTurbineRotor rotor = MekanismUtils.getTileEntity(TileEntityTurbineRotor.class, pointer.getTileWorld(), new BlockPos(centerX, y, centerZ));
            if (rotor == null) {
                // Not a contiguous set of rotors
                FormationResult.fail(GeneratorsLang.TURBINE_INVALID_ROTORS_NOT_CONTIGUOUS);
            }
            turbineHeight++;
            blades += rotor.getHousedBlades();
            structure.internalLocations.add(rotor.getPos());
            turbines.remove(new BlockPos(centerX, y, centerZ));
        }

        // If there are any rotors left over, they are in the wrong place in the structure
        if (!turbines.isEmpty()) {
            return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_BAD_ROTORS);
        }

        // Update the structure with number of blades found on rotors
        structure.blades = blades;

        BlockPos startCoord = complex.offset(Direction.UP);
        if (MekanismUtils.getTileEntity(TileEntityElectromagneticCoil.class, pointer.getTileWorld(), startCoord) != null) {
            structure.coils = FormationProtocol.explore(startCoord, coord -> MekanismUtils.getTileEntity(TileEntityElectromagneticCoil.class, pointer.getTileWorld(), coord) != null);
        }

        if (coils.size() > structure.coils) {
            return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_MALFORMED_COILS);
        }

        for (BlockPos coord : structure.locations) {
            if (MekanismUtils.getTileEntity(TileEntityTurbineVent.class, pointer.getTileWorld(), coord) != null) {
                if (coord.getY() < complex.getY()) {
                    return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_VENT_BELOW_COMPLEX, coord);
                }
                structure.vents++;
            }
        }
        structure.lowerVolume = structure.length * structure.width * turbineHeight;
        structure.complex = complex;
        return FormationResult.SUCCESS;
    }

    @Override
    protected MultiblockManager<TurbineMultiblockData> getManager() {
        return MekanismGenerators.turbineManager;
    }
}