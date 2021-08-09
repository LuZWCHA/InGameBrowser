package top.nowandfuture.gamebrowser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
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
import top.nowandfuture.gamebrowser.setup.*;
import top.nowandfuture.gamebrowser.utils.Tools;

import java.util.List;
import java.util.function.Consumer;

import static top.nowandfuture.gamebrowser.InGameBrowser.ID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(value = ID)
public class InGameBrowser
{
    private static String HOME_PAGE;
    // Directly reference a log4j logger.
    public static IProxy proxy;

    public final static String ID = "gamebrowser";
    private final KeyHandler keyHandler;

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
        keyHandler = new KeyHandler();
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

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent tickEvent){
        if(tickEvent.phase == TickEvent.Phase.START) {
            keyHandler.onClientTick(tickEvent);

            ScreenManager.getInstance().updateMouseMoved();
            ScreenManager.getInstance().tick();
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent worldLastEvent){
        Entity entity = Minecraft.getInstance().getRenderViewEntity();
        if(entity != null) {
            final float pt = worldLastEvent.getPartialTicks();

            if(!Minecraft.getInstance().gameRenderer.getActiveRenderInfo().isThirdPerson())
                ScreenManager.getInstance().renderFollowingScreen(worldLastEvent.getMatrixStack(), pt);

            RayTraceResult rayTraceResult = Minecraft.getInstance().objectMouseOver;

            if(rayTraceResult instanceof EntityRayTraceResult) {
                Entity insEnt = ((EntityRayTraceResult) rayTraceResult).getEntity();
                if (insEnt instanceof ScreenEntity) {

                    Vector3d v0 = ((ScreenEntity) insEnt).getAnchor();
                    Vector3d n = insEnt.getLookVec();
                    Vector3d p0 = entity.getEyePosition(pt);
                    Vector3d u = entity.getLook(pt);
                    Vector3d end = Tools.getAbsLocation(n, v0, p0, u);
                    Vector3d relLocation = end.subtract(v0);

                    Vector3d planeYAxis = new Vector3d(0, 1, 0);
                    Vector3d planeXAxis = planeYAxis.crossProduct(n);

                    //bottom-top: 0 ~ screenHeight
                    double localY = planeYAxis.dotProduct(relLocation);
                    //left-right: -screenWidth ~ 0
                    double localX = planeXAxis.dotProduct(relLocation);

                    localX = ((ScreenEntity) insEnt).getScreenWidth() + localX;
                    localY = ((ScreenEntity) insEnt).getScreenHeight() - localY;

                    ScreenManager.getInstance().setFocusedScreen((ScreenEntity) insEnt);
                    ScreenManager.getInstance().setLoc(new Vector2f((float) localX, (float) localY), ((ScreenEntity) insEnt).getScale());

                    return;
                }

            }

            ScreenManager.getInstance().setFocusedScreen((ScreenEntity) null);

        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    //to process the browsers before mcef
    public void onWorldUnload(WorldEvent.Unload unload){
        IWorld world = unload.getWorld();
        if(world.isRemote() && world instanceof ClientWorld){
            ((ClientWorld) world).getAllEntities().forEach(new Consumer<Entity>() {
                @Override
                public void accept(Entity entity) {
                    if(entity instanceof ScreenEntity){
                        //save the entity
                        ScreenEntitySaveHelper.save((ScreenEntity) entity);
                        // TODO: 2021/8/9 save the nbt to files

                    }
                }
            });
            ScreenManager.getInstance().removeAll();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    //to process the browsers before mcef
    public void onWorldLoad(WorldEvent.Load load){
        IWorld world = load.getWorld();
        if(world.isRemote() && world instanceof ClientWorld){
            // TODO: 2021/8/9 read the nbt files and create the entities
            List<ScreenEntity> screenEntityList = ScreenEntitySaveHelper.load();
            screenEntityList.forEach(new Consumer<ScreenEntity>() {
                @Override
                public void accept(ScreenEntity entity) {
                    ((ClientWorld) world).addEntity(entity.getEntityId(), entity);
                }
            });
        }
    }

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
    public void onChunkUnload(ChunkEvent.Unload chunkEvent){

    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
