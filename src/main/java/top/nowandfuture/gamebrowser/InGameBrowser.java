package top.nowandfuture.gamebrowser;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import top.nowandfuture.gamebrowser.setup.ClientProxy;
import top.nowandfuture.gamebrowser.setup.CommonProxy;
import top.nowandfuture.gamebrowser.setup.IProxy;
import top.nowandfuture.gamebrowser.utils.RenderHelper;
import top.nowandfuture.gamebrowser.utils.Tools;

import static top.nowandfuture.gamebrowser.InGameBrowser.ID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(value = ID)
public class InGameBrowser
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    public static IProxy proxy;

    public final static String ID = "gamebrowser";

    private static KeyBinding keyBinding = new KeyBinding("summon a entity", GLFW.GLFW_KEY_I, "top.nowandfuture.screen");

    public InGameBrowser() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent tickEvent){
        if(tickEvent.phase == TickEvent.Phase.START) {
            ClientWorld world = Minecraft.getInstance().world;
            Entity entity = Minecraft.getInstance().getRenderViewEntity();
            if (keyBinding.isPressed()) {
                if (world != null && entity != null && !entity.isSpectator()) {
                    ScreenManager.getInstance().create(1, 1, world, entity, 1);
                }
            }

            ScreenManager.getInstance().updateMouseMoved();
            ScreenManager.getInstance().tick();
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent worldLastEvent){
        Entity entity = Minecraft.getInstance().getRenderViewEntity();
        if(entity != null) {
            final float pt = worldLastEvent.getPartialTicks();
            Vector4f vector4f = RenderHelper.getEntityRenderPos(entity, worldLastEvent.getPartialTicks());

            double x = vector4f.getX();
            double y = vector4f.getY();
            double z = vector4f.getZ();
            MatrixStack stack = worldLastEvent.getMatrixStack();
            IRenderTypeBuffer buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());

            ScreenManager.getInstance().render(stack, buffer, x, y, z, pt);

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

                    ScreenManager.getInstance().setFsc((ScreenEntity) insEnt);
                    ScreenManager.getInstance().setLoc(new Vector2f((float) localX, (float) localY));

                    return;
                }

            }

            ScreenManager.getInstance().setFsc((ScreenEntity) null);

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
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
