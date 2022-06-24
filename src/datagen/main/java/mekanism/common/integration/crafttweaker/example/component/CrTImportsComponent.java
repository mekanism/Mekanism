package mekanism.common.integration.crafttweaker.example.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class CrTImportsComponent implements ICrTExampleComponent {

    private final Map<String, String> paths = new HashMap<>();

    public String addImport(String path) {
        return paths.computeIfAbsent(path, p -> {
            int last = p.lastIndexOf('.');
            if (last == -1) {
                throw new IllegalArgumentException("Path being imported has no packages and may as well just be directly used.");
            } else if (p.length() <= last + 1) {
                throw new IllegalArgumentException("Path being imported ends has no class declared.");
            }
            return p.substring(last + 1);
        });
    }

    public boolean hasImports() {
        return !paths.isEmpty();
    }

    @NotNull
    @Override
    public String asString() {
        if (paths.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        //Sort imports alphabetically
        List<String> imports = new ArrayList<>(paths.keySet());
        Collections.sort(imports);
        for (String path : imports) {
            stringBuilder.append("import ").append(path).append(";\n");
        }
        stringBuilder.append('\n');
        return stringBuilder.toString();
    }
}