package com.bervan.toolsapp.views.shopapp;

import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.search.SearchService;
import com.bervan.shstat.service.ProductConfigService;
import com.bervan.shstat.view.AbstractProductConfigView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Deprecated
@Route(value = AbstractProductConfigView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
public class ProductConfigView extends AbstractProductConfigView {

    public ProductConfigView(ProductConfigService productConfigService, SearchService searchService, BervanViewConfig bervanViewConfig) {
        super(productConfigService, searchService, bervanViewConfig);
    }
}
