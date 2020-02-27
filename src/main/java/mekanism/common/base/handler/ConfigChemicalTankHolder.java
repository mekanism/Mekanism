package mekanism.common.base.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.base.IChemicalTankHolder;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.util.Direction;

public abstract class ConfigChemicalTankHolder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements IChemicalTankHolder<CHEMICAL, STACK> {

    protected final List<IChemicalTank<CHEMICAL, STACK>> tanks = new ArrayList<>();
    protected final Supplier<TileComponentConfig> configSupplier;
    protected final Supplier<Direction> facingSupplier;

    ConfigChemicalTankHolder(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        this.facingSupplier = facingSupplier;
        this.configSupplier = configSupplier;
    }

    void addTank(@Nonnull IChemicalTank<CHEMICAL, STACK> tank) {
        tanks.add(tank);
    }
}