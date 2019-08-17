package mekanism.generators.common.inventory.container.passive;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.slot.SlotEnergy.SlotCharge;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.generators.common.inventory.container.GeneratorsContainerTypes;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class WindGeneratorContainer extends PassiveGeneratorContainer<TileEntityWindGenerator> {

    public WindGeneratorContainer(int id, PlayerInventory inv, TileEntityWindGenerator tile) {
        super(GeneratorsContainerTypes.WIND_GENERATOR, id, inv, tile);
    }

    public WindGeneratorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityWindGenerator.class));
    }

    @Override
    protected void addSlots() {
        addSlot(new SlotCharge(tile, 0, 143, 35));
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new WindGeneratorContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanismgenerators.container.wind_generator");
    }
}