package mekanism.common.attachments.containers.creator;

import mekanism.common.attachments.containers.IAttachedContainers;

public interface IContainerCreator<CONTAINER, ATTACHED extends IAttachedContainers<?, ATTACHED>> extends IBasicContainerCreator<CONTAINER> {

    int totalContainers();

    ATTACHED initStorage();
}