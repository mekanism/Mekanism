package mekanism.common.attachments.containers.creator;

public interface IContainerCreator<CONTAINER, ATTACHED> extends IBasicContainerCreator<CONTAINER> {

    int totalContainers();

    ATTACHED initStorage();
}