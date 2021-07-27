package top.nowandfuture.gamebrowser;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharModsCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import top.nowandfuture.gamebrowser.screens.MainScreen;
import top.nowandfuture.mygui.GUIRenderer;
import top.nowandfuture.mygui.MyScreen;
import top.nowandfuture.mygui.api.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static top.nowandfuture.gamebrowser.utils.RenderHelper.colorInt;

// TODO: 2021/7/10
@OnlyIn(Dist.CLIENT)
public class ScreenManager {
    private float scale = 1 / 512f;
    private final Minecraft mc = Minecraft.getInstance();
    private final List<ScreenEntity> screenToRender;

    private final List<ScreenEntity> screenEntities;

    //Focused Screen
    private ScreenEntity fsc;
    //Mouse over location at the focused screen
    private Vector2f loc;

    private MyScreen followScreen;

    private final static ScreenManager instance
            = new ScreenManager();

    private ScreenManager() {
        screenToRender = new LinkedList<>();
        screenEntities = new LinkedList<>();
    }

    public static ScreenManager getInstance() {
        return instance;
    }

    public void updateMouseMoved() {
        Optional.ofNullable(fsc)
                .ifPresent(screenEntity -> fsc.onMouseMoved(loc.x, loc.y));

    }

//    public void updateMouseDragged(int btn, float dx, float dy){
//        if(fsc != null){
//            fsc.onMouseDragged(loc.x, loc.y, btn, dx, dy);
//        }
//    }

    public boolean updateMouseAction(int btn, int action, int mod) {

        return Optional.ofNullable(fsc)
                .map(screenEntity -> {
                    if (GLFW.GLFW_PRESS == action)
                        return fsc.onMouseClicked(loc.x, loc.y, btn);
                    else if (GLFW.GLFW_RELEASE == action) {
                        return fsc.onMouseReleased(loc.x, loc.y, btn);
                    }
                    return false;
                }).orElse(false);

    }

    public boolean updateMouseScrolled(double dy) {
        return Optional.ofNullable(fsc)
                .map(screenEntity -> fsc.onMouseScrolled(loc.x, loc.y, dy))
                .orElse(false);
    }

    public boolean charType(char c, int keyCode) {
        return Optional.ofNullable(fsc)
                .map(screenEntity -> fsc.onCharTyped(c, keyCode))
                .orElse(false);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return Optional.ofNullable(fsc)
                .map(screenEntity -> fsc.onKeyPressed(keyCode, scanCode, modifiers))
                .orElse(false);
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return Optional.ofNullable(fsc)
                .map(screenEntity -> fsc.onKeyReleased(keyCode, scanCode, modifiers))
                .orElse(false);
    }

    public void tick() {
        for (ScreenEntity sc :
                screenToRender) {
            MyScreen screen = sc.getScreen();
            screen.tick();
        }
    }

    public void removeRender(ScreenEntity screenEntity) {
        screenToRender.remove(screenEntity);
    }

    public void addRender(ScreenEntity screenEntity) {
        if (screenEntity.isAlive())
            screenToRender.add(screenEntity);
    }

    public void removeDeadRender() {
        screenToRender.removeIf(screenEntity -> !screenEntity.isAlive());
    }

    public void removeBy(@NotNull MyScreen screen) {
        screenEntities.removeIf(screenEntity -> {
            if(screenEntity.getScreen() == screen) {
                screenEntity.remove();
                return true;
            }
            return false;
        });

    }

    public void resizeEntity(@NotNull MyScreen screen, boolean l, boolean r, boolean t, boolean b) {
        screenEntities.stream()
                .filter(screenEntity -> screenEntity.getScreen() == screen && screenEntity.isAlive())
                .forEach(screenEntity -> screenEntity.resize(l, r, t, b));
    }

    // TODO: 2021/7/13
    public ScreenEntity create(double width, double height, @NotNull ClientWorld world, Entity player, int offset) {
        Vector3d look = player.getLookVec();
        BlockPos blockPos = new BlockPos.Mutable(player.getPosX(), player.getPosY() + player.getEyeHeight(), player.getPosZ());
        Direction direction = Direction.getFacingFromVector(look.getX(), look.getY(), look.getZ());
        blockPos = blockPos.offset(direction, offset);
        ScreenEntity screenEntity = new ScreenEntity(world);

        screenEntity.rotationYaw = direction.getOpposite().getHorizontalAngle();
        screenEntity.setScreenWidth((int) width);
        screenEntity.setScreenHeight((int) height);
        screenEntity.setScreen(MainScreen.create(screenEntity.getUniqueID().toString(), width, height));
        screenEntity.setPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        world.addEntity(screenEntity.getEntityId(), screenEntity);
        screenEntities.add(screenEntity);
        return screenEntity;
    }

