package mekanism.common.block.attribute;

/** If we should draw a custom selection box for the block. */
public class AttributeCustomSelectionBox implements Attribute {

    public static final Attribute JSON = new AttributeCustomSelectionBox(false);
    public static final Attribute JAVA = new AttributeCustomSelectionBox(true);

    private final boolean isJavaModel;

    private AttributeCustomSelectionBox(boolean isJavaModel) {
        this.isJavaModel = isJavaModel;
    }

    public boolean isJavaModel() {
        return isJavaModel;
    }
}