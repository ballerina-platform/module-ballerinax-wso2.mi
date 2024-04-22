package io.ballerina.stdlib.mi.plugin.model;

public class Param {

    private String name = "dummyParam";

    private String description = "just a param";

    private String index = "0";

    public Param(String index, String name) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
