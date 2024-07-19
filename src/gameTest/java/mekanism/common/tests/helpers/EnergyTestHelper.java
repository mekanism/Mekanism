package mekanism.common.tests.helpers;

import java.util.Collections;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.integration.energy.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.integration.energy.forgeenergy.ForgeStrictEnergyHandler;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestInfo;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EnergyTestHelper extends MekGameTestHelper {

    public EnergyTestHelper(GameTestInfo info) {
        super(info);
    }

    public IEnergyStorage createForgeWrappedStrictEnergyHandler(long energy, long capacityJoules) {
        IEnergyContainer container = BasicEnergyContainer.create(capacityJoules, null);
        container.setEnergy(energy);
        return createForgeWrappedStrictEnergyHandler(container);
    }

    public IEnergyStorage createForgeWrappedStrictEnergyHandler(IEnergyContainer container) {
        List<IEnergyContainer> containers = Collections.singletonList(container);
        return new ForgeEnergyIntegration(new IMekanismStrictEnergyHandler() {
            @Override
            public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
                return containers;
            }

            @Override
            public void onContentsChanged() {
            }
        });
    }

    public IStrictEnergyHandler createStrictForgeEnergyHandler(int energy, int capacityFE) {
        return new ForgeStrictEnergyHandler(new EnergyStorage(capacityFE, capacityFE, capacityFE, energy));
    }
}