package mekanism.common.tests.helpers;

import mekanism.api.AutomationType;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.resource.IResourceContainer;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.IContainerType;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class ContainerGameTestHelper extends MekGameTestHelper {

    public ContainerGameTestHelper(GameTestInfo info) {
        super(info);
    }

    public IChemicalTank getChemicalTank(Holder<Item> itemHolder) {
        return getContainer(ContainerType.CHEMICAL, itemHolder);
    }

    public IFluidTank getFluidTank(Holder<Item> itemHolder) {
        return getContainer(ContainerType.FLUID, itemHolder);
    }

    public IEnergyContainer getEnergyContainer(Holder<Item> itemHolder) {
        return getContainer(ContainerType.ENERGY, itemHolder);
    }

    private <CONTAINER extends ValueIOSerializable> CONTAINER getContainer(IContainerType<CONTAINER, ?> containerType, Holder<Item> itemHolder) {
        CONTAINER container = containerType.createContainer(ItemAccessUtils.sideEffectFreeAccess(ItemResource.of(itemHolder)), 0);
        assertNotNull(container, "Expected container to not be null");
        return container;
    }

    public <RESOURCE extends Resource> void testTransfer(IResourceContainer<RESOURCE> container, RESOURCE resource, int transferRate, AutomationType limited, AutomationType limitless) {
        int capacity = container.capacityAsInt(resource);
        testTransfer(container, resource, capacity, transferRate, limited, limitless, false);
        testTransfer(container, resource, capacity, transferRate, limited, limitless, true);
        try (Transaction transaction = Transaction.openRoot()) {
            assertTrue(container.extract(resource, capacity, transaction, limited) == 0, "Expected the container to not allow extracting given the rate was already used up this tick");
            assertTrue(container.extract(resource, capacity, transaction, limitless) == capacity, "Expected the container to allow extracting via a different automation type");
            assertTrue(container.insert(resource, capacity, transaction, limited) == 0, "Expected the container to not allow inserting given the rate was already used up this tick");
            assertTrue(container.insert(resource, capacity, transaction, limitless) == capacity, "Expected the container to allow inserting via a different automation type");
            //Reset the container for the next tick
            assertTrue(container.extract(resource, capacity, transaction, limitless) == capacity, "Expected the container to allow extracting via a different automation type");
            transaction.commit();
        }
    }
    
    private <RESOURCE extends Resource> void testTransfer(IResourceContainer<RESOURCE> container, RESOURCE resource, int capacity, int transferRate,
          AutomationType limited, AutomationType limitless, boolean commit) {
        int diff = capacity - transferRate;
        try (Transaction transaction = Transaction.openRoot()) {
            assertTrue(container.insert(resource, capacity, transaction, limitless) == capacity, "Expected the container to accept the entire capacity");
            assertTrue(container.extract(resource, capacity, transaction, limited) == transferRate, "Expected the container to only allow extracting " + transferRate);
            assertTrue(container.extract(resource, capacity, transaction, limited) == 0, "Expected the container to not allow extracting anything else this tick");
            assertTrue(container.extract(resource, capacity, transaction, limitless) == diff, "Expected the container to allow extracting the remaining amount via a different automation type");
            assertTrue(container.insert(resource, capacity, transaction, limited) == transferRate, "Expected the container to only accept " + transferRate);
            assertTrue(container.insert(resource, capacity, transaction, limited) == 0, "Expected the container to not accept anything else this tick");
            assertTrue(container.insert(resource, capacity, transaction, limitless) == diff, "Expected the container to accept the remaining amount when inserting with a different automation type");
            if (commit) {
                transaction.commit();
            }
        }
    }

    public void testTransfer(IEnergyContainer container, int transferRate, AutomationType limited, AutomationType limitless) {
        int capacity = container.getCapacityAsInt();
        testTransfer(container, capacity, transferRate, limited, limitless, false);
        testTransfer(container, capacity, transferRate, limited, limitless, true);
        try (Transaction transaction = Transaction.openRoot()) {
            assertTrue(container.extract(capacity, transaction, limited) == 0, "Expected the container to not allow extracting given the rate was already used up this tick");
            assertTrue(container.extract(capacity, transaction, limitless) == capacity, "Expected the container to allow extracting via a different automation type");
            assertTrue(container.insert(capacity, transaction, limited) == 0, "Expected the container to not allow inserting given the rate was already used up this tick");
            assertTrue(container.insert(capacity, transaction, limitless) == capacity, "Expected the container to allow inserting via a different automation type");
            //Reset the container for the next tick
            assertTrue(container.extract(capacity, transaction, limitless) == capacity, "Expected the container to allow extracting via a different automation type");
            transaction.commit();
        }
    }

    private void testTransfer(IEnergyContainer container, int capacity, int transferRate, AutomationType limited, AutomationType limitless, boolean commit) {
        int diff = capacity - transferRate;
        try (Transaction transaction = Transaction.openRoot()) {
            assertTrue(container.insert(capacity, transaction, limitless) == capacity, "Expected the container to accept the entire capacity");
            assertTrue(container.extract(capacity, transaction, limited) == transferRate, "Expected the container to only allow extracting " + transferRate);
            assertTrue(container.extract(capacity, transaction, limited) == 0, "Expected the container to not allow extracting anything else this tick");
            assertTrue(container.extract(capacity, transaction, limitless) == diff, "Expected the container to allow extracting the remaining amount via a different automation type");
            assertTrue(container.insert(capacity, transaction, limited) == transferRate, "Expected the container to only accept " + transferRate);
            assertTrue(container.insert(capacity, transaction, limited) == 0, "Expected the container to not accept anything else this tick");
            assertTrue(container.insert(capacity, transaction, limitless) == diff, "Expected the container to accept the remaining amount when inserting with a different automation type");
            if (commit) {
                transaction.commit();
            }
        }
    }
}