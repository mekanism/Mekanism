package mekanism.generators.common.content.fission;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.multiblock.IValveHandler.ValveData;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsBlockTypes;
import mekanism.generators.common.tile.fission.TileEntityControlRodAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionFuelAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorPort;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class FissionReactorUpdateProtocol extends UpdateProtocol<FissionReactorMultiblockData> {

    public FissionReactorUpdateProtocol(TileEntityFissionReactorCasing tile) {
        super(tile);
    }

    @Override
    protected CasingType getCasingType(BlockPos pos) {
        Block block = pointer.getWorld().getBlockState(pos).getBlock();
        if (BlockTypeTile.is(block, GeneratorsBlockTypes.FISSION_REACTOR_CASING)) {
            return CasingType.FRAME;
        } else if (BlockTypeTile.is(block, GeneratorsBlockTypes.FISSION_REACTOR_PORT)) {
            return CasingType.VALVE;
        } else if (BlockTypeTile.is(block, GeneratorsBlockTypes.FISSION_REACTOR_LOGIC_ADAPTER)) {
            return CasingType.OTHER;
        }
        return CasingType.INVALID;
    }

    @Override
    protected boolean isValidInnerNode(BlockPos pos) {
        if (super.isValidInnerNode(pos)) {
            return true;
        }
        TileEntity tile = MekanismUtils.getTileEntity(pointer.getWorld(), pos);
        return tile instanceof TileEntityFissionFuelAssembly || tile instanceof TileEntityControlRodAssembly;
    }

    @Override
    protected FormationResult validate(FissionReactorMultiblockData structure) {
        Map<AssemblyPos, FuelAssembly> map = new HashMap<>();
        Set<Coord4D> fuelAssemblyCoords = new HashSet<>();
        int assemblyCount = 0, surfaceArea = 0;

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
                // compute surface area
                surfaceArea += 6;
                for (Direction side : Direction.values()) {
                    if (fuelAssemblyCoords.contains(coord.offset(side))) {
                        surfaceArea -= 2;
                    }
                }
                fuelAssemblyCoords.add(coord);
                structure.internalLocations.add(coord);
            } else if (tile instanceof TileEntityControlRodAssembly) {
                if (assembly == null) {
                    map.put(pos, new FuelAssembly(coord, true));
                } else if (assembly.controlRodAssembly != null) {
                    // only one control rod per assembly
                    return FormationResult.fail(GeneratorsLang.FISSION_INVALID_EXTRA_CONTROL_ROD, coord.getPos());
                } else {
                    assembly.controlRodAssembly = coord;
                }
            }
        }

        // require at least one fuel assembly
        if (map.isEmpty()) {
            return FormationResult.fail(GeneratorsLang.FISSION_INVALID_MISSING_FUEL_ASSEMBLY);
        }

        for (FuelAssembly assembly : map.values()) {
            FormationResult result = assembly.validate();
            if (!result.isFormed()) {
                return result;
            }
            structure.assemblies.add(assembly.build());
        }

        structure.fuelAssemblies = assemblyCount;
        structure.surfaceArea = surfaceArea;

        return FormationResult.SUCCESS;
    }

    @Override
    protected MultiblockManager<FissionReactorMultiblockData> getManager() {
        return MekanismGenerators.fissionReactorManager;
    }

    @Override
    protected void onStructureCreated(FissionReactorMultiblockData structure, int origX, int origY, int origZ, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax) {
        for (Coord4D obj : structure.locations) {
            if (MekanismUtils.getTileEntity(pointer.getWorld(), obj.getPos()) instanceof TileEntityFissionReactorPort) {
                ValveData data = new ValveData();
                data.location = obj;
                data.side = getSide(obj, origX + xmin, origX + xmax, origY + ymin, origY + ymax, origZ + zmin, origZ + zmax);
                structure.valves.add(data);
            }
        }
    }

    public static class FuelAssembly {
        public TreeSet<Coord4D> fuelAssemblies = new TreeSet<>((pos1, pos2) -> pos1.y - pos2.y);
        public Coord4D controlRodAssembly;

        public FuelAssembly(Coord4D start, boolean isControlRod) {
            if (isControlRod) {
                controlRodAssembly = start;
            } else {
                fuelAssemblies.add(start);
            }
        }

        public FormationResult validate() {
            if (fuelAssemblies.isEmpty() || controlRodAssembly == null) {
                return FormationResult.fail(GeneratorsLang.FISSION_INVALID_BAD_FUEL_ASSEMBLY);
            }
            int prevY = -1;
            for (Coord4D coord : fuelAssemblies) {
                if (prevY != -1 && coord.y != prevY + 1) {
                    return FormationResult.fail(GeneratorsLang.FISSION_INVALID_MALFORMED_FUEL_ASSEMBLY, coord.getPos());
                }
                prevY = coord.y;
            }

            if (controlRodAssembly.y != prevY + 1) {
                return FormationResult.fail(GeneratorsLang.FISSION_INVALID_BAD_CONTROL_ROD, controlRodAssembly.getPos());
            }
            return FormationResult.SUCCESS;
        }

        public FormedAssembly build() {
            Coord4D base = fuelAssemblies.first();
            return new FormedAssembly(new BlockPos(base.x, base.y, base.z), fuelAssemblies.size());
        }
    }

    public static class FormedAssembly {

        BlockPos pos;
        int height;

        public FormedAssembly(BlockPos pos, int height) {
            this.pos = pos;
            this.height = height;
        }

        public CompoundNBT write() {
            CompoundNBT ret = new CompoundNBT();
            ret.putInt(NBTConstants.X, pos.getX());
            ret.putInt(NBTConstants.Y, pos.getY());
            ret.putInt(NBTConstants.Z, pos.getZ());
            ret.putInt(NBTConstants.HEIGHT, height);
            return ret;
        }

        public BlockPos getPos() {
            return pos;
        }

        public int getHeight() {
            return height;
        }

        public static FormedAssembly read(CompoundNBT nbt) {
            return new FormedAssembly(new BlockPos(nbt.getInt(NBTConstants.X), nbt.getInt(NBTConstants.Y), nbt.getInt(NBTConstants.Z)),
                                      nbt.getInt(NBTConstants.HEIGHT));
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
