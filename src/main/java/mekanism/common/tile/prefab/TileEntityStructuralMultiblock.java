package mekanism.common.tile.prefab;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.IConfigurable;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;

public abstract class TileEntityStructuralMultiblock extends TileEntityMekanism implements IStructuralMultiblock, IConfigurable {

    private final Map<MultiblockManager<?>, Structure> structures = new HashMap<>();
    private final Structure invalidStructure = Structure.INVALID;
    private final MultiblockData defaultMultiblock = new MultiblockData(this);

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
    public Map<MultiblockManager<?>, Structure> getStructureMap() {
        return structures;
    }

    @Override
    public void onUpdateServer() {
        super.onUpdateServer();
        structures.entrySet().removeIf(entry -> !entry.getValue().isValid());
        if (ticker >= 3 && structures.isEmpty()) {
            invalidStructure.tick(this);
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
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        if (!isRemote()) {
            for (Structure s : structures.values()) {
                IMultiblock<?> master = s.getController();
                if (master != null && !getMultiblockData(s.getManager()).isFormed()) {
                    FormationResult result = s.runUpdate(this);
                    if (!result.isFormed() && result.getResultText() != null) {
                        player.sendMessage(result.getResultText());
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

    @Override
    public void remove() {
        super.remove();
        if (!isRemote()) {
            structures.values().forEach(s -> s.invalidate(world));
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (!isRemote()) {
            structures.values().forEach(s -> s.invalidate(world));
        }
    }
}