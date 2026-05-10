package com.bervan.toolsapp.views.canvasapp;

import com.bervan.canvas.CanvasService;
import com.bervan.canvas.view.AbstractCanvasPagesView;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Deprecated
@Route(value = AbstractCanvasPagesView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
public class CanvasPagesView extends AbstractCanvasPagesView {

    public CanvasPagesView(CanvasService service, BervanViewConfig bervanViewConfig) {
        super(service, bervanViewConfig);
    }
}
