package top.nowandfuture.gamebrowser;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
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
import top.nowandfuture.gamebrowser.screens.FollowingScreenWrapper;
import top.nowandfuture.gamebrowser.screens.MainScreen;
import top.nowandfuture.mygui.MyScreen;
import top.nowandfuture.mygui.api.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

// TODO: 2021/7/10
@OnlyIn(Dist.CLIENT)
public class ScreenManager {
    public static float BASE_SCALE = 1 / 512f;
    private final Minecraft mc = Minecraft.getInstance();
    private final List<ScreenEntity> screenToRender;

    private final List<ScreenEntity> screenEntities;

    //Focused Screen
    private ScreenEntity focusedScreen;
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
        Optional.ofNullable(focusedScreen)
                .ifPresent(screenEntity -> focusedScreen.onMouseMoved(loc.x, loc.y));

    }

    public boolean updateMouseAction(int btn, int action, int mod) {

        return Optional.ofNullable(focusedScreen)
                .map(screenEntity -> {
                    if (GLFW.GLFW_PRESS == action)
                        return focusedScreen.onMouseClicked(loc.x, loc.y, btn);
                    else if (GLFW.GLFW_RELEASE == action) {
                        return focusedScreen.onMouseReleased(loc.x, loc.y, btn);
                    }
                    return false;
                }).orElse(false);

    }

    public boolean updateMouseScrolled(double dy) {
        return Optional.ofNullable(focusedScreen)
                .map(screenEntity -> focusedScreen.onMouseScrolled(loc.x, loc.y, dy))
                .orElse(false);
    }

    public boolean charType(char c, int keyCode) {
        return Optional.ofNullable(focusedScreen)
                .map(screenEntity -> focusedScreen.onCharTyped(c, keyCode))
                .orElse(false);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return Optional.ofNullable(focusedScreen)
                .map(screenEntity -> focusedScreen.onKeyPressed(keyCode, scanCode, modifiers))
                .orElse(false);
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return Optional.ofNullable(focusedScreen)
                .map(screenEntity -> focusedScreen.onKeyReleased(keyCode, scanCode, modifiers))
                .orElse(false);
    }

    public void tick() {
        for (ScreenEntity sc :
                screenToRender) {
            MyScreen screen = sc.getScreen();
            screen.tick();
        }

        if (followScreen != null) {
            wrapper.wrap(followScreen);
            wrapper.tick();
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
            if (screenEntity.getScreen() == screen) {
                screenEntity.remove();
                return true;
            }
            return false;
        });

    }

    public Optional<ScreenEntity> findBy(@NotNull MyScreen screen) {
        for (ScreenEntity se :
                screenEntities) {
            if (se.isAlive() && se.getScreen() == screen) {
                return Optional.of(se);
            }
        }
        return Optional.empty();
    }

    public void resizeEntity(@NotNull MyScreen screen, boolean l, boolean r, boolean t, boolean b) {
        screenEntities.stream()
                .filter(screenEntity -> screenEntity.getScreen() == screen && screenEntity.isAlive())
                .forEach(screenEntity -> screenEntity.resize(l, r, t, b));
    }

    public ScreenEntity createDefault(double width, double height, @NotNull ClientWorld world, Entity player, int offset) {
        Vector3d look = player.getLookVec();
        BlockPos blockPos = new BlockPos.Mutable(player.getPosX(), player.getPosY() + player.getEyeHeight(), player.getPosZ());
        Direction direction = Direction.getFacingFromVector(look.getX(), look.getY(), look.getZ());
        blockPos = blockPos.offset(direction, offset);
        ScreenEntity screenEntity = new ScreenEntity(world);

        screenEntity.rotationYaw = direction.getOpposite().getHorizontalAngle();
        screenEntity.setScreen(MainScreen.create(screenEntity.getUniqueID().toString(), width, height, screenEntity.getScale()));
        screenEntity.setPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        world.addEntity(screenEntity.getEntityId(), screenEntity);
        screenEntities.add(screenEntity);
        return screenEntity;
    }

    public ScreenEntity create(@Nonnull MyScreen screen, @NotNull ClientWorld world, Entity player) {
        Vector3d look = player.getLookVec();
        BlockPos blockPos = new BlockPos.Mutable(player.getPosX(), player.getPosY() + player.getEyeHeight(), player.getPosZ());
        Direction direction = Direction.getFacingFromVector(look.getX(), look.getY(), look.getZ());
        blockPos = blockPos.offset(direction, 1);
        ScreenEntity screenEntity = new ScreenEntity(world);

        screenEntity.rotationYaw = direction.getOpposite().getHorizontalAngle();
        screenEntity.setScreen(screen);
        screenEntity.setPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        world.addEntity(screenEntity.getEntityId(), screenEntity);
        screenEntities.add(screenEntity);
        return screenEntity;
    }

    private void setFollowScreen(@Nullable MyScreen followScreen) {
        this.followScreen = followScreen;
    }

    @Nullable
    public MyScreen getFollowScreen() {
        return followScreen;
    }

    public void removeFollowScreen(boolean destroy) {
        if (destroy && this.followScreen != null) {
            this.followScreen.closeScreen();
        }
        setFollowScreen((MyScreen) null);
    }

    public boolean hasFollowScreen() {
        return this.followScreen != null;
    }

    public void swapFollowScreen(@Nonnull ScreenEntity entity) {
        if (entity.isAlive()) {
            Optional.ofNullable(entity.getScreen())
                    .ifPresent(entityScreen -> {
                        MyScreen oldScreen = getFollowScreen();
                        Optional.ofNullable(oldScreen)
                                .ifPresent(screen -> {
                                    entity.setScreen(screen);
                                    entity.updateBoundBox();
                                    setFollowScreen(entityScreen);
                                });
                    });
        }
    }

    public void setFollowScreen(@Nonnull ScreenEntity entity) {
        if (!entity.isAlive()) return;
        MyScreen screen = entity.getScreen();
        Optional.ofNullable(screen)
                .ifPresent(screen1 -> {
                    entity.setEmptyScreen();
                    entity.remove();

                    if (getFollowScreen() != null) {
                        getFollowScreen().closeScreen();
                    }

                    setFollowScreen(screen1);
                });
    }

    public boolean placeFollowScreen(@Nonnull ClientWorld world, @Nonnull Entity player) {
        if (this.followScreen != null) {
            create(this.followScreen, world, player);
            removeFollowScreen(false);
            return true;
        }
        return false;
    }

    private final FollowingScreenWrapper wrapper = new FollowingScreenWrapper();

    public void renderFollowingScreen(MatrixStack stack, float pk) {
        if (followScreen != null) {
            wrapper.wrap(followScreen);

            stack.push();
            float yaw = -Minecraft.getInstance().player.getYaw(pk);
//            float pitch =  90 - Minecraft.getInstance().player.getPitch(pk);
            float scale = ScreenManager.BASE_SCALE;
//            stack.rotate(new Quaternion(pitch, 0, 0, true));
            stack.rotate(wrapper.getCurQuaternion());
            Quaternion quaternion = new Quaternion(0, yaw, 180, true);
            stack.rotate(quaternion);
            stack.translate(- followScreen.width * scale / 2, - followScreen.height * scale / 2, 1);

            stack.scale(scale, scale, scale);

            //to make a mask to do depth test for other screens
            wrapper.render(stack, pk);
            stack.pop();
        }
    }

    public void setRotation4FollowingScreen(Quaternion to) {
        if (followScreen != null) {
            wrapper.wrap(followScreen);
            wrapper.rotateTo(to);
        }
    }

    public void rotateFollowingScreen(Quaternion to) {
        if (followScreen != null) {
            wrapper.wrap(followScreen);
            wrapper.rotate(to);
        }
    }

    public ScreenEntity getFocusedScreen() {
        return focusedScreen;
    }

    public void setFocusedScreen(ScreenEntity focusedScreen) {
        if (this.focusedScreen != null && focusedScreen != this.focusedScreen) {
            this.focusedScreen.onMouseMoved(-1, -1);

            this.focusedScreen.loseFocus();
        }
        this.focusedScreen = focusedScreen;
    }

    public Vector2f getLoc() {
        return loc;
    }

    public void setLoc(Vector2f loc, float scale) {
        this.loc = new Vector2f(loc.x / BASE_SCALE * scale, loc.y / BASE_SCALE * scale);
    }

    public float getScale() {
        return BASE_SCALE;
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

            } else if (this.mc.getLoadingGui() == null && focusedScreen != null) {
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
