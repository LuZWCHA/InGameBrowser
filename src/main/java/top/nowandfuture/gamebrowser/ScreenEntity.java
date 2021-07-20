package top.nowandfuture.gamebrowser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import top.nowandfuture.mygui.MyScreen;

import javax.annotation.Nonnull;

public class ScreenEntity extends ClientEntity {
    public final static EntityType SCREEN_ENTITY_TYPE =
            EntityType.Builder.create(EntityClassification.MISC)
                    .size(1, 1)
                    .build("screen");

    private MyScreen screen;
    private Vector3d anchor;
    private int screenWidth, screenHeight;

    @Override
    public void setBoundingBox(AxisAlignedBB bb) {
        if (world.isRemote && screen != null) {
            final  double scale = ScreenManager.getInstance().getScale();

            screen.resize(Minecraft.getInstance(),
                    ((int) ((bb.maxX - bb.minX) / scale)),
                    ((int) ((bb.maxY - bb.minY) / scale)));
        }

        super.setBoundingBox(bb);
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

    public void updateBoundBox(){
        updateBoundBox(getPosX(), getPosY(), getPosZ());
    }

    private void updateBoundBox(double x, double y, double z){
        Vector3d start = new Vector3d(x - screenWidth + 1, y, z);
        Vector3d end = new Vector3d(x + 1, y + screenHeight, z);

        Vector3d offset = start.add(screenWidth -.5, 0, .5);

        start = start.subtract(offset);
        end = end.subtract(offset);

        start = start.rotateYaw((float) ((180 - rotationYaw) / 180 * Math.PI));
        end = end.rotateYaw((float) ((180 - rotationYaw) / 180  * Math.PI));

        start = start.add(offset);
        end = end.add(offset);

        this.anchor = start;
        this.setBoundingBox(new AxisAlignedBB(start, end));

        if(screen != null){
            final  double scale = ScreenManager.getInstance().getScale();
            screen.resize(Minecraft.getInstance(), (int)(screenWidth / scale), (int)(screenHeight / scale));
        }
    }

    public Vector3d getAnchor() {
        return anchor;
    }


    private int maxWidth = 16;
    private int maxHeight = 16;

    public void resize(boolean l, boolean r, boolean t, boolean b){
        if(l){
            screenWidth += screenWidth > 1 ? -1 : 0;
            setPosition(getPosX(), getPosY(), getPosZ());
        }
        if(r){
            screenWidth += screenWidth < maxWidth ? 1 : 0;
            setPosition(getPosX() , getPosY(), getPosZ());
        }
        if(t){
            screenHeight += screenHeight < maxHeight ? 1 : 0;
            setPosition(getPosX() , getPosY(), getPosZ());
        }
        if(b){
            screenHeight += screenHeight > 1 ? -1 : 0;
            setPosition(getPosX() , getPosY(), getPosZ());
        }
    }

    public void onMouseMoved(double x, double y){
        if(screen != null){
            screen.mouseMoved(x, y);
        }
    }

    public boolean onMouseClicked(double x, double y, int btn){
        if(screen != null){
            return screen.mouseClicked(x, y, btn);
        }
        return false;
    }

    public boolean onMouseReleased(double x, double y, int btn){
        if(screen != null){
            return screen.mouseReleased(x, y, btn);
        }
        return false;
    }

    public boolean onMouseScrolled(double x, double y, double dy){
        if(screen != null){
            return screen.mouseScrolled(x, y, dy);
        }
        return false;
    }

    public boolean onCharTyped(char c, int m){
        if(screen != null){
            return screen.charTyped(c, m);
        }
        return false;
    }

    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers){
        if(screen != null){
            return screen.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    public boolean onKeyReleased(int keyCode, int scanCode, int modifiers){
        if(screen != null){
            return screen.keyReleased(keyCode, scanCode, modifiers);
        }
        return false;
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
    }

    public void loseFocus(){
        if(screen != null){
            screen.getRootView().forceLoseFocus();
        }
    }

    public MyScreen getScreen(){
        return screen;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(int screenHeight) {
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
        this.screenWidth = compound.getInt("screenHeight");
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putInt("screenWidth", this.screenWidth);
        compound.putInt("screenHeight", this.screenHeight);
    }

    @Override
    public String toString() {
        return "ScreenEntity{" +
                "screen=" + screen +
                ", screenWidth=" + screenWidth +
                ", screenHeight=" + screenHeight +
                '}';
    }

    public void onMouseDragged(float x, float y, int btn, float dx, float dy) {

    }
}
