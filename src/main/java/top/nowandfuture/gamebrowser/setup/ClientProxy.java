package top.nowandfuture.gamebrowser.setup;

import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.montoyo.mcef.api.API;
import net.montoyo.mcef.api.MCEFApi;
import org.checkerframework.checker.units.qual.K;
import top.nowandfuture.gamebrowser.ClientChunkManager;
import top.nowandfuture.gamebrowser.ScreenEntity;
import top.nowandfuture.gamebrowser.ScreenEntitySaveHelper;
import top.nowandfuture.gamebrowser.ScreenManager;
import top.nowandfuture.gamebrowser.utils.Tools;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class ClientProxy extends CommonProxy{
    private KeyHandler keyHandler;

    public ClientProxy(){
    }

    @Override
    public void setup(FMLCommonSetupEvent event) {
        keyHandler = new KeyHandler();
        MinecraftForge.EVENT_BUS.register(this);
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

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent tickEvent){
        if(tickEvent.phase == TickEvent.Phase.START) {
            keyHandler.onClientTick(tickEvent);

            ScreenManager.getInstance().updateMouseMoved();
            ScreenManager.getInstance().tick();
        }
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload chunkEvent){
        ClientChunkManager.INSTANCE.onChunkUnload(chunkEvent);
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load chunkEvent) {
        ClientChunkManager.INSTANCE.onChunkLoad(chunkEvent);
    }

    @OnlyIn(Dist.CLIENT)
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
                        try {
                            ScreenEntitySaveHelper.save((ScreenEntity) entity, (ClientWorld) world);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            ScreenManager.getInstance().removeAll();
        }
    }

    String worldId = Strings.EMPTY;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGH)
    //to process the browsers before mcef
    public void onWorldLoad(WorldEvent.Load load){
        IWorld world = load.getWorld();

        if(world.isRemote() && world instanceof ClientWorld){
            ScreenEntitySaveHelper.setSaveWorld((World) world);
        }
    }

        @Override
    public void doClientStuff(FMLClientSetupEvent event) {
        // Register the ScreenEntity.
        ScreenEntity.register();
        // Register char type listener to cover the minecraft's one.
        ScreenManager.getInstance().reRegisterCharType();

    }
}
