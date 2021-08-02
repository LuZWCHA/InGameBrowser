package top.nowandfuture.mygui;

public class Dialog {
    private TopView frameLayout;
    private ViewGroup content;
    private boolean inCenter;

    public Dialog() {
        inCenter = false;
    }

    Dialog(ViewGroup content) {
        inCenter = false;
        this.frameLayout = new TopView(content.getRoot());
        frameLayout.wrapContentWidth = true;
        frameLayout.wrapContentHeight = true;
        this.content = content;
        frameLayout.addChild(content);
    }

    void setSize(int width, int height) {
        frameLayout.setX(0);
        frameLayout.setY(0);
        frameLayout.setWidth(width);
        frameLayout.setHeight(height);
    }

    public void setPos(int x, int y) {
        frameLayout.setX(x);
        frameLayout.setY(y);
    }

    public Dialog setCenter() {
        RootView rootView = frameLayout.getRoot();
        int x = rootView.getX(), y = rootView.getY();
        int w = rootView.getWidth(), h = rootView.getHeight();
        x += (w - frameLayout.getWidth()) / 2;
        y += (h - frameLayout.getHeight()) / 2;
        frameLayout.setX(x);
        frameLayout.setY(y);

        inCenter = true;
        return this;
    }

    public boolean isInCenter() {
        return inCenter;
    }

    public void hide() {
        content.setVisible(false);
    }

    public void show() {
        frameLayout.load();
        frameLayout.setVisible(true);
        frameLayout.layout(frameLayout.getWidth(), frameLayout.getHeight());
    }

    public void dispose() {
        inCenter = false;
        content.destroy();
        content = null;
    }

    protected void layout(int sw, int sh){

        if(isShowing()) {

            if(isInCenter()){
                setCenter();
            }

            frameLayout.layout(sw, sh);
        }
    }

    public boolean isShowing() {
        return content != null && content.isVisible();
    }

    //for rootView
    ViewGroup getView() {
        return frameLayout;
    }
}
