package top.nowandfuture.gamebrowser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.nowandfuture.gamebrowser.screens.MainScreen;
import top.nowandfuture.mygui.MyScreen;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Consumer;

public class ScreenEntity extends ClientEntity {
    public final static EntityType SCREEN_ENTITY_TYPE =
            EntityType.Builder.create(EntityClassification.MISC)
                    .size(1, 1)
                    .build("screen");

    private MyScreen screen;
    private Vector3d anchor;
    private int screenWidth, screenHeight;
    private float brightness = .5f;
    private float scale = 1f;

    @Override
    public void setBoundingBox(AxisAlignedBB bb) {
        super.setBoundingBox(bb);
        resizeScreen();
    }

    @OnlyIn(Dist.CLIENT)
    public void resizeScreen() {
        AxisAlignedBB bb = getBoundingBox();
        if (world.isRemote() && screen != null) {
            int w = (int) Math.max(bb.getZSize(), bb.getXSize());
            screen.init(Minecraft.getInstance(),
                    ((int) (w / ScreenManager.BASE_SCALE * scale)),
                    ((int) (bb.getYSize() / ScreenManager.BASE_SCALE * scale)));
        }
    }

    @Override
    protected AxisAlignedBB getBoundingBox(Pose pose) {
        return getBoundingBox();
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return super.getBoundingBox();
    }

    @Override
    public void setPosition(double x, double y, double z) {
        updateBoundBox(x, y, z);
        this.setRawPosition(x, y, z);
    }

    public void updateBoundBox() {
        updateBoundBox(getPosX(), getPosY(), getPosZ());
    }

    private void updateBoundBox(double x, double y, double z) {
        Vector3d start = new Vector3d(x - screenWidth + 1, y, z);
        Vector3d end = new Vector3d(x + 1, y + screenHeight, z);

        Vector3d offset = start.add(screenWidth - .5, 0, .5);

        start = start.subtract(offset);
        end = end.subtract(offset);

        start = start.rotateYaw((float) ((180 - rotationYaw) / 180 * Math.PI));
        end = end.rotateYaw((float) ((180 - rotationYaw) / 180 * Math.PI));

        start = start.add(offset);
        end = end.add(offset);

        this.anchor = start;
        this.setBoundingBox(new AxisAlignedBB(start, end));
    }

    public Vector3d getAnchor() {
        return anchor;
    }

    private int maxWidth = 16;
    private int maxHeight = 16;

    public void resize(boolean l, boolean r, boolean t, boolean b) {
        if (l) {
            screenWidth += screenWidth > 1 ? -1 : 0;
            setPosition(getPosX(), getPosY(), getPosZ());
        }
        if (r) {
            screenWidth += screenWidth < maxWidth ? 1 : 0;
            setPosition(getPosX(), getPosY(), getPosZ());
        }
        if (t) {
            screenHeight += screenHeight < maxHeight ? 1 : 0;
            setPosition(getPosX(), getPosY(), getPosZ());
        }
        if (b) {
            screenHeight += screenHeight > 1 ? -1 : 0;
            setPosition(getPosX(), getPosY(), getPosZ());
        }
    }

    public void onMouseMoved(double x, double y) {
        Optional.ofNullable(screen)
                .ifPresent(screen -> screen.mouseMoved(x, y));
    }

    public boolean onMouseClicked(double x, double y, int btn) {
        return Optional.ofNullable(screen)
                .map(screen -> screen.mouseClicked(x, y, btn))
                .orElse(false);
    }

    public boolean onMouseReleased(double x, double y, int btn) {
        return Optional.ofNullable(screen)
                .map(screen -> screen.mouseReleased(x, y, btn))
                .orElse(false);
    }

    public boolean onMouseScrolled(double x, double y, double dy) {
        return Optional.ofNullable(screen)
                .map(screen -> screen.mouseScrolled(x, y, dy))
                .orElse(false);
    }

