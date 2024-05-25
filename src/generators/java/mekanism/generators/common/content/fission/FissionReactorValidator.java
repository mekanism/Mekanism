package mekanism.generators.common.content.fission;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol.CasingType;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsBlockTypes;
import mekanism.generators.common.tile.fission.TileEntityControlRodAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionFuelAssembly;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.Nullable;

public class FissionReactorValidator extends CuboidStructureValidator<FissionReactorMultiblockData> {

    @Override
    protected CasingType getCasingType(BlockState state) {
        Block block = state.getBlock();
        if (BlockType.is(block, GeneratorsBlockTypes.FISSION_REACTOR_CASING)) {
            return CasingType.FRAME;
        } else if (BlockType.is(block, GeneratorsBlockTypes.FISSION_REACTOR_PORT)) {
            return CasingType.VALVE;
        } else if (BlockType.is(block, GeneratorsBlockTypes.FISSION_REACTOR_LOGIC_ADAPTER)) {
            return CasingType.OTHER;
        }
        return CasingType.INVALID;
    }

    @Override
    protected boolean validateInner(BlockState state, Long2ObjectMap<ChunkAccess> chunkMap, BlockPos pos) {
        if (super.validateInner(state, chunkMap, pos)) {
            return true;
        }
        return BlockType.is(state.getBlock(), GeneratorsBlockTypes.FISSION_FUEL_ASSEMBLY, GeneratorsBlockTypes.CONTROL_ROD_ASSEMBLY);
    }

    @Override
    public FormationResult postcheck(FissionReactorMultiblockData structure, Long2ObjectMap<ChunkAccess> chunkMap) {
        Map<AssemblyPos, FuelAssembly> map = new HashMap<>();
        Set<BlockPos> fuelAssemblyCoords = new HashSet<>();
        int assemblyCount = 0, surfaceArea = 0;

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (BlockPos coord : structure.internalLocations) {
            BlockEntity tile = WorldUtils.getTileEntity(world, chunkMap, coord);
            AssemblyPos pos = new AssemblyPos(coord.getX(), coord.getZ());
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
                for (Direction side : EnumUtils.DIRECTIONS) {
                    mutable.setWithOffset(coord, side);
                    if (fuelAssemblyCoords.contains(mutable)) {
                        surfaceArea -= 2;
                    }
                }
                fuelAssemblyCoords.add(coord);
            } else if (tile instanceof TileEntityControlRodAssembly) {
                if (assembly == null) {
                    map.put(pos, new FuelAssembly(coord, true));
                } else if (assembly.controlRodAssembly != null) {
                    // only one control rod per assembly
                    return FormationResult.fail(GeneratorsLang.FISSION_INVALID_EXTRA_CONTROL_ROD, coord);
                } else {
                    assembly.controlRodAssembly = coord;
                }
            }
        }

        // require at least one fuel assembly
        if (map.isEmpty()) {
            return FormationResult.fail(GeneratorsLang.FISSION_INVALID_MISSING_FUEL_ASSEMBLY);
        }

        for (Entry<AssemblyPos, FuelAssembly> entry : map.entrySet()) {
            FuelAssembly assembly = entry.getValue();
            FormationResult result = assembly.validate(entry.getKey());
            if (!result.isFormed()) {
                return result;
            }
            structure.assemblies.add(assembly.build());
        }

        structure.setAssemblies(assemblyCount);
        structure.surfaceArea = surfaceArea;

        return FormationResult.SUCCESS;
    }

    public static class FuelAssembly {

        public final SortedSet<BlockPos> fuelAssemblies = new TreeSet<>(Comparator.comparingInt(Vec3i::getY));
        public BlockPos controlRodAssembly;

        public FuelAssembly(BlockPos start, boolean isControlRod) {
            if (isControlRod) {
                controlRodAssembly = start;
            } else {
                fuelAssemblies.add(start);
            }
        }

        public FormationResult validate(AssemblyPos assemblyPos) {
            if (controlRodAssembly == null) {
                return FormationResult.fail(GeneratorsLang.FISSION_INVALID_MISSING_CONTROL_ROD.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                      MekanismLang.GENERIC_PARENTHESIS.translate(MekanismLang.GENERIC_WITH_COMMA.translate(assemblyPos.x, assemblyPos.z))));
            } else if (fuelAssemblies.isEmpty()) {
                return FormationResult.fail(GeneratorsLang.FISSION_INVALID_BAD_FUEL_ASSEMBLY, controlRodAssembly);
            }
            int prevY = -1;
            for (BlockPos coord : fuelAssemblies) {
                if (prevY != -1 && coord.getY() != prevY + 1) {
                    return FormationResult.fail(GeneratorsLang.FISSION_INVALID_MALFORMED_FUEL_ASSEMBLY, coord);
                }
                prevY = coord.getY();
            }

            if (controlRodAssembly.getY() != prevY + 1) {
                return FormationResult.fail(GeneratorsLang.FISSION_INVALID_BAD_CONTROL_ROD, controlRodAssembly);
            }
            return FormationResult.SUCCESS;
        }

        public FormedAssembly build() {
            BlockPos base = fuelAssemblies.first();
            return new FormedAssembly(base, fuelAssemblies.size());
        }
    }

    public record FormedAssembly(BlockPos pos, int height) {

        public CompoundTag write() {
            CompoundTag ret = new CompoundTag();
            ret.put(SerializationConstants.POSITION, NbtUtils.writeBlockPos(pos));
            ret.putInt(SerializationConstants.HEIGHT, height);
            return ret;
        }

        @Nullable
        public static FormedAssembly read(CompoundTag nbt) {
            BlockPos blockPos = NbtUtils.readBlockPos(nbt, SerializationConstants.POSITION).orElse(null);
            if (blockPos == null) {
                return null;
            }
            return new FormedAssembly(blockPos, nbt.getInt(SerializationConstants.HEIGHT));
        }
    }

    private record AssemblyPos(int x, int z) {
    }
}
