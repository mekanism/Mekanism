package mekanism.common.tile.prefab;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import mekanism.api.IConfigurable;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class TileEntityStructuralMultiblock extends TileEntityMekanism implements IStructuralMultiblock, IConfigurable {

    private final Map<MultiblockManager<?>, Structure> structures = new HashMap<>();
    private final Structure invalidStructure = Structure.INVALID;
    private final MultiblockData defaultMultiblock = new MultiblockData(this);

    private String clientActiveMultiblock = null;

    public TileEntityStructuralMultiblock(IBlockProvider provider, BlockPos pos, BlockState state) {
        super(provider, pos, state);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE, this));
    }

    @Override
    public MultiblockData getDefaultData() {
        return defaultMultiblock;
    }

    @Override
    public void setStructure(MultiblockManager<?> manager, Structure structure) {
        structures.put(manager, structure);
    }

    @Override
    public Structure getStructure(MultiblockManager<?> manager) {
        return structures.getOrDefault(manager, invalidStructure);
    }

    @Override
    public boolean hasStructure(Structure structure) {
        return structures.get(structure.getManager()) == structure;
    }

    @Override
    public boolean hasFormedMultiblock() {
        return clientActiveMultiblock != null;
    }

    @Override
    public boolean structuralGuiAccessAllowed() {
        return hasFormedMultiblock() && structuralGuiAccessAllowed(clientActiveMultiblock);
    }

    protected boolean structuralGuiAccessAllowed(@NotNull String multiblock) {
        return !multiblock.contains("fusion") && !multiblock.contains("evaporation");
    }

    @Override
    public Map<MultiblockManager<?>, Structure> getStructureMap() {
        return structures;
    }

    private MultiblockData getMultiblockData(Structure structure) {
        //Like the getMultiblockData(MultiblockManager) method except can assume the structure is indeed in our structures map,
        // so we can slightly short circuit lookup
        MultiblockData data = structure.getMultiblockData();
        if (data != null && data.isFormed()) {
            return data;
        }
        return getDefaultData();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (ticker % 10 == 0) {
            String activeMultiblock = null;
            if (!structures.isEmpty()) {
                Iterator<Map.Entry<MultiblockManager<?>, Structure>> iterator = structures.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<MultiblockManager<?>, Structure> entry = iterator.next();
                    Structure structure = entry.getValue();
                    if (structure.isValid()) {
                        if (activeMultiblock == null && structure.getController() != null && getMultiblockData(structure).isFormed()) {
                            activeMultiblock = entry.getKey().getNameLower();
                        }
                    } else {
                        iterator.remove();
                    }
                }
            }
            if (ticker >= 3 && structures.isEmpty()) {
                invalidStructure.tick(this, true);
                //If we managed to find any structures check which one is active
                for (Map.Entry<MultiblockManager<?>, Structure> entry : structures.entrySet()) {
                    Structure structure = entry.getValue();
                    if (structure.getController() != null && getMultiblockData(structure).isFormed()) {
                        activeMultiblock = entry.getKey().getNameLower();
                        break;
                    }
                }
            }
            // this could potentially fail if this structural multiblock tracks multiple structures, but 99.99% of the time this will be accurate
            if (!Objects.equals(activeMultiblock, clientActiveMultiblock)) {
                clientActiveMultiblock = activeMultiblock;
                sendUpdatePacket();
            }
        }
    }

    @Override
    public InteractionResult onActivate(Player player, InteractionHand hand, ItemStack stack) {
        for (Map.Entry<MultiblockManager<?>, Structure> entry : structures.entrySet()) {
            Structure structure = entry.getValue();
            IMultiblock<?> master = structure.getController();
            if (master != null) {
                MultiblockData data = getMultiblockData(structure);
                if (data.isFormed() && structuralGuiAccessAllowed(entry.getKey().getNameLower())) {
                    // make sure this block is on the structure first
                    if (data.getBounds().getRelativeLocation(getBlockPos()).isWall()) {
                        return master.onActivate(player, hand, stack);
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onNeighborChange(Block block, BlockPos neighborPos) {
        super.onNeighborChange(block, neighborPos);
        if (!isRemote()) {
            //TODO - V11: Make this properly support changing blocks inside the structure when they aren't touching any part of the multiblocks
            for (Structure s : structures.values()) {
                //For each structure this structural multiblock is a part of
                if (s.getController() != null) {
                    MultiblockData multiblockData = getMultiblockData(s);
                    if (multiblockData.isPositionInsideBounds(s, neighborPos)) {
                        if (level.isEmptyBlock(neighborPos) || !multiblockData.internalLocations.contains(neighborPos)) {
                            //And we are not already an internal part of the structure, or we are changing an internal part to air
                            // then we mark the structure as needing to be re-validated
                            //Note: This isn't a super accurate check as if a node gets replaced by command or mod with say dirt
                            // it won't know to invalidate it but oh well. (See java docs on internalLocations for more caveats)
                            s.markForUpdate(level, true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public InteractionResult onRightClick(Player player) {
        if (!isRemote()) {
            for (Structure s : structures.values()) {
                if (s.getController() != null && !getMultiblockData(s).isFormed()) {
                    FormationResult result = s.runUpdate(this);
                    if (!result.isFormed() && result.getResultText() != null) {
                        player.sendSystemMessage(result.getResultText());
                        return InteractionResult.sidedSuccess(isRemote());
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onSneakRightClick(Player player) {
        return InteractionResult.PASS;
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag() {
        CompoundTag updateTag = super.getReducedUpdateTag();
        if (clientActiveMultiblock != null) {
            updateTag.putString(NBTConstants.ACTIVE_STATE, clientActiveMultiblock);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag) {
        super.handleUpdateTag(tag);
        clientActiveMultiblock = tag.contains(NBTConstants.ACTIVE_STATE, Tag.TAG_STRING) ? tag.getString(NBTConstants.ACTIVE_STATE) : null;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!isRemote()) {
            structures.values().forEach(s -> s.invalidate(level));
        }
    }

    @Override
    public boolean shouldDumpRadiation() {
        //We handle dumping radiation separately for multiblocks
        return false;
    }
}