    public ScreenEntity create(MyScreen screen, @NotNull ClientWorld world, Entity player) {
        Vector3d look = player.getLookVec();
        BlockPos blockPos = new BlockPos.Mutable(player.getPosX(), player.getPosY(), player.getPosZ());
        Direction direction = Direction.getFacingFromVector(look.getX(), look.getY(), look.getZ());
        blockPos = blockPos.offset(direction);
        ScreenEntity screenEntity = new ScreenEntity(world);

        screenEntity.rotationYaw = direction.getOpposite().getHorizontalAngle();
        screenEntity.setScreen(screen);
        screenEntity.setPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        world.addEntity(screenEntity.getEntityId(), screenEntity);
        screenEntities.add(screenEntity);
        return screenEntity;
    }

    public ScreenEntity getFsc() {
        return fsc;
    }

    public void setFsc(ScreenEntity fsc) {
        if (this.fsc != null && fsc != this.fsc) {
            this.fsc.onMouseMoved(-1, -1);

            this.fsc.loseFocus();
        }
        this.fsc = fsc;
    }

    public Vector2f getLoc() {
        return loc;
    }

    public void setLoc(Vector2f loc) {
        this.loc = new Vector2f(loc.x / scale, loc.y / scale);
    }

    public float getScale() {
        return scale;
    }

    public void reRegisterCharType() {
        GLFW.glfwSetCharModsCallback(mc.getMainWindow().getHandle(), new GLFWCharModsCallback() {
            @Override
            public void invoke(long window, int codepoint, int mods) {
                onCharEvent(window, codepoint, mods);
            }
        });

        GLFW.glfwSetKeyCallback(mc.getMainWindow().getHandle(), new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                boolean consumed = false;
                if (window == mc.getMainWindow().getHandle() && mc.currentScreen == null) {
                    if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
                        consumed = keyPressed(key, scancode, mods);
                    } else if (action == GLFW.GLFW_RELEASE) {
                        consumed = keyReleased(key, scancode, mods);
                    }
                }

                if (!consumed) {
                    mc.keyboardListener.onKeyEvent(window, key, scancode, action, mods);
                }
            }
        });
    }

    //copy from minecraft
    private void onCharEvent(long windowPointer, int codePoint, int modifiers) {
        if (windowPointer == this.mc.getMainWindow().getHandle()) {
            IGuiEventListener iguieventlistener = this.mc.currentScreen;
            if (iguieventlistener != null && this.mc.getLoadingGui() == null) {
                if (Character.charCount(codePoint) == 1) {
                    net.minecraft.client.gui.screen.Screen.wrapScreenError(() -> {
                        if (net.minecraftforge.client.ForgeHooksClient.onGuiCharTypedPre(this.mc.currentScreen, (char) codePoint, modifiers))
                            return;
                        if (iguieventlistener.charTyped((char) codePoint, modifiers)) return;
                        net.minecraftforge.client.ForgeHooksClient.onGuiCharTypedPost(this.mc.currentScreen, (char) codePoint, modifiers);
                    }, "charTyped event handler", iguieventlistener.getClass().getCanonicalName());
                } else {
                    for (char c0 : Character.toChars(codePoint)) {
                        Screen.wrapScreenError(() -> {
                            if (net.minecraftforge.client.ForgeHooksClient.onGuiCharTypedPre(this.mc.currentScreen, c0, modifiers))
                                return;
                            if (iguieventlistener.charTyped(c0, modifiers)) return;
                            net.minecraftforge.client.ForgeHooksClient.onGuiCharTypedPost(this.mc.currentScreen, c0, modifiers);
                        }, "charTyped event handler", iguieventlistener.getClass().getCanonicalName());
                    }
                }

            } else if (this.mc.getLoadingGui() == null && fsc != null) {
                if (Character.charCount(codePoint) == 1) {
                    this.charType((char) codePoint, modifiers);
                } else {
                    for (char c0 : Character.toChars(codePoint)) {
                        this.charType(c0, modifiers);
                    }
                }
            }
        }
    }
}
