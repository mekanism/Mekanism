package mekanism.generators.common.content.turbine;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mekanism.api.Coord4D;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.block.states.BlockStateGenerator.GeneratorType;
import mekanism.generators.common.tile.turbine.TileEntityElectromagneticCoil;
import mekanism.generators.common.tile.turbine.TileEntityRotationalComplex;
import mekanism.generators.common.tile.turbine.TileEntitySaturatingCondenser;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import mekanism.generators.common.tile.turbine.TileEntityTurbineVent;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class TurbineUpdateProtocol extends UpdateProtocol<SynchronizedTurbineData> {

    public static final int FLUID_PER_TANK = TankUpdateProtocol.FLUID_PER_TANK;
    public static final int MAX_BLADES = 28;

    public TurbineUpdateProtocol(TileEntityTurbineCasing tileEntity) {
        super(tileEntity);
    }

    @Override
    protected boolean isValidFrame(int x, int y, int z) {
        return GeneratorType.get(pointer.getWorld().getBlockState(new BlockPos(x, y, z)))
              == GeneratorType.TURBINE_CASING;
    }

    @Override
    protected boolean isValidInnerNode(int x, int y, int z) {
        if (super.isValidInnerNode(x, y, z)) {
            return true;
        }

        TileEntity tile = pointer.getWorld().getTileEntity(new BlockPos(x, y, z));

        return tile instanceof TileEntityTurbineRotor || tile instanceof TileEntityRotationalComplex ||
              tile instanceof TileEntityPressureDisperser || tile instanceof TileEntityElectromagneticCoil ||
              tile instanceof TileEntitySaturatingCondenser;
    }

    @Override
    protected boolean canForm(SynchronizedTurbineData structure) {
        if (structure.volLength % 2 == 1 && structure.volWidth % 2 == 1) {
            int innerRadius = (Math.min(structure.volLength, structure.volWidth) - 3) / 2;

            if (innerRadius >= Math.ceil((structure.volHeight - 2) / 4)) {
                int centerX = structure.minLocation.x + (structure.volLength - 1) / 2;
                int centerZ = structure.minLocation.z + (structure.volWidth - 1) / 2;

                Coord4D complex = null;

                Set<Coord4D> turbines = new HashSet<>();
                Set<Coord4D> dispersers = new HashSet<>();
                Set<Coord4D> coils = new HashSet<>();
                Set<Coord4D> condensers = new HashSet<>();

                //Scan for complex
                for (Coord4D coord : innerNodes) {
                    TileEntity tile = coord.getTileEntity(pointer.getWorld());

                    if (tile instanceof TileEntityRotationalComplex) {
                        if (complex != null) {
                            return false;
                        } else if (coord.x != centerX || coord.z != centerZ) {
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
                        if (!(x == centerX && z == centerZ)) {
                            TileEntity tile = pointer.getWorld().getTileEntity(new BlockPos(x, complex.y, z));

                            if (!(tile instanceof TileEntityPressureDisperser)) {
                                return false;
                            }

                            dispersers.remove(new Coord4D(x, complex.y, z, pointer.getWorld().provider.getDimension()));
                        }
                    }
                }

                //If any dispersers were not processed, they're in the wrong place
                if (dispersers.size() > 0) {
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
                    TileEntity tile = pointer.getWorld().getTileEntity(new BlockPos(centerX, y, centerZ));
                    if (tile instanceof TileEntityTurbineRotor) {
                        TileEntityTurbineRotor rotor = (TileEntityTurbineRotor)tile;
                        turbineHeight++;
                        blades += rotor.getHousedBlades();
                        structure.internalLocations.add(Coord4D.get(tile));
                        turbines.remove(new Coord4D(centerX, y, centerZ, pointer.getWorld().provider.getDimension()));
                    } else {
                        // Not a contiguous set of rotors
                        return false;
                    }
                }

                // If there are any rotors left over, they are in the wrong place in the structure
                if (turbines.size() > 0) {
                    return false;
                }

                // Update the structure with number of blades found on rotors
                structure.blades = blades;

                Coord4D startCoord = complex.offset(EnumFacing.UP);

                if (startCoord.getTileEntity(pointer.getWorld()) instanceof TileEntityElectromagneticCoil) {
                    structure.coils = new NodeCounter(new NodeChecker() {
                        @Override
                        public boolean isValid(Coord4D coord) {
                            return coord.getTileEntity(pointer.getWorld()) instanceof TileEntityElectromagneticCoil;
                        }
                    }).calculate(startCoord);
                }

                if (coils.size() > structure.coils) {
                    return false;
                }



                for (Coord4D coord : structure.locations) {
                    if (coord.getTileEntity(pointer.getWorld()) instanceof TileEntityTurbineVent) {
                        if (coord.y >= complex.y) {
                            structure.vents++;
                        } else {
                            return false;
                        }
                    }
                }

                structure.lowerVolume = structure.volLength * structure.volWidth * turbineHeight;
                structure.complex = complex;

                return true;
            }
        }

        return false;
    }

    @Override
    protected MultiblockCache<SynchronizedTurbineData> getNewCache() {
        return new TurbineCache();
    }

    @Override
    protected SynchronizedTurbineData getNewStructure() {
        return new SynchronizedTurbineData();
    }

    @Override
    protected MultiblockManager<SynchronizedTurbineData> getManager() {
        return MekanismGenerators.turbineManager;
    }

    @Override
    protected void mergeCaches(List<ItemStack> rejectedItems, MultiblockCache<SynchronizedTurbineData> cache,
          MultiblockCache<SynchronizedTurbineData> merge) {
        if (((TurbineCache) cache).fluid == null) {
            ((TurbineCache) cache).fluid = ((TurbineCache) merge).fluid;
        } else if (((TurbineCache) merge).fluid != null && ((TurbineCache) cache).fluid
              .isFluidEqual(((TurbineCache) merge).fluid)) {
            ((TurbineCache) cache).fluid.amount += ((TurbineCache) merge).fluid.amount;
        }

        ((TurbineCache) cache).electricity += ((TurbineCache) merge).electricity;
        ((TurbineCache) cache).dumpMode = ((TurbineCache) merge).dumpMode;
    }

    @Override
    protected void onFormed() {
        super.onFormed();

        if (structureFound.fluidStored != null) {
            structureFound.fluidStored.amount = Math
                  .min(structureFound.fluidStored.amount, structureFound.getFluidCapacity());
        }

        structureFound.electricityStored = Math
              .min(structureFound.electricityStored, structureFound.getEnergyCapacity());
    }
}
