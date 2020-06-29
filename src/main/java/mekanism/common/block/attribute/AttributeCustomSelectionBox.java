package mekanism.common.block.attribute;

/** If we should draw a custom selection box for the block. */
public class AttributeCustomSelectionBox implements Attribute {

    private final boolean isJavaModel;

    public AttributeCustomSelectionBox(boolean isJavaModel) {
        this.isJavaModel = isJavaModel;
    }

    public boolean isJavaModel() {
        return isJavaModel;
    }
}