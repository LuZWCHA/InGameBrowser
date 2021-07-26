package top.nowandfuture.mygui.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import top.nowandfuture.mygui.Color;
import top.nowandfuture.mygui.RootView;
import top.nowandfuture.mygui.View;
import top.nowandfuture.mygui.ViewGroup;
import joptsimple.internal.Strings;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EditorView extends View {
    private MyTextFieldWidget textField;
    private Consumer<String> textChangedConsumer;
    private Consumer<EditorView> onLoseFocus;

    public EditorView(@Nonnull RootView rootView) {
        super(rootView);
    }

    public EditorView(@Nonnull ViewGroup parent) {
        super(parent);
    }

    @Override
    protected void onCreate(RootView rootView, @Nullable ViewGroup parent) {
        textField = new MyTextFieldWidget(getRoot().context.fontRenderer,
                0, 0, this.getWidth(), this.getHeight(),
                new StringTextComponent(Strings.EMPTY));
    }

    @Override
    protected void onLoad() {
        textField.setWidth(this.getWidth());
        textField.setHeight(this.getHeight());
    }

    @Override
    public void setHeightWithoutLayout(int height) {
        super.setHeightWithoutLayout(height);
        updateSize(getWidth(), getHeight(), padLeft, padTop, padRight, padBottom);
    }

    @Override
    public void setWidthWithoutLayout(int width) {
        super.setWidthWithoutLayout(width);
        updateSize(getWidth(), getHeight(), padLeft, padTop, padRight, padBottom);
    }

    @Override
    public void setPadLeft(int padLeft) {
        super.setPadLeft(padLeft);
        updateSize(getWidth(), getHeight(), padLeft, padTop, padRight, padBottom);
    }

    @Override
    public void setPadBottom(int padBottom) {
        super.setPadBottom(padBottom);
        updateSize(getWidth(), getHeight(), padLeft, padTop, padRight, padBottom);
    }

    @Override
    public void setPadRight(int padRight) {
        super.setPadRight(padRight);
        updateSize(getWidth(), getHeight(), padLeft, padTop, padRight, padBottom);
    }

    @Override
    public void setPadTop(int padTop) {
        super.setPadTop(padTop);
        updateSize(getWidth(), getHeight(), padLeft, padTop, padRight, padBottom);
    }

    private void updateSize(int w, int h, int padLeft, int padTop, int padRight, int padBottom) {
        textField.setX(padLeft);
        textField.setY(padTop);
        int contentWidth = w - padLeft - padRight;
        textField.setWidth(Math.max(0, contentWidth));
        int contentHeight = h - padTop - padBottom;
        textField.setHeight(Math.max(0, contentHeight));
    }

    @Override
    protected void onDraw(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        textField.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        textField.mouseClicked(mouseX, mouseY, mouseButton);
        return true;
    }

    @Override
    protected boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        textField.keyPressed(keyCode, scanCode, modifiers);
        return textField.canWrite();
    }

    @Override
    protected boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        textField.keyReleased(keyCode, scanCode, modifiers);
        return textField.canWrite();
    }

    @Override
    public boolean onKeyType(char typedChar, int mod) {
        boolean flag = textField.charTyped(typedChar, mod);
        if (flag) {
            onTextChanged();
        }
        return flag;
    }

    private void onTextChanged() {
        if (textChangedConsumer != null)
            textChangedConsumer.accept(getText());
    }

    public String getText() {
        return textField.getText();
    }

    public String getSelectText() {
        return textField.getSelectedText();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        textField.setVisible(visible);
    }

    @Override
    public void loseFocus() {
        super.loseFocus();
        if (textField.isFocused()) {
            textField.setFocused2(false);
            if (onLoseFocus != null)
                onLoseFocus.accept(this);
        }
    }

    @Override
    public void onUpdate() {
        if (textField.isFocused())
            textField.tick();
    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    public void setEditable(boolean value) {
        textField.setEnabled(value);
    }

    public void setValidator(@Nullable Predicate<String> predicate) {
        textField.setValidator(predicate);
    }

    public void setText(String text) {
        textField.setText(text);
    }

    public void setTextChangedConsumer(Consumer<String> textChangedConsumer) {
        this.textChangedConsumer = textChangedConsumer;
    }

    public void setTextColor(Color color) {
        this.textField.setTextColor(colorInt(color));
    }

    public void setDisabledTextColor(Color color) {
        this.textField.setDisabledTextColour(colorInt(color));
    }

    public void setMaxStringLength(int length) {
        this.textField.setMaxStringLength(length);
    }

    public void setEnabled(boolean enabled) {
        this.textField.setEnabled(enabled);
    }

    public void setOnLoseFocus(Consumer<EditorView> onLoseFocus) {
        this.onLoseFocus = onLoseFocus;
    }

    public void setSelectionColor(Color color) {
        this.textField.setSelectionColor(color);
    }

    public void setCursorColor(Color color) {
        this.textField.setCursorColor(colorInt(color));
    }

    public void setDrawDecoration(boolean v) {
        this.textField.setEnableBackgroundDrawing(v);
    }

    public void setDrawShadow(boolean drawShadow) {
        this.textField.setDrawShadow(drawShadow);
    }
}
