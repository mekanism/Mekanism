package mekanism.common.inventory.container.tile.advanced;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.recipe.machines.OsmiumCompressorRecipe;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class OsmiumCompressorContainer extends AdvancedElectricMachineContainer<OsmiumCompressorRecipe, TileEntityOsmiumCompressor> {

    public OsmiumCompressorContainer(int id, PlayerInventory inv, TileEntityOsmiumCompressor tile) {
        super(MekanismContainerTypes.OSMIUM_COMPRESSOR, id, inv, tile);
    }

    public OsmiumCompressorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityOsmiumCompressor.class));
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new OsmiumCompressorContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.osmium_compressor");
    }
}