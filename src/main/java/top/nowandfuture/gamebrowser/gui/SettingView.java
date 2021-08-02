package top.nowandfuture.gamebrowser.gui;

import org.checkerframework.common.value.qual.IntRange;
import top.nowandfuture.mygui.Color;
import top.nowandfuture.mygui.RootView;
import top.nowandfuture.mygui.ViewGroup;
import top.nowandfuture.mygui.components.SliderView;
import top.nowandfuture.mygui.layouts.LinearLayout;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

public class SettingView extends LinearLayout {

    private SliderView brSliderView;
    private SliderView scaSliderView;
    private Consumer<Float> brChangingListener;
    private Consumer<Float> scaChangedListener;

    public SettingView(@Nonnull RootView rootView) {
        super(rootView);
    }

    protected SettingView(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }

    public SettingView(@Nonnull RootView rootView, ViewGroup parent, @Nonnull LinearLayoutParameter linearLayoutParameter) {
        super(rootView, parent, linearLayoutParameter);
    }

    public SettingView(ViewGroup parent, @Nonnull LinearLayoutParameter linearLayoutParameter) {
        super(parent, linearLayoutParameter);
    }

    @Override
    public void onCreate(RootView rootView, @Nullable ViewGroup parent) {
        brSliderView = new SliderView(this);
        brSliderView.setHeight(40);
        brSliderView.setSliderHalfHeight(6);
        brSliderView.setSliderHalfWidth(4);
        brSliderView.setFlowParentWidth(true);
        brSliderView.setRange(100, 0, 0);
        brSliderView.setProgressChanging(new Consumer<Float>() {
            @Override
            public void accept(Float aFloat) {
                Optional.ofNullable(brChangingListener)
                        .ifPresent(new Consumer<Consumer<Float>>() {
                            @Override
                            public void accept(Consumer<Float> floatConsumer) {
                                floatConsumer.accept(aFloat);
                            }
                        });
            }
        });

        scaSliderView = new SliderView(this);
        scaSliderView.setHeight(40);
        scaSliderView.setSliderHalfHeight(6);
        scaSliderView.setSliderHalfWidth(4);
        scaSliderView.setFlowParentWidth(true);
        scaSliderView.setRange(100, 0, 0);
        scaSliderView.setProgressChanged(new Consumer<Float>() {
            @Override
            public void accept(Float aFloat) {
                Optional.ofNullable(scaChangedListener)
                        .ifPresent(new Consumer<Consumer<Float>>() {
                            @Override
                            public void accept(Consumer<Float> floatConsumer) {
                                floatConsumer.accept(aFloat);
                            }
                        });
            }
        });
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        setBackgroundColor(new Color(0,0,0));
        pushLayoutParameter(new LinearLayoutParameter(true, true, true));
        setFlowParentHeight(true);
        setFlowParentWidth(true);
        setPadLeft(8);
        setPadRight(8);
    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected void onReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        return false;
    }

    public void setBrightness(@IntRange(to = 15) int brightness){
        brSliderView.setProgress(brightness / 15f * 100);
    }

    public void setScale(@IntRange(to = 100) int scale){
        scaSliderView.setProgress(scale);
    }

    public void setBrChangingListener(Consumer<Float> brChangingListener) {
        this.brChangingListener = brChangingListener;
    }

    public void setScaChangedListener(Consumer<Float> scaChangedListener) {
        this.scaChangedListener = scaChangedListener;
    }
}
