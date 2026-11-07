package mekanism.common.integration.computer.opencomputers;

import java.util.List;
import li.cil.oc.api.Network;
import li.cil.oc.api.UnrecoverablePersistanceException;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import mekanism.common.integration.computer.BoundMethodHolder;
import mekanism.common.integration.computer.IComputerTile;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.component.DataComponentHolder;
import net.neoforged.neoforge.common.MutableDataComponentHolder;

final class MekanismEnvironment<TILE extends BlockEntity & IComputerTile> extends BoundMethodHolder
      implements ManagedEnvironment, ManagedPeripheral {

    private final Node node;

    MekanismEnvironment(TILE tile) {
        tile.getComputerMethods(this);
        node = Network.newNode(this, Visibility.Network).withComponent(tile.getComputerName(), Visibility.Network).create();
        Network.joinNewNetwork(node);
    }

    @Override
    public Node node() {
        return node;
    }

    @Override
    public void onConnect(Node node) {
    }

    @Override
    public void onDisconnect(Node node) {
    }

    @Override
    public void onMessage(Message message) {
    }

    @Override
    public void loadData(DataComponentHolder holder) throws UnrecoverablePersistanceException {
        if (node != null) {
            node.loadData(holder);
        }
    }

    @Override
    public void saveData(MutableDataComponentHolder holder) {
        if (node != null) {
            node.saveData(holder);
        }
    }

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    public void update() {
    }

    @Override
    public String[] methods() {
        return methods.keySet().toArray(new String[0]);
    }

    @Override
    public Object[] invoke(String method, Context context, Arguments arguments) throws Exception {
        List<BoundMethodData<?>> overloads = methods.get(method);
        for (BoundMethodData<?> methodData : overloads) {
            if (methodData.argumentNames().length == arguments.count()) {
                Object result = methodData.call(new OCComputerHelper(arguments));
                return result == null ? null : new Object[]{result};
            }
        }
        throw new NoSuchMethodException(method + "/" + arguments.count());
    }
}
