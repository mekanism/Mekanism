package mekanism.common.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.container.IMekanismResourceHandler;
import mekanism.api.container.IResourceContainer;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

//TODO - 26.1: Docs
public final class ResourceUtils {

    private ResourceUtils() {
    }

    public static <RESOURCE extends Resource> int extractManual(ResourceHandler<RESOURCE> handler, RESOURCE resource, int amount, TransactionContext transaction) {
        if (handler instanceof IMekanismResourceHandler<RESOURCE, ?> mekHandler) {
            //Ensure droppers use the manual automation type
            return mekHandler.extract(resource, amount, transaction, AutomationType.MANUAL);
        }
        return handler.extract(resource, amount, transaction);
    }

    public static <RESOURCE extends Resource> int insertManual(ResourceHandler<RESOURCE> handler, RESOURCE resource, int amount, TransactionContext transaction) {
        if (handler instanceof IMekanismResourceHandler<RESOURCE, ?> mekHandler) {
            //Ensure droppers use the manual automation type
            return mekHandler.insert(resource, amount, transaction, AutomationType.MANUAL);
        }
        return handler.insert(resource, amount, transaction);
    }

    //TODO - 26.1: validate and then add as docs that we don't need to also be modifying toAdd
    public static <RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> void merge(@NotNull List<CONTAINER> orig, @NotNull List<CONTAINER> toAdd,
          Object2IntMap<RESOURCE> rejects, TransactionContext transaction) {
        StorageUtils.validateSizeMatches(orig, toAdd, "container");
        for (int i = 0, slotCount = toAdd.size(); i < slotCount; i++) {
            CONTAINER toAddSlot = toAdd.get(i);
            if (!toAddSlot.isEmpty()) {
                RESOURCE toAddResource = toAddSlot.getResource();
                int toAddAmount = toAddSlot.amount();
                //TODO - 26.1: Validate all callers have this work with the given automation type
                // Also how much do we care about merging identical slots? Should we use the InventoryUtils#insertItem helper
                // to try inserting against all the slots of the other?
                int added = orig.get(i).insert(toAddResource, toAddAmount, transaction, AutomationType.INTERNAL);
                if (added < toAddAmount) {
                    //Add any remainder to the rejects
                    rejects.mergeInt(toAddResource, toAddAmount - added, Integer::sum);
                }
            }
        }
    }
}