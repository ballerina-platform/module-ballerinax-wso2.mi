package io.ballerina.stdlib.mi.plugin.model;

import io.ballerina.stdlib.mi.plugin.Utils;

import java.io.File;
import java.util.ArrayList;

public class Connector extends ModelElement {

    public static final String TYPE_NAME = "connector";
    public static final String TEMP_FOLDER_PATH = "connector";
    public static final String ICON_FOLDER = "icon";
    public static final String SMALL_ICON_NAME = "icon-small.png";
    public static final String LARGE_ICON_NAME = "icon-large.png";
    public static final String JAR_FOLDER_PATH = "lib";
    private static final Connector connector = new Connector();
    private final ArrayList<Component> components = new ArrayList<>();
    private String name;
    private String description = "helps to connect with external systems";
    private String iconPath = "icon/icon-small.gif";
    private String version = "1.0.0-SNAPSHOT";

    private Connector() {
    }

    public static Connector getConnector() {
        if (connector == null) {
            return new Connector();
        }
        return connector;
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

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public ArrayList<Component> getComponents() {
        return components;
    }

    public void setComponent(Component component) {
        this.components.add(component);
    }

    public String getType() {
        return name;
    }

    public String getZipFileName() {
        return this.name + "-" + TYPE_NAME + "-" + this.version + ".zip";
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void generateInstanceXml(File folder) {
        Utils.generateXml(TYPE_NAME, folder + File.separator + TYPE_NAME, this);
    }
}
