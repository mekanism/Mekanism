package mekanism.common.transmitters.grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import mekanism.api.Coord4D;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class InventoryNetwork extends DynamicNetwork<TileEntity, InventoryNetwork, Void> {

    public InventoryNetwork() {
    }

    public InventoryNetwork(Collection<InventoryNetwork> networks) {
        for (InventoryNetwork net : networks) {
            if (net != null) {
                adoptTransmittersAndAcceptorsFrom(net);
                net.deregister();
            }
        }

        register();
    }

    public List<AcceptorData> calculateAcceptors(TransitRequest request, TransporterStack stack) {
        List<AcceptorData> toReturn = new ArrayList<>();

        for (Coord4D coord : possibleAcceptors.keySet()) {
            if (coord == null || coord.equals(stack.homeLocation)) {
                continue;
            }

            EnumSet<EnumFacing> sides = acceptorDirections.get(coord);
            TileEntity acceptor = coord.getTileEntity(getWorld());

            if (sides == null || sides.isEmpty()) {
                continue;
            }

            AcceptorData data = null;

            for (EnumFacing side : sides) {
                TransitResponse response = TransporterManager
                      .getPredictedInsert(acceptor, stack.color, request, side.getOpposite());

                if (!response.isEmpty()) {
                    if (data == null) {
                        data = new AcceptorData(coord, response, side.getOpposite());
                    } else {
                        data.sides.add(side.getOpposite());
                    }
                }
            }

            if (data != null) {
                toReturn.add(data);
            }
        }

        return toReturn;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            //Future!
        }
    }

    @Override
    public void absorbBuffer(IGridTransmitter<TileEntity, InventoryNetwork, Void> transmitter) {
    }

    @Override
    public void clampBuffer() {
    }

    @Override
    public String toString() {
        return "[InventoryNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size()
              + " acceptors.";
    }

    @Override
    public String getNeededInfo() {
        return null;
    }

    @Override
    public String getStoredInfo() {
        return null;
    }

    @Override
    public String getFlowInfo() {
        return null;
    }

    public static class AcceptorData {

        public Coord4D location;
        public TransitResponse response;
        public EnumSet<EnumFacing> sides = EnumSet.noneOf(EnumFacing.class);

        public AcceptorData(Coord4D coord, TransitResponse ret, EnumFacing side) {
            location = coord;
            response = ret;
            sides.add(side);
        }
    }
}
