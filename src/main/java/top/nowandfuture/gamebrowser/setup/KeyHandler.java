package top.nowandfuture.gamebrowser.setup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;
import top.nowandfuture.gamebrowser.ScreenManager;

public class KeyHandler {
    private static final KeyBinding summonKey = new KeyBinding("summon a entity", GLFW.GLFW_KEY_I, "top.nowandfuture.screen");
    private static final KeyBinding screenDeleteKey = new KeyBinding("remove", GLFW.GLFW_KEY_O, "top.nowandfuture.screen");
    private static final KeyBinding screenPlaceKey = new KeyBinding("place the following screen to world", GLFW.GLFW_KEY_P, "top.nowandfuture.screen");
    private static KeyBinding fsRotateKey = new KeyBinding("rotate screen", GLFW.GLFW_KEY_L, "top.nowandfuture.screen");

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
