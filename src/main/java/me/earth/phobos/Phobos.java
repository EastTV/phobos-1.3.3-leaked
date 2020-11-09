package me.earth.phobos;

import me.earth.phobos.manager.ColorManager;
import me.earth.phobos.manager.CommandManager;
import me.earth.phobos.manager.ConfigManager;
import me.earth.phobos.manager.EventManager;
import me.earth.phobos.manager.FileManager;
import me.earth.phobos.manager.FriendManager;
import me.earth.phobos.manager.HoleManager;
import me.earth.phobos.manager.InventoryManager;
import me.earth.phobos.manager.ModuleManager;
import me.earth.phobos.manager.NotificationManager;
import me.earth.phobos.manager.PacketManager;
import me.earth.phobos.manager.PositionManager;
import me.earth.phobos.manager.PotionManager;
import me.earth.phobos.manager.ReloadManager;
import me.earth.phobos.manager.RotationManager;
import me.earth.phobos.manager.ServerManager;
import me.earth.phobos.manager.SpeedManager;
import me.earth.phobos.manager.TextManager;
import me.earth.phobos.manager.TimerManager;
import me.earth.phobos.manager.TotemPopManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

@Mod(modid = Phobos.MODID, name = Phobos.MODNAME, version = Phobos.MODVER)
public class Phobos {

    public static final String MODID = "earthhack";
    public static final String MODNAME = "3arthh4ck";
    public static final String MODVER = "1.3.3";
    public static final String NAME_UNICODE = "3\u1D00\u0280\u1D1B\u029C\u029C4\u1D04\u1D0B"; //"\u1D18\u029C\u1D0F\u0299\u1D0F\uA731.\u1D07\u1D1C";
	public static final String PHOBOS_UNICODE = "\u1D18\u029C\u1D0F\u0299\u1D0F\uA731";
    public static final String CHAT_SUFFIX = " \u23D0 " + NAME_UNICODE;
	public static final String PHOBOS_SUFFIX = " \u23D0 " + PHOBOS_UNICODE;
	public static final Logger LOGGER = LogManager.getLogger("3arthh4ck");

	public static ModuleManager moduleManager;
	public static SpeedManager speedManager;
	public static PositionManager positionManager;
	public static RotationManager rotationManager;
	public static CommandManager commandManager;
	public static EventManager eventManager;
	public static ConfigManager configManager;
	public static FileManager fileManager;
	public static FriendManager friendManager;
	public static TextManager textManager;
	public static ColorManager colorManager;
	public static ServerManager serverManager;
	public static PotionManager potionManager;
	public static InventoryManager inventoryManager;
	public static TimerManager timerManager;
	public static PacketManager packetManager;
	public static ReloadManager reloadManager;
	public static TotemPopManager totemPopManager;
	public static HoleManager holeManager;
	public static NotificationManager notificationManager;
	private static boolean unloaded = false;

    @Mod.Instance
    public static Phobos INSTANCE;
	
	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
		LOGGER.info("ohare is cute!!!");
		LOGGER.info("faggot above - 3vt");
		LOGGER.info("megyn wins again");
		LOGGER.info("gtfo my logs - 3arth");
	}

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
		Display.setTitle("3arthh4ck - v." + MODVER);
		load();
    }

    public static void load() {
		LOGGER.info("\n\nLoading 3arthh4ck " + MODVER);
		unloaded = false;
		if(reloadManager != null) {
			reloadManager.unload();
			reloadManager = null;
		}

		totemPopManager = new TotemPopManager();
		timerManager = new TimerManager();
		packetManager = new PacketManager();
		serverManager = new ServerManager();
		colorManager = new ColorManager();
		textManager = new TextManager();
		moduleManager = new ModuleManager();
		speedManager = new SpeedManager();
		rotationManager = new RotationManager();
		positionManager = new PositionManager();
		commandManager = new CommandManager();
		eventManager = new EventManager();
		configManager = new ConfigManager();
		fileManager = new FileManager();
		friendManager = new FriendManager();
		potionManager = new PotionManager();
		inventoryManager  = new InventoryManager();
		holeManager = new HoleManager();
		notificationManager = new NotificationManager();
		LOGGER.info("Initialized Managers");

		moduleManager.init();
		LOGGER.info("Modules loaded.");
		configManager.init();
		eventManager.init();
		LOGGER.info("EventManager loaded.");
		textManager.init(true);
		moduleManager.onLoad();
		totemPopManager.init();
		timerManager.init();
		LOGGER.info("3arthh4ck initialized!\n");
	}

	public static void unload(boolean unload) {
		LOGGER.info("\n\nUnloading 3arthh4ck " + MODVER);
		if(unload) {
			reloadManager = new ReloadManager();
			reloadManager.init(commandManager != null ? commandManager.getPrefix() : ".");
		}
		onUnload();
		eventManager = null;
		holeManager = null;
		timerManager = null;
		moduleManager = null;
		totemPopManager = null;
		serverManager = null;
		colorManager = null;
		textManager = null;
		speedManager = null;
		rotationManager = null;
		positionManager = null;
		commandManager = null;
		configManager = null;
		fileManager = null;
		friendManager = null;
		potionManager = null;
		inventoryManager = null;
		notificationManager = null;
		LOGGER.info("3arthh4ck unloaded!\n");
	}

	public static void reload() {
		unload(false);
		load();
	}

	public static void onUnload() {
    	if(!unloaded) {
			eventManager.onUnload();
			moduleManager.onUnload();
			configManager.saveConfig(configManager.config.replaceFirst("phobos/", ""));
			moduleManager.onUnloadPost();
			timerManager.unload();
			unloaded = true;
		}
	}
}
