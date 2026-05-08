package mekanism.common.util;

import com.google.common.primitives.Ints;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
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
          Object2LongMap<RESOURCE> rejects, TransactionContext transaction) {
        StorageUtils.validateSizeMatches(orig, toAdd, "container");
        for (int container = 0, size = toAdd.size(); container < size; container++) {
            CONTAINER toAddContainer = toAdd.get(container);
            if (!toAddContainer.isEmpty()) {
                RESOURCE toAddResource = toAddContainer.getResource();
                long toAddAmount = toAddContainer.amountAsLong();
                CONTAINER origContainer = orig.get(container);
                //TODO - 26.1: Validate all callers have this work with the given automation type
                // Also how much do we care about merging identical slots? Should we use the InventoryUtils#insertItem helper
                // to try inserting against all the slots of the other?
                //TODO - 26.1: Is  this how we want to handle trying to insert it, or would it be better to basically loop inserting multiple times as long
                // as we are inserting max int while we get closer to toAddAmount
                int added = origContainer.insert(toAddResource, Ints.saturatedCast(toAddAmount), transaction, AutomationType.INTERNAL);
                if (added < toAddAmount) {
                    //Add any remainder to the rejects
                    rejects.mergeLong(toAddResource, toAddAmount - added, Long::sum);
                }
            }
        }
    }
}