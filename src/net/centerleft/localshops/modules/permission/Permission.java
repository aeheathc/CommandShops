package net.centerleft.localshops.modules.permission;

import java.util.logging.Logger;

public interface Permission {
    
    public static final Logger log = Logger.getLogger("Minecraft");

    public String getName();
    public boolean isEnabled();
    public boolean hasPermission(String playerName, String permission);

}
