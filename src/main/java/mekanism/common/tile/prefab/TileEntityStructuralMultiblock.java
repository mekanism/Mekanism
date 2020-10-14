package mekanism.common.tile.prefab;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import mekanism.api.IConfigurable;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

public abstract class TileEntityStructuralMultiblock extends TileEntityMekanism implements IStructuralMultiblock, IConfigurable {

    private final Map<MultiblockManager<?>, Structure> structures = new HashMap<>();
    private final Structure invalidStructure = Structure.INVALID;
    private final MultiblockData defaultMultiblock = new MultiblockData(this);

    private String clientActiveMultiblock = null;

    public TileEntityStructuralMultiblock(IBlockProvider provider) {
        super(provider);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE_CAPABILITY, this));
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
        return hasFormedMultiblock() && !clientActiveMultiblock.contains("fusion") && !clientActiveMultiblock.contains("evaporation");
    }

    @Override
    public Map<MultiblockManager<?>, Structure> getStructureMap() {
        return structures;
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        structures.entrySet().removeIf(entry -> !entry.getValue().isValid());
        if (ticker >= 3 && structures.isEmpty()) {
            invalidStructure.tick(this);
        }
        // this could potentially fail if this structural multiblock tracks multiple structures, but 99.99% of the time this will be accurate
        String activeMultiblock = null;
        for (Structure s : structures.values()) {
            IMultiblock<?> master = s.getController();
            if (master != null && getMultiblockData(s.getManager()).isFormed()) {
                activeMultiblock = master.getManager().getName().toLowerCase(Locale.ROOT);
                break;
            }
        }
        if (!Objects.equals(activeMultiblock, clientActiveMultiblock)) {
            clientActiveMultiblock = activeMultiblock;
            sendUpdatePacket();
        }
    }

    @Override
    public ActionResultType onActivate(PlayerEntity player, Hand hand, ItemStack stack) {
        for (Map.Entry<MultiblockManager<?>, Structure> entry : structures.entrySet()) {
            IMultiblock<?> master = entry.getValue().getController();
            if (master != null && getMultiblockData(entry.getKey()).isFormed()) {
                // make sure this block is on the structure first
                if (entry.getValue().getMultiblockData().getBounds().getRelativeLocation(getPos()).isWall()) {
                    return master.onActivate(player, hand, stack);
                }
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public void onNeighborChange(Block block, BlockPos neighborPos) {
        super.onNeighborChange(block, neighborPos);
        if (!isRemote()) {
            //TODO - V11: Make this properly support removing blocks from the "inside" and rechecking the structure
            // For now we "ignore" this case as the structure can be rechecked manually with a configurator
            // and checking on every neighbor changed when we don't have a multiblock (so don't know its bounds)
            // would not be very performant
            for (Structure s : structures.values()) {
                //For each structure this structural multiblock is a part of
                if (s.getController() != null) {
                    MultiblockData multiblockData = getMultiblockData(s.getManager());
                    if (multiblockData.isPositionInsideBounds(s, neighborPos)) {
                        if (!multiblockData.innerNodes.contains(neighborPos) || world.isAirBlock(neighborPos)) {
                            //And we are not already an internal part of the structure, or we are changing an internal part to air
                            // then we mark the structure as needing to be re-validated
                            //Note: This isn't a super accurate check as if a node gets replaced by command or mod with say dirt
                            // it won't know to invalidate it but oh well. (See java docs on innerNode for more caveats)
                            s.markForUpdate(world, true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        if (!isRemote()) {
            for (Structure s : structures.values()) {
                IMultiblock<?> master = s.getController();
                if (master != null && !getMultiblockData(s.getManager()).isFormed()) {
                    FormationResult result = s.runUpdate(this);
                    if (!result.isFormed() && result.getResultText() != null) {
                        player.sendMessage(result.getResultText(), Util.DUMMY_UUID);
                        return ActionResultType.SUCCESS;
                    }
                }
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        if (clientActiveMultiblock != null) {
            updateTag.putString(NBTConstants.ACTIVE_STATE, clientActiveMultiblock);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(BlockState state, @Nonnull CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        clientActiveMultiblock = tag.contains(NBTConstants.ACTIVE_STATE, NBT.TAG_STRING) ? tag.getString(NBTConstants.ACTIVE_STATE) : null;
    }

    @Override
    public void remove() {
        super.remove();
        if (!isRemote()) {
            structures.values().forEach(s -> s.invalidate(world));
        }
    }

    @Override
    protected void dumpRadiation() {
        //NO-OP we handle dumping radiation separately for multiblocks
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (!isRemote()) {
            structures.values().forEach(s -> s.invalidate(world));
        }
    }
}