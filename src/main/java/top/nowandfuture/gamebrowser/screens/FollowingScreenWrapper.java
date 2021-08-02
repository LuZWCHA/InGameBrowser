package top.nowandfuture.gamebrowser.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Quaternion;
import top.nowandfuture.gamebrowser.utils.Tools;
import top.nowandfuture.mygui.MyScreen;

public class FollowingScreenWrapper {
    private MyScreen screen;
    private Quaternion quaternion;

    private boolean doAnimation;
    private Quaternion lastQuaternion;
    private float t;
    private float animationSpeed;

    public FollowingScreenWrapper(){
        this(null);
    }

    public FollowingScreenWrapper(MyScreen screen) {
        this(screen, Quaternion.ONE.copy());
    }

    public FollowingScreenWrapper(MyScreen screen, Quaternion quaternion) {
        this.screen = screen;
        this.quaternion = quaternion;
        this.lastQuaternion = this.quaternion.copy();
        this.doAnimation = true;
        this.animationSpeed = 0.1f;
    }

    public void wrap(MyScreen screen){
        if(this.screen != screen){
            screen.forceLoseFocus();
            this.screen = screen;
            if(t > 0 && t < 1){
                t = 0;
            }
        }
    }

    public void tick(){
        if(doAnimation) {
            t -= animationSpeed;
        }else{
            t = 0;
        }

        if (t <= 0) {
            t = 0;
            if(!this.lastQuaternion.equals(this.quaternion))
                this.lastQuaternion = this.quaternion.copy();
        }

    }

    public Quaternion getCurQuaternion(){
        Quaternion middle = quaternion.copy();
        if(t > 0 && t <= 1) {
            Tools.slerp(lastQuaternion, quaternion, middle, 1 - t);
        }

        return middle;
    }

    public void render(MatrixStack stack, float pk){
        if(screen != null) {
//            GUIRenderer.getInstance().setRenderer(new IRenderer.DefaultRenderer());
            screen.render(stack, -1, -1, pk);

            // Finish the buffer render
            // Care: This is not finish at the end of world render event, so we have to complete the drawing.
            Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().finish();
//            GUIRenderer.getInstance().setRenderer(new InWorldRenderer());
        }
    }

    public void rotateTo(Quaternion quaternion){
        this.quaternion = quaternion;
        this.lastQuaternion = Quaternion.ONE.copy();
        if(this.t <= 0)
            this.t = 1;
    }

    public void rotate(Quaternion quaternion){
        this.lastQuaternion = this.quaternion.copy();
        this.quaternion.multiply(quaternion);

        if(this.t <= 0)
            this.t = 1;
    }

    public void setAnimationSpeed(float animationSpeed) {
        this.animationSpeed = animationSpeed;
    }

    public void setDoAnimation(boolean doAnimation) {
        this.doAnimation = doAnimation;
    }

    public void setCurAnmTime(float t) {
        this.t = t;
    }
}
