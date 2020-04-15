package mekanism.generators.common.content.fission;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import mekanism.api.Coord4D;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsBlockTypes;
import mekanism.generators.common.tile.fission.TileEntityControlRodAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionFuelAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorPort;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class FissionReactorUpdateProtocol extends UpdateProtocol<SynchronizedFissionReactorData> {

    public FissionReactorUpdateProtocol(TileEntityFissionReactorCasing tile) {
        super(tile);
    }

    @Override
    protected boolean isValidFrame(int x, int y, int z) {
        return BlockTypeTile.is(pointer.getWorld().getBlockState(new BlockPos(x, y, z)).getBlock(), GeneratorsBlockTypes.FISSION_REACTOR_CASING);
    }

    @Override
    protected boolean isValidInnerNode(int x, int y, int z) {
        if (super.isValidInnerNode(x, y, z)) {
            return true;
        }
        TileEntity tile = MekanismUtils.getTileEntity(pointer.getWorld(), new BlockPos(x, y, z));
        return tile instanceof TileEntityFissionFuelAssembly || tile instanceof TileEntityControlRodAssembly;
    }

    @Override
    protected boolean canForm(SynchronizedFissionReactorData structure) {
        Map<AssemblyPos, FuelAssembly> map = new HashMap<>();
        int assemblyCount = 0;

        for (Coord4D coord : innerNodes) {
            TileEntity tile = MekanismUtils.getTileEntity(pointer.getWorld(), coord.getPos());
            AssemblyPos pos = new AssemblyPos(coord.x, coord.z);
            FuelAssembly assembly = map.get(pos);

            if (tile instanceof TileEntityFissionFuelAssembly) {
                if (assembly == null) {
                    map.put(pos, new FuelAssembly(coord, false));
                } else {
                    assembly.fuelAssemblies.add(coord);
                }
                assemblyCount++;
            } else if (tile instanceof TileEntityControlRodAssembly) {
                if (assembly == null) {
                    map.put(pos, new FuelAssembly(coord, true));
                } else if (assembly.controlRodAssembly != null) {
                    // only one control rod per assembly
                    return false;
                } else {
                    assembly.controlRodAssembly = coord;
                }
            }
        }

        // require at least one fuel assembly
        if (map.isEmpty()) {
            return false;
        }

        for (FuelAssembly assembly : map.values()) {
            if (!assembly.validate()) {
                return false;
            }
        }

        structure.fuelAssemblies = assemblyCount;

        return true;
    }

    @Override
    protected MultiblockManager<SynchronizedFissionReactorData> getManager() {
        return MekanismGenerators.fissionReactorManager;
    }

    @Override
    protected void onStructureCreated(SynchronizedFissionReactorData structure, int origX, int origY, int origZ, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax) {
        for (Coord4D obj : structure.locations) {
            if (MekanismUtils.getTileEntity(pointer.getWorld(), obj.getPos()) instanceof TileEntityFissionReactorPort) {
                structure.portLocations.add(obj);
            }
        }
    }

    public static class FuelAssembly {
        public Set<Coord4D> fuelAssemblies = new TreeSet<>((pos1, pos2) -> pos1.y - pos2.y);
        public Coord4D controlRodAssembly;

        public FuelAssembly(Coord4D start, boolean isControlRod) {
            if (isControlRod) {
                controlRodAssembly = start;
            } else {
                fuelAssemblies.add(start);
            }
        }

        public boolean validate() {
            if (fuelAssemblies.isEmpty() || controlRodAssembly == null) {
                return false;
            }
            int prevY = -1;
            for (Coord4D coord : fuelAssemblies) {
                if (prevY != -1 && coord.y != prevY + 1) {
                    return false;
                }
                prevY = coord.y;
            }
            return controlRodAssembly.y == prevY + 1;
        }
    }

    public static class AssemblyPos {
        int x, z;

        public AssemblyPos(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + x;
            result = prime * result + z;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof AssemblyPos && ((AssemblyPos) obj).x == x && ((AssemblyPos) obj).z == z;
        }
    }
}
