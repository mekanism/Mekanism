package mekanism.common.integration.crafttweaker.example.component;

import org.jetbrains.annotations.NotNull;

public class CrTExampleComment implements ICrTExampleComponent {

    private final String[] comments;

    public CrTExampleComment(String... comments) {
        if (comments == null || comments.length == 0) {
            throw new IllegalArgumentException("No comment specified.");
        }
        for (String comment : comments) {
            if (comment == null) {
                throw new RuntimeException("Comments cannot be null. To specify an empty comment use an empty String.");
            } else if (comment.endsWith(" ")) {
                throw new RuntimeException("Comments should not end with any trailing whitespace.");
            }
        }
        this.comments = comments;
    }

    @NotNull
    @Override
    public String asString() {
        if (comments.length == 1) {
            return "//" + comments[0];
        }//else multiple comments
        StringBuilder stringBuilder = new StringBuilder("/*\n");
        for (String comment : comments) {
            stringBuilder.append(" * ").append(comment).append('\n');
        }
        stringBuilder.append("*/");
        return stringBuilder.toString();
    }
}