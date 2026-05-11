package com.bervan.toolsapp.views.streamingplatformapp;

import com.bervan.streamingapp.StreamingAdminService;
import com.bervan.streamingapp.VideoManager;
import com.bervan.streamingapp.config.ProductionData;
import com.bervan.streamingapp.view.AbstractProductionDetailsView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Map;

@Route(value = AbstractProductionDetailsView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed({"USER", "STREAMING"})
@Deprecated
public class ProductionDetailsView extends AbstractProductionDetailsView {

    public ProductionDetailsView(VideoManager videoManager, Map<String, ProductionData> streamingProductionData,
                                  StreamingAdminService adminService) {
        super(videoManager, streamingProductionData, adminService);
    }
}
