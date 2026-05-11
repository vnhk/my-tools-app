package com.bervan.toolsapp.views.streamingplatformapp;

import com.bervan.streamingapp.VideoManager;
import com.bervan.streamingapp.config.ProductionData;
import com.bervan.streamingapp.config.StreamingConfigLoader;
import com.bervan.streamingapp.view.AbstractProductionListView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Map;

@Route(value = AbstractProductionListView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed({"USER", "STREAMING"})
@Deprecated
public class ProductionListView extends AbstractProductionListView {

    public ProductionListView(Map<String, ProductionData> streamingProductionData, VideoManager videoManager, StreamingConfigLoader streamingConfigLoader) {
        super(videoManager, streamingProductionData, streamingConfigLoader);
    }
}
