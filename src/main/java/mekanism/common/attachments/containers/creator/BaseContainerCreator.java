package mekanism.common.attachments.containers.creator;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.containers.IAttachedContainers;
import net.neoforged.neoforge.transfer.access.ItemAccess;

@NothingNullByDefault
public abstract class BaseContainerCreator<ATTACHED extends IAttachedContainers<?, ATTACHED>, CONTAINER> implements IContainerCreator<CONTAINER, ATTACHED> {

    private final List<IBasicContainerCreator<CONTAINER>> creators;

    public BaseContainerCreator(List<IBasicContainerCreator<CONTAINER>> creators) {
        //TODO - 1.21: Is this copy necessary? We probably want it to be immutable so yes?
        this.creators = List.copyOf(creators);
    }

    @Override
    public int totalContainers() {
        return creators.size();
    }

    @Override
    public CONTAINER create(ItemAccess attachedAccess, int containerIndex) {
        //TODO - 1.21: Figure out how to handle this and if we want to validate the index
        /*if (containerIndex < 0 || containerIndex >= creators.size()) {
            return null;
        }*/
        return creators.get(containerIndex).create(attachedAccess, containerIndex);
    }
}