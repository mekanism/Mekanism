package mekanism.generators.common.inventory.container.reactor.info;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.generators.common.inventory.container.GeneratorsContainerTypes;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class ReactorFuelContainer extends ReactorInfoContainer {

    public ReactorFuelContainer(int id, PlayerInventory inv, TileEntityReactorController tile) {
        super(GeneratorsContainerTypes.REACTOR_FUEL, id, inv, tile);
    }

    public ReactorFuelContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityReactorController.class));
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new ReactorFuelContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanismgenerators.container.reactor_fuel");
    }
}