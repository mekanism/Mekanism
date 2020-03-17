package mekanism.common.content.boiler;

import java.util.List;
import java.util.Set;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.tile.TileEntityBoilerValve;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.tile.TileEntitySuperheatingElement;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class BoilerUpdateProtocol extends UpdateProtocol<SynchronizedBoilerData> {

    public static final int WATER_PER_TANK = 16_000;
    public static final int STEAM_PER_TANK = 160_000;

    public BoilerUpdateProtocol(TileEntityBoilerCasing tile) {
        super(tile);
    }

    @Override
    protected boolean isValidFrame(int x, int y, int z) {
        return BlockTypeTile.is(pointer.getWorld().getBlockState(new BlockPos(x, y, z)).getBlock(), MekanismBlockTypes.BOILER_CASING, MekanismBlockTypes.BOILER_VALVE);
    }

    @Override
    protected boolean isValidInnerNode(int x, int y, int z) {
        if (super.isValidInnerNode(x, y, z)) {
            return true;
        }
        TileEntity tile = MekanismUtils.getTileEntity(pointer.getWorld(), new BlockPos(x, y, z));
        return tile instanceof TileEntityPressureDisperser || tile instanceof TileEntitySuperheatingElement;
    }

    @Override
    protected boolean canForm(SynchronizedBoilerData structure) {
        if (structure.volHeight < 3) {
            return false;
        }
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
            return false;
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
                    return false;
                }
                dispersers.remove(new Coord4D(shifted, pointer.getWorld()));
            }
        }

        //If there are more dispersers than those on the plane found, the structure is invalid
        if (!dispersers.isEmpty()) {
            return false;
        }

        if (!elements.isEmpty()) {
            structure.superheatingElements = new NodeCounter(new NodeChecker() {
                @Override
                public boolean isValid(Coord4D coord) {
                    return MekanismUtils.getTileEntity(TileEntitySuperheatingElement.class, pointer.getWorld(), coord.getPos()) != null;
                }
            }).calculate(elements.iterator().next());
        }

        if (elements.size() > structure.superheatingElements) {
            return false;
        }

        Coord4D initAir = null;
        int totalAir = 0;

        //Find the first available block in the structure for water storage (including casings)
        for (int x = structure.renderLocation.x; x < structure.renderLocation.x + structure.volLength; x++) {
            for (int y = structure.renderLocation.y; y < initDisperser.y; y++) {
                for (int z = structure.renderLocation.z; z < structure.renderLocation.z + structure.volWidth; z++) {
                    if (pointer.getWorld().isAirBlock(new BlockPos(x, y, z)) || isViableNode(x, y, z)) {
                        initAir = new Coord4D(x, y, z, pointer.getWorld().getDimension().getType());
                        totalAir++;
                    }
                }
            }
        }

        //Some air must exist for the structure to be valid
        if (initAir == null) {
            return false;
        }

        //Gradle build requires these fields to be final
        final Coord4D renderLocation = structure.renderLocation;
        final int volLength = structure.volLength;
        final int volWidth = structure.volWidth;
        structure.waterVolume = new NodeCounter(new NodeChecker() {
            @Override
            public final boolean isValid(Coord4D coord) {
                BlockPos coordPos = coord.getPos();
                int x = coordPos.getX();
                int y = coordPos.getY();
                int z = coordPos.getZ();
                return y >= renderLocation.y - 1 && y < initDisperser.y &&
                       x >= renderLocation.x && x < renderLocation.x + volLength &&
                       z >= renderLocation.z && z < renderLocation.z + volWidth &&
                       (pointer.getWorld().isAirBlock(coordPos) || isViableNode(coordPos));
            }
        }).calculate(initAir);

        //Make sure all air blocks are connected
        if (totalAir > structure.waterVolume) {
            return false;
        }

        int steamHeight = (structure.renderLocation.y + structure.volHeight - 2) - initDisperser.y;
        structure.steamVolume = structure.volWidth * structure.volLength * steamHeight;
        structure.upperRenderLocation = new Coord4D(structure.renderLocation.x, initDisperser.y + 1, structure.renderLocation.z, pointer.getWorld().getDimension().getType());
        return true;
    }

    @Override
    protected BoilerCache getNewCache() {
        return new BoilerCache();
    }

    @Override
    protected SynchronizedBoilerData getNewStructure() {
        return new SynchronizedBoilerData((TileEntityBoilerCasing) pointer);
    }

    @Override
    protected MultiblockManager<SynchronizedBoilerData> getManager() {
        return Mekanism.boilerManager;
    }

    @Override
    protected void mergeCaches(List<ItemStack> rejectedItems, MultiblockCache<SynchronizedBoilerData> cache, MultiblockCache<SynchronizedBoilerData> merge) {
        BoilerCache boilerCache = (BoilerCache) cache;
        BoilerCache mergeCache = (BoilerCache) merge;
        StorageUtils.mergeTanks(boilerCache.getFluidTanks(null).get(0), mergeCache.getFluidTanks(null).get(0));
        StorageUtils.mergeTanks(boilerCache.getGasTanks(null).get(0), mergeCache.getGasTanks(null).get(0));
        boilerCache.temperature = Math.max(boilerCache.temperature, mergeCache.temperature);
    }

    @Override
    protected void onFormed() {
        super.onFormed();
        if (!structureFound.waterTank.isEmpty()) {
            structureFound.waterTank.setStackSize(Math.min(structureFound.waterTank.getFluidAmount(), structureFound.waterTank.getCapacity()), Action.EXECUTE);
        }
        if (!structureFound.steamTank.isEmpty()) {
            structureFound.steamTank.setStackSize(Math.min(structureFound.steamTank.getStored(), structureFound.steamTank.getCapacity()), Action.EXECUTE);
        }
    }

    @Override
    protected void onStructureCreated(SynchronizedBoilerData structure, int origX, int origY, int origZ, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax) {
        for (Coord4D obj : structure.locations) {
            if (MekanismUtils.getTileEntity(pointer.getWorld(), obj.getPos()) instanceof TileEntityBoilerValve) {
                ValveData data = new ValveData();
                data.location = obj;
                data.side = getSide(obj, origX + xmin, origX + xmax, origY + ymin, origY + ymax, origZ + zmin, origZ + zmax);
                structure.valves.add(data);
            }
        }
    }
}