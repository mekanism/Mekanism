package mekanism.common.util;

import mekanism.api.AutomationType;
import mekanism.api.container.IMekanismResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

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
}