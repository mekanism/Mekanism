package mekanism.common.capabilities.holder.chemical;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.holder.ConfigHolder;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo.GasSlotInfo;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo.InfusionSlotInfo;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo.PigmentSlotInfo;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo.SlurrySlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import net.minecraft.util.Direction;

public abstract class ConfigChemicalTankHolder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
      extends ConfigHolder implements IChemicalTankHolder<CHEMICAL, STACK, TANK> {

    protected final List<TANK> tanks = new ArrayList<>();

    protected ConfigChemicalTankHolder(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        super(facingSupplier, configSupplier);
    }

    void addTank(@Nonnull TANK tank) {
        tanks.add(tank);
    }

    @Nonnull
    protected abstract List<TANK> getTanksFromSlot(ISlotInfo slotInfo);

    @Nonnull
    @Override
    public List<TANK> getTanks(@Nullable Direction direction) {
        if (direction == null) {
            //If we want the internal, give all of our slots
            return tanks;
        }
        TileComponentConfig config = configSupplier.get();
        if (config == null) {
            //If we don't have a config (most likely case is it hasn't been setup yet), just return all slots
            return tanks;
        }
        ConfigInfo configInfo = config.getConfig(getTransmissionType());
        if (configInfo == null) {
            //We don't support the given chemical type in our configuration at all so just return all
            return tanks;
        }
        RelativeSide side = RelativeSide.fromDirections(facingSupplier.get(), direction);
        ISlotInfo slotInfo = configInfo.getSlotInfo(side);
        return slotInfo != null && slotInfo.isEnabled() ? getTanksFromSlot(slotInfo) : Collections.emptyList();
    }

    public static class ConfigGasTankHolder extends ConfigChemicalTankHolder<Gas, GasStack, IGasTank> {

        public ConfigGasTankHolder(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
            super(facingSupplier, configSupplier);
        }

        @Override
        protected TransmissionType getTransmissionType() {
            return TransmissionType.GAS;
        }

        @Nonnull
        @Override
        protected List<IGasTank> getTanksFromSlot(ISlotInfo slotInfo) {
            return slotInfo instanceof GasSlotInfo ? ((GasSlotInfo) slotInfo).getTanks() : Collections.emptyList();
        }
    }

    public static class ConfigInfusionTankHolder extends ConfigChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> {

        public ConfigInfusionTankHolder(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
            super(facingSupplier, configSupplier);
        }

        @Override
        protected TransmissionType getTransmissionType() {
            return TransmissionType.INFUSION;
        }

        @Nonnull
        @Override
        protected List<IInfusionTank> getTanksFromSlot(ISlotInfo slotInfo) {
            return slotInfo instanceof InfusionSlotInfo ? ((InfusionSlotInfo) slotInfo).getTanks() : Collections.emptyList();
        }
    }

    public static class ConfigPigmentTankHolder extends ConfigChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> {

        public ConfigPigmentTankHolder(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
            super(facingSupplier, configSupplier);
        }

        @Override
        protected TransmissionType getTransmissionType() {
            return TransmissionType.PIGMENT;
        }

        @Nonnull
        @Override
        protected List<IPigmentTank> getTanksFromSlot(ISlotInfo slotInfo) {
            return slotInfo instanceof PigmentSlotInfo ? ((PigmentSlotInfo) slotInfo).getTanks() : Collections.emptyList();
        }
    }

    public static class ConfigSlurryTankHolder extends ConfigChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> {

        public ConfigSlurryTankHolder(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
            super(facingSupplier, configSupplier);
        }

        @Override
        protected TransmissionType getTransmissionType() {
            return TransmissionType.SLURRY;
        }

        @Nonnull
        @Override
        protected List<ISlurryTank> getTanksFromSlot(ISlotInfo slotInfo) {
            return slotInfo instanceof SlurrySlotInfo ? ((SlurrySlotInfo) slotInfo).getTanks() : Collections.emptyList();
        }
    }
}