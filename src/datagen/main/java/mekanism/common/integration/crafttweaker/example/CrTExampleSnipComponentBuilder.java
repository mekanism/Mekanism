package mekanism.common.integration.crafttweaker.example;

import mekanism.common.integration.crafttweaker.example.component.ICrTExampleComponent;
import org.jetbrains.annotations.NotNull;

public class CrTExampleSnipComponentBuilder<BUILDER_TYPE extends CrTExampleBuilder<BUILDER_TYPE>> extends CrTExampleBuilder<CrTExampleSnipComponentBuilder<BUILDER_TYPE>>
      implements ICrTExampleComponent {

    private final BUILDER_TYPE parent;
    private final String snipData;

    CrTExampleSnipComponentBuilder(BaseCrTExampleProvider exampleProvider, BUILDER_TYPE parent, String snipType, String snipData) {
        super(exampleProvider, snipType);
        this.parent = parent;
        this.snipData = snipData;
    }

    public BUILDER_TYPE end() {
        if (contents.isEmpty()) {
            invalidContents();
        }
        return parent;
    }

    @NotNull
    @Override
    public String asString() {
        return "#snip " + name + ' ' + snipData + '\n' + build() + "\n#snip end";
    }

    @Override
    protected void invalidContents() {
        throw new RuntimeException("Snip '" + name + " " + snipData + "' is empty and should either be implemented or removed.");
    }
}