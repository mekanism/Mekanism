package mekanism.common.inventory.container.tile;

import javax.annotation.Nonnull;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class TransporterConfigurationContainer<TILE extends TileEntityMekanism & ISideConfiguration> extends MekanismTileContainer<TILE> implements IEmptyContainer {

    public TransporterConfigurationContainer(int id, PlayerInventory inv, TILE tile) {
        super(MekanismContainerTypes.TRANSPORTER_CONFIGURATION, id, inv, tile);
    }

    public TransporterConfigurationContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        //TODO
        this(id, inv, (TILE) getTileFromBuf(buf, TileEntityMekanism.class));
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.transporter_configuration");
    }
}