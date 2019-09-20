package mekanism.api.recipes.outputs;

public interface IOutputHandler<OUTPUT> {

    //TODO: Rename to add?
    void handleOutput(OUTPUT toOutput, int operations);

    //TODO: Rename
    int operationsRoomFor(OUTPUT toOutput, int currentMax);
}