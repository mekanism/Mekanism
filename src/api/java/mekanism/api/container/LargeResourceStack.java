package mekanism.api.container;

import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.NonNull;

//TODO - 26.1: Docs and reference ResourceStack
//TODO - 26.1: Should we sanitize the input amount
public record LargeResourceStack<RESOURCE extends @NonNull Resource>(RESOURCE resource, long amount) {

    public boolean isEmpty() {
        return amount <= 0 || resource.isEmpty();
    }

    @NonNull
    @Override
    public String toString() {
        return amount + "x " + resource;
    }
}