package net.mathias2246.buildmc.database;

import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.util.config.YamlConfigurationManager;

public class DatabaseConfig extends YamlConfigurationManager {

    private static final String USE_SERVER_MODE = "database.useServerMode";
    private static final String SERVER_URL = "database.serverUrl";
    private static final String SERVER_PORT = "database.serverPort";
    private static final String H2_ARGS = "database.h2Args";

    private boolean serverMode;
    private String serverUrl;
    private int serverPort;
    private String h2Args;

    public DatabaseConfig() {
        super(CoreMain.plugin, "database.yml");
    }

    @Override
    public void setupConfiguration() {
        serverMode = configuration.getBoolean(USE_SERVER_MODE, false);
        serverUrl  = configuration.getString(SERVER_URL, "jdbc:h2:tcp://localhost/./database");
        serverPort = configuration.getInt(SERVER_PORT, 9092);
        h2Args     = configuration.getString(H2_ARGS, "TRACE_LEVEL_FILE=0;TRACE_LEVEL_SYSTEM_OUT=0");
    }

    @Override
    protected void preSave() {
        configuration.set(USE_SERVER_MODE, serverMode);
        configuration.set(SERVER_URL, serverUrl);
        configuration.set(SERVER_PORT, serverPort);
        configuration.set(H2_ARGS, h2Args);
    }

    public boolean isServerMode() { return serverMode; }
    public String getServerUrl()  { return serverUrl; }
    public int getServerPort()    { return serverPort; }
    public String getH2Args()     { return h2Args; }

    public void setServerMode(boolean serverMode) { this.serverMode = serverMode; }
    public void setServerUrl(String serverUrl)    { this.serverUrl = serverUrl; }
    public void setServerPort(int serverPort)     { this.serverPort = serverPort; }
    public void setH2Args(String h2Args)          { this.h2Args = h2Args; }
}