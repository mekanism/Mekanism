package mekanism.common.inventory.container.tile.advanced;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.recipe.machines.PurificationRecipe;
import mekanism.common.tile.TileEntityPurificationChamber;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class PurificationChamberContainer extends AdvancedElectricMachineContainer<PurificationRecipe, TileEntityPurificationChamber> {

    public PurificationChamberContainer(int id, PlayerInventory inv, TileEntityPurificationChamber tile) {
        super(MekanismContainerTypes.PURIFICATION_CHAMBER, id, inv, tile);
    }

    public PurificationChamberContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityPurificationChamber.class));
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.osmium_compressor");
    }
}