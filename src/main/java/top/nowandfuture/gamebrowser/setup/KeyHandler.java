package top.nowandfuture.gamebrowser.setup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;
import top.nowandfuture.gamebrowser.ScreenManager;

@OnlyIn(Dist.CLIENT)
public class KeyHandler {
    private static final KeyBinding summonKey = new KeyBinding("key.gamebrowser.summon", GLFW.GLFW_KEY_I, "mod.gamebrowser.id");
    private static final KeyBinding screenDeleteKey = new KeyBinding("key.gamebrowser.remove", GLFW.GLFW_KEY_O, "mod.gamebrowser.id");
    private static final KeyBinding screenPlaceKey = new KeyBinding("key.gamebrowser.place", GLFW.GLFW_KEY_P, "mod.gamebrowser.id");
    private static final KeyBinding fsRotateKey = new KeyBinding("key.gamebrowser.rotate", GLFW.GLFW_KEY_L, "mod.gamebrowser.id");


    public KeyHandler(){
        ClientRegistry.registerKeyBinding(summonKey);
        ClientRegistry.registerKeyBinding(screenDeleteKey);
        ClientRegistry.registerKeyBinding(screenPlaceKey);
        ClientRegistry.registerKeyBinding(fsRotateKey);
    }

    public void onClientTick(TickEvent.ClientTickEvent tickEvent) {
        ClientWorld world = Minecraft.getInstance().world;
        Entity entity = Minecraft.getInstance().getRenderViewEntity();
        boolean isWorldAndEntityValid = world != null && entity != null && !entity.isSpectator();
        if (summonKey.isPressed()) {
            if (isWorldAndEntityValid) {
                ScreenManager.getInstance().createDefault(1, 1, world, entity, 1);
            }
        }

        if (screenPlaceKey.isPressed()) {
            if (isWorldAndEntityValid) {
                ScreenManager.getInstance().placeFollowScreen(world, entity);
            }
        }else if (screenDeleteKey.isPressed()) {
            ScreenManager.getInstance().removeFollowScreen(true);
        }else if (fsRotateKey.isKeyDown()){
            boolean shift = Screen.hasShiftDown();
            ScreenManager.getInstance().rotateFollowingScreen(new Quaternion(0, shift ? -5:5, 0, true));
        }
    }
}
