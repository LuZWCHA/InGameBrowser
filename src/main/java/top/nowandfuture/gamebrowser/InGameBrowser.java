package top.nowandfuture.gamebrowser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.nowandfuture.gamebrowser.setup.ClientProxy;
import top.nowandfuture.gamebrowser.setup.CommonProxy;
import top.nowandfuture.gamebrowser.setup.Config;
import top.nowandfuture.gamebrowser.setup.IProxy;

import static top.nowandfuture.gamebrowser.InGameBrowser.ID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(value = ID)
public class InGameBrowser
{
    private static String HOME_PAGE;
    // Directly reference a log4j logger.
    public static IProxy proxy;

    public final static String ID = "gamebrowser";

    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static String getHomePage() {
        return HOME_PAGE;
    }

    public InGameBrowser() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onLoad);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onReload);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    }

    public void onLoad(final ModConfig.Loading configEvent) {
        loadConfig();
    }

    public void onReload(final ModConfig.Reloading configEvent) {
        loadConfig();
    }

    public void loadConfig(){
        HOME_PAGE = Config.HOME_PAGE.get();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onMouseInput(InputEvent.RawMouseEvent inputEvent){
        int btn = inputEvent.getButton();
        int action = inputEvent.getAction();
        if (Minecraft.getInstance().currentScreen == null) {
            boolean consume = ScreenManager.getInstance().updateMouseAction(btn, action, inputEvent.getMods());
            if(consume){
                inputEvent.setCanceled(true);
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onMouseScrolled(InputEvent.MouseScrollEvent inputEvent){
        double d = inputEvent.getScrollDelta();
        boolean consume = ScreenManager.getInstance().updateMouseScrolled(d);
        if(consume){
            inputEvent.setCanceled(true);
        }
    }

    private void setup(final FMLCommonSetupEvent event){
        proxy.setup(event);
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        proxy.doClientStuff(event);
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
