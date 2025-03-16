package mekanism.generators.common.tile.fission;

import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelProperty;

//TODO: Eventually make use of the commented out code in this class which is required to get model data working properly for use of the FuelAssemblyBakedModel
public class TileEntityFissionAssembly extends TileEntityInternalMultiblock {

    //private static final Map<UUID, MultiblockPairing> CACHED_MULTIBLOCKS = new HashMap<>();
    public static final ModelProperty<Void> GLOWING = new ModelProperty<>();

    public TileEntityFissionAssembly(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    /*@Override
    public void setRemoved() {
        if (isRemote()) {
            removeMultiblock(getMultiblockUUID(), this);
        }
        super.setRemoved();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (isRemote() && addMultiblock(getMultiblockUUID(), this)) {
            updateModelData();
        }
    }

    @Override
    protected void multiblockChanged(@Nullable UUID old) {
        super.multiblockChanged(old);
        if (isRemote()) {
            //Note: We use binary or here to ensure that we first remove and then add but only update the model data
            // once and only if one of them needed it
            if (removeMultiblock(old, this) | addMultiblock(getMultiblockUUID(), this)) {
                updateModelData();
            }
        }
    }

    @NotNull
    @Override
    public ModelData getModelData() {
        UUID multiblockUUID = getMultiblockUUID();
        if (multiblockUUID != null) {
            MultiblockPairing multiblockPairing = CACHED_MULTIBLOCKS.get(multiblockUUID);
            if (multiblockPairing != null && multiblockPairing.master != null) {
                FissionReactorMultiblockData multiblock = multiblockPairing.master.getMultiblock();
                if (multiblock.isFormed() && multiblock.isBurning()) {
                    return ModelData.builder().with(GLOWING, null).build();
                }
            }
        }
        return super.getModelData();
    }

    private static boolean addMultiblock(@Nullable UUID multiblockID, TileEntityFissionAssembly internal) {
        if (multiblockID != null) {
            MultiblockPairing multiblockPairing = CACHED_MULTIBLOCKS.computeIfAbsent(multiblockID, id -> new MultiblockPairing());
            //If we added it and there is a master return that we need to invalidate the model data
            return multiblockPairing.internalMultiblocks.add(internal) && multiblockPairing.master != null;
        }
        return false;
    }

    private static boolean removeMultiblock(@Nullable UUID multiblockID, TileEntityFissionAssembly internal) {
        if (multiblockID != null) {
            MultiblockPairing multiblockPairing = CACHED_MULTIBLOCKS.get(multiblockID);
            if (multiblockPairing != null && multiblockPairing.internalMultiblocks.remove(internal)) {
                if (multiblockPairing.master != null) {
                    //There is a master the multiblock needs to invalidate the model data
                    return true;
                }
                CACHED_MULTIBLOCKS.remove(multiblockID);
            }
        }
        return false;
    }

    public static void removeMultiblockMaster(@Nullable UUID multiblockID, TileEntityFissionReactorCasing master) {
        if (multiblockID != null) {
            MultiblockPairing multiblockPairing = CACHED_MULTIBLOCKS.get(multiblockID);
            if (multiblockPairing != null && multiblockPairing.master == master) {
                multiblockPairing.master = null;
                if (multiblockPairing.internalMultiblocks.isEmpty()) {
                    CACHED_MULTIBLOCKS.remove(multiblockID);
                } else {
                    multiblockPairing.requestUpdate();
                }
            }
        }
    }

    public static void updateMultiblockMaster(@Nullable UUID multiblockID, TileEntityFissionReactorCasing master) {
        if (multiblockID != null) {
            MultiblockPairing multiblockPairing = CACHED_MULTIBLOCKS.computeIfAbsent(multiblockID, id -> new MultiblockPairing());
            multiblockPairing.master = master;
            multiblockPairing.requestUpdate();
        }
    }

    private static class MultiblockPairing {

        private final Set<TileEntityFissionAssembly> internalMultiblocks = new HashSet<>();
        @Nullable
        public TileEntityFissionReactorCasing master;

        private void requestUpdate() {
            for (TileEntityFissionAssembly internalMultiblock : internalMultiblocks) {
                internalMultiblock.updateModelData();
            }
        }
    }*/
}