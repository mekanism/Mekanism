package mekanism.common.inventory.container.tile.electric;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.recipe.machines.EnrichmentRecipe;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class EnrichmentChamberContainer extends ElectricMachineContainer<EnrichmentRecipe, TileEntityEnrichmentChamber> {

    public EnrichmentChamberContainer(int id, PlayerInventory inv, TileEntityEnrichmentChamber tile) {
        super(MekanismContainerTypes.ENRICHMENT_CHAMBER, id, inv, tile);
    }

    public EnrichmentChamberContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityEnrichmentChamber.class));
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new EnrichmentChamberContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.enrichment_chamber");
    }
}