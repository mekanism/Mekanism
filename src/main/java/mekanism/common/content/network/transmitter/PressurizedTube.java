package mekanism.common.content.network.transmitter;

import com.google.common.primitives.Ints;
import java.util.Collection;
import java.util.UUID;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.container.LargeResourceStack;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.network.ChemicalNetwork;
import mekanism.common.lib.transmitter.CompatibleTransmitterValidator;
import mekanism.common.lib.transmitter.CompatibleTransmitterValidator.CompatibleChemicalTransmitterValidator;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.acceptor.AbstractAcceptorCache;
import mekanism.common.lib.transmitter.acceptor.AcceptorCache;
import mekanism.common.tier.TubeTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.upgrade.transmitter.PressurizedTubeUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PressurizedTube extends BufferedResourceTransmitter<ChemicalResource, IChemicalTank, ChemicalNetwork, PressurizedTube>
      implements IUpgradeableTransmitter<PressurizedTubeUpgradeData> {

    public final TubeTier tier;

    public PressurizedTube(Holder<Block> blockProvider, TileEntityTransmitter tile) {
        this.tier = Attribute.getTier(blockProvider, TubeTier.class);
        super(tile, SerializerHelper.CHEMICAL_RESOURCE_STACK_CODEC, BasicChemicalTank::createAllValid, TransmissionType.CHEMICAL);
    }

    @Override
    protected AbstractAcceptorCache<ResourceHandler<ChemicalResource>, ?> createAcceptorCache() {
        return new AcceptorCache<>(getTransmitterTile(), Capabilities.CHEMICAL.block());
    }

    @Override
    public LargeResourceStack<ChemicalResource> getEmptyResourceStack() {
        return LargeResourceStack.EMPTY_CHEMICAL_STACK;
    }

    @Override
    public TubeTier getTier() {
        return tier;
    }

    @Override
    protected int getAvailablePull() {
        //TODO - 26.1: Make available pull use ints natively
        return Ints.saturatedCast(Math.min(tier.getTubePullAmount(), getContainer().getNeededAsLong()));
    }

    @Nullable
    @Override
    public PressurizedTubeUpgradeData getUpgradeData() {
        return new PressurizedTubeUpgradeData(redstoneReactive, getConnectionTypesRaw(), getShare());
    }

    @Override
    public boolean dataTypeMatches(@NotNull TransmitterUpgradeData data) {
        return data instanceof PressurizedTubeUpgradeData;
    }

    @Override
    public void parseUpgradeData(@NotNull PressurizedTubeUpgradeData data) {
        redstoneReactive = data.redstoneReactive;
        setConnectionTypesRaw(data.connectionTypes);
        getContainer().setContentsUnchecked(data.contents.resource(), data.contents.amount());
    }

    @Override
    public ChemicalNetwork createEmptyNetworkWithID(UUID networkID) {
        return new ChemicalNetwork(networkID);
    }

    @Override
    public ChemicalNetwork createNetworkByMerging(Collection<ChemicalNetwork> toMerge) {
        return new ChemicalNetwork(toMerge);
    }

    @Override
    public CompatibleTransmitterValidator<ResourceHandler<ChemicalResource>, ChemicalNetwork, PressurizedTube> getNewOrphanValidator() {
        return new CompatibleChemicalTransmitterValidator(this);
    }

    @Override
    public boolean isValidTransmitter(TileEntityTransmitter transmitter, Direction side) {
        return super.isValidTransmitter(transmitter, side) && transmitter.getTransmitter() instanceof PressurizedTube other && isValidTransmitter(other);
    }

    @Override
    public long getCapacity() {
        return tier.getTubeCapacity();
    }

    @Override
    protected void handleContentsUpdateTag(@NotNull ChemicalNetwork network, @NotNull ValueInput input) {
        super.handleContentsUpdateTag(network, input);
        network.currentScale = input.getFloatOr(SerializationConstants.SCALE, network.currentScale);
        network.setLastType(input.read(SerializationConstants.CHEMICAL, ChemicalResource.CODEC).orElse(ChemicalResource.EMPTY));
    }

    public float getRadiationScale() {
        IChemicalTank chemicalTank = getContainer();
        ChemicalResource resource = chemicalTank.getResource();
        if (!resource.isEmpty() && resource.isRadioactive()) {
            return chemicalTank.amountAsLong() / (float) chemicalTank.getLimitAsLong(resource);
        }
        return 0;
    }
}