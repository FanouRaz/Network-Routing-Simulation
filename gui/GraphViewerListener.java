package gui;

import org.graphstream.ui.view.ViewerListener;

public class GraphViewerListener implements ViewerListener {
    private Fenetre fenetre;

    public GraphViewerListener(Fenetre fenetre){
        this.fenetre = fenetre;
    }

    @Override
    public void buttonPushed(String id) {
        System.out.println(id+" clicked!");
        fenetre.selectServer(id);
    }

    @Override
    public void buttonReleased(String arg0) {}

    @Override
    public void mouseLeft(String arg0) {}

    @Override
    public void mouseOver(String arg0) {}

    @Override
    public void viewClosed(String arg0) {}
}
