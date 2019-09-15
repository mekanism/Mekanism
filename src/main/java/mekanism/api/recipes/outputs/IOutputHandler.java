package mekanism.api.recipes.outputs;

//TODO: Figure out a better name for this?
public interface IOutputHandler<OUTPUT> {

    void handleOutput(OUTPUT toOutput, int operations);

    //TODO: Rename, also should we have it take a param for what we already have the max operations being?
    int operationsRoomFor(OUTPUT toOutput, int currentMax);
}