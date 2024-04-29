package io.ballerina.stdlib.mi.plugin.model;

import io.ballerina.stdlib.mi.plugin.Utils;

import java.io.File;
import java.util.ArrayList;

public class Component extends ModelElement {

    public static final String TYPE_NAME = "component";
    private String name;
    private String description = "just a description";
    private ArrayList<Param> params = new ArrayList<>();

    public Component(String name) {
        this.name = name;
    }

    public Component(String name, ArrayList<Param> params) {
        this.name = name;
        this.params = params;
    }

    public ArrayList<Param> getParams() {
        return params;
    }

    public void setParam(Param param) {
        this.params.add(param);
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

    public String getType() {
        return "component";
    }

    public void generateInstanceXml(File connectorFolder) {
        File file = new File(connectorFolder, this.getName());
        if (!file.exists()) {
            file.mkdir();
        }
        Utils.generateXml(TYPE_NAME, file + File.separator + "component", this);
    }

    public void generateTemplateXml(File connectorFolder) {
        File file = new File(connectorFolder, this.getName());
        if (!file.exists()) {
            file.mkdir();
        }
        Utils.generateXml(TYPE_NAME + "_template", file + File.separator + this.getName() + "_template", this);
    }
}
