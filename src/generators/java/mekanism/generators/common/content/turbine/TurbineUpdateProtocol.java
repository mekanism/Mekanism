package mekanism.generators.common.content.turbine;

import java.util.Set;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Coord4D;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsBlockTypes;
import mekanism.generators.common.tile.turbine.TileEntityElectromagneticCoil;
import mekanism.generators.common.tile.turbine.TileEntityRotationalComplex;
import mekanism.generators.common.tile.turbine.TileEntitySaturatingCondenser;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import mekanism.generators.common.tile.turbine.TileEntityTurbineVent;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class TurbineUpdateProtocol extends UpdateProtocol<SynchronizedTurbineData> {

    public static final long GAS_PER_TANK = TankUpdateProtocol.FLUID_PER_TANK;
    public static final int MAX_BLADES = 28;

    public TurbineUpdateProtocol(TileEntityTurbineCasing tile) {
        super(tile);
    }

    @Override
    protected boolean isValidFrame(int x, int y, int z) {
        return BlockTypeTile.is(pointer.getWorld().getBlockState(new BlockPos(x, y, z)).getBlock(), GeneratorsBlockTypes.TURBINE_CASING);
    }

    @Override
    protected boolean isValidInnerNode(int x, int y, int z) {
        if (super.isValidInnerNode(x, y, z)) {
            return true;
        }
        TileEntity tile = MekanismUtils.getTileEntity(pointer.getWorld(), new BlockPos(x, y, z));
        return tile instanceof TileEntityTurbineRotor || tile instanceof TileEntityRotationalComplex || tile instanceof TileEntityPressureDisperser ||
               tile instanceof TileEntityElectromagneticCoil || tile instanceof TileEntitySaturatingCondenser;
    }

    @Override
    protected boolean canForm(SynchronizedTurbineData structure) {
        if (structure.volLength % 2 != 1 || structure.volWidth % 2 != 1) {
            return false;
        }
        int innerRadius = (Math.min(structure.volLength, structure.volWidth) - 3) / 2;
        if (innerRadius < Math.ceil((structure.volHeight - 2) / 4)) {
            return false;
        }
        int centerX = structure.minLocation.x + (structure.volLength - 1) / 2;
        int centerZ = structure.minLocation.z + (structure.volWidth - 1) / 2;

        Coord4D complex = null;

        Set<Coord4D> turbines = new ObjectOpenHashSet<>();
        Set<Coord4D> dispersers = new ObjectOpenHashSet<>();
        Set<Coord4D> coils = new ObjectOpenHashSet<>();
        Set<Coord4D> condensers = new ObjectOpenHashSet<>();

        //Scan for complex
        for (Coord4D coord : innerNodes) {
            TileEntity tile = MekanismUtils.getTileEntity(pointer.getWorld(), coord.getPos());
            if (tile instanceof TileEntityRotationalComplex) {
                if (complex != null || coord.x != centerX || coord.z != centerZ) {
                    return false;
                }
                structure.internalLocations.add(coord);
                complex = coord;
            } else if (tile instanceof TileEntityTurbineRotor) {
                if (coord.x != centerX || coord.z != centerZ) {
                    return false;
                }
                turbines.add(coord);
            } else if (tile instanceof TileEntityPressureDisperser) {
                dispersers.add(coord);
            } else if (tile instanceof TileEntityElectromagneticCoil) {
                coils.add(coord);
            } else if (tile instanceof TileEntitySaturatingCondenser) {
                condensers.add(coord);
            }
        }

        //Terminate if complex doesn't exist
        if (complex == null) {
            return false;
        }

        //Make sure a flat, horizontal plane of dispersers exists within the multiblock around the complex
        for (int x = complex.x - innerRadius; x <= complex.x + innerRadius; x++) {
            for (int z = complex.z - innerRadius; z <= complex.z + innerRadius; z++) {
                if (x != centerX || z != centerZ) {
                    TileEntityPressureDisperser tile = MekanismUtils.getTileEntity(TileEntityPressureDisperser.class, pointer.getWorld(), new BlockPos(x, complex.y, z));
                    if (tile == null) {
                        return false;
                    }
                    dispersers.remove(new Coord4D(x, complex.y, z, pointer.getWorld().getDimension().getType()));
                }
            }
        }

        //If any dispersers were not processed, they're in the wrong place
        if (!dispersers.isEmpty()) {
            return false;
        }

        //Make sure all condensers are in proper locations
        for (Coord4D coord : condensers) {
            if (coord.y <= complex.y) {
                return false;
            }
        }

        structure.condensers = condensers.size();
        int turbineHeight = 0;
        int blades = 0;

        // Starting from the complex, walk down and count the number of rotors/blades in the structure
        for (int y = complex.y - 1; y > structure.minLocation.y; y--) {
            TileEntityTurbineRotor rotor = MekanismUtils.getTileEntity(TileEntityTurbineRotor.class, pointer.getWorld(), new BlockPos(centerX, y, centerZ));
            if (rotor == null) {
                // Not a contiguous set of rotors
                return false;
            }
            turbineHeight++;
            blades += rotor.getHousedBlades();
            structure.internalLocations.add(Coord4D.get(rotor));
            turbines.remove(new Coord4D(centerX, y, centerZ, pointer.getWorld().getDimension().getType()));
        }

        // If there are any rotors left over, they are in the wrong place in the structure
        if (!turbines.isEmpty()) {
            return false;
        }

        // Update the structure with number of blades found on rotors
        structure.blades = blades;

        Coord4D startCoord = complex.offset(Direction.UP);
        if (MekanismUtils.getTileEntity(TileEntityElectromagneticCoil.class, pointer.getWorld(), startCoord.getPos()) != null) {
            structure.coils = new NodeCounter(new NodeChecker() {
                @Override
                public boolean isValid(Coord4D coord) {
                    return MekanismUtils.getTileEntity(TileEntityElectromagneticCoil.class, pointer.getWorld(), coord.getPos()) != null;
                }
            }).calculate(startCoord);
        }

        if (coils.size() > structure.coils) {
            return false;
        }

        for (Coord4D coord : structure.locations) {
            if (MekanismUtils.getTileEntity(TileEntityTurbineVent.class, pointer.getWorld(), coord.getPos()) != null) {
                if (coord.y < complex.y) {
                    return false;
                }
                structure.vents++;
            }
        }
        structure.lowerVolume = structure.volLength * structure.volWidth * turbineHeight;
        structure.complex = complex;
        return true;
    }

    @Override
    protected MultiblockManager<SynchronizedTurbineData> getManager() {
        return MekanismGenerators.turbineManager;
    }
}