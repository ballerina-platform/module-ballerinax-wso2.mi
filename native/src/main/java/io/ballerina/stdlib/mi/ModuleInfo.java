package io.ballerina.stdlib.mi;

public class ModuleInfo {
    private static String ORG_NAME = "BALLERINA_ORG_NAME";
    private static String MODULE_NAME = "BALLERINA_MODULE_NAME";
    private static String MODULE_VERSION = "BALLERINA_MODULE_VERSION";

    public String getOrgName() {
        return ORG_NAME;
    }

    public String getModuleName() {
        return MODULE_NAME;
    }

    public String getModuleVersion() {
        return MODULE_VERSION;
    }

    public ModuleInfo() {}

    public ModuleInfo(String orgName, String moduleName, String moduleVersion) {
        ORG_NAME = orgName;
        MODULE_NAME = moduleName;
        MODULE_VERSION = moduleVersion;
    }
}