    public boolean onCharTyped(char c, int m) {
        return Optional.ofNullable(screen)
                .map(screen -> screen.charTyped(c, m))
                .orElse(false);
    }

    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return Optional.ofNullable(screen)
                .map(screen -> screen.keyPressed(keyCode, scanCode, modifiers))
                .orElse(false);

    }

    public boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        return Optional.ofNullable(screen)
                .map(screen -> screen.keyReleased(keyCode, scanCode, modifiers))
                .orElse(false);
    }

    public EntitySize getSize() {
        return new EntitySize(screenWidth, screenHeight, false);
    }

    @Override
    public final EntitySize getSize(Pose poseIn) {
        return getSize();
    }

    public ScreenEntity(World worldIn) {
        super(SCREEN_ENTITY_TYPE, worldIn);
    }


    public ScreenEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    public EntityType<?> getType() {
        return SCREEN_ENTITY_TYPE;
    }

    public static void register() {
        EntityRendererManager entityRendererManager = Minecraft.getInstance().getRenderManager();
        entityRendererManager.register(SCREEN_ENTITY_TYPE, new ScreenEntityRenderer(entityRendererManager));
    }

    public void setScreen(@Nonnull MyScreen screen) {
        this.screen = screen;
        updateSizeByScreen();
    }

    private boolean freeze = true;
    private CompoundNBT screenNBT;

    public boolean isFreeze() {
        return freeze;
    }

    public void freezeScreen() {
        if (!freeze) {
            Optional.ofNullable(screen)
                    .ifPresent(screen -> {
                        screenNBT = new CompoundNBT();
                        screen.writeNBT(screenNBT);
                        screen.closeScreen();
                    });
            freeze = true;
        }
    }

    // TODO: 2021/8/11 ... this method should only be used when the entities not init.
    public void setFreeze(boolean freeze) {
        this.freeze = freeze;
    }

    public void unfreezeScreen() {

        if (freeze) {

            if(isAlive() && screen == null){
                screen = createEmptyScreen();
            }

            Optional.of(screen)
                    .ifPresent(new Consumer<MyScreen>() {
                        @Override
                        public void accept(MyScreen screen) {
                            // TODO: 2021/8/3 recover the screen by url
                            screen.readNBT(screenNBT);
                            //Minecraft.getInstance(), (int) (screenWidth / ScreenManager.BASE_SCALE * scale), (int) (screenWidth / ScreenManager.BASE_SCALE * scale)
                            screen.reload();
                        }
                    });
            freeze = false;
        }
    }

    public void setEmptyScreen() {
        this.screen = null;
    }

    private void updateSizeByScreen() {
        if (screen != null) {
            this.screenWidth = (int) (screen.width * ScreenManager.BASE_SCALE / scale);
            this.screenHeight = (int) (screen.height * ScreenManager.BASE_SCALE / scale);
        }
    }

    public void loseFocus() {
        Optional.ofNullable(screen)
                .ifPresent(MyScreen::forceLoseFocus);

    }

    public MyScreen getScreen() {
        return screen;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    private void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    private void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.screenWidth = compound.getInt("screenWidth");
        this.screenHeight = compound.getInt("screenHeight");
        this.brightness = compound.getFloat("brightness");
        this.scale = compound.getFloat("screenScale");
        this.screenNBT = compound.getCompound("screenNBT");

        if (!freeze) {
            if (screen == null) {
                this.screen = createEmptyScreen();
            }

            this.screen.readNBT(this.screenNBT);
        }
    }

    private MyScreen createEmptyScreen(){
        return MainScreen.create(getUniqueID().toString(), getScreenWidth(),
                getScreenHeight(), getScale());
    }

    private MyScreen createEmptyScreen(String url){
        return MainScreen.create(url, getUniqueID().toString(), getScreenWidth(),
                getScreenHeight(), getScale());
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putInt("screenWidth", this.screenWidth);
        compound.putInt("screenHeight", this.screenHeight);
        compound.putFloat("brightness", this.brightness);
        compound.putFloat("screenScale", this.scale);

        if(this.screenNBT == null) this.screenNBT = new CompoundNBT();

        if (screen != null) {
            screen.writeNBT(this.screenNBT);
        }

        compound.put("screenNBT", this.screenNBT);
    }

    public void readScreen(CompoundNBT compound) {
        this.screenNBT = compound.getCompound("screenNBT");
        if (screen != null) {
            screen.readNBT(this.screenNBT);
        }
    }

    public void writeScreen(CompoundNBT compound) {
        if (screen != null) {
            screen.writeNBT(this.screenNBT);
        }
        compound.put("screenNBT", this.screenNBT);
    }

    @Override
    public String toString() {
        return "ScreenEntity{" +
                ", screenWidth=" + screenWidth +
                ", screenHeight=" + screenHeight +
                ", brightness=" + brightness +
                ", scale=" + scale +
                ", screenNBT=" + screenNBT.toString() +
                '}';
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        if (this.scale != scale) {
            this.scale = scale;
            Optional.ofNullable(screen)
                    .ifPresent(screen -> resizeScreen());
        }
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        ScreenManager.getInstance().add(this);
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        ScreenManager.getInstance().remove(this);
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        return super.isInRangeToRenderDist(distance);
    }
}
