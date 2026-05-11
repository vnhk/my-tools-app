package com.bervan.toolsapp.views.streamingplatformapp;

import com.bervan.streamingapp.StreamingAdminService;
import com.bervan.streamingapp.config.ProductionData;
import com.bervan.streamingapp.view.AbstractStreamingAdminView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Map;

@Route(value = AbstractStreamingAdminView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed({"USER"})
@Deprecated
public class StreamingAdminView extends AbstractStreamingAdminView {

    public StreamingAdminView(StreamingAdminService adminService,
                               Map<String, ProductionData> streamingProductionData) {
        super(adminService, streamingProductionData);
    }
}
