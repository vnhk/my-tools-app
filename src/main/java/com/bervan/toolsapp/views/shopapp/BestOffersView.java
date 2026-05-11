package com.bervan.toolsapp.views.shopapp;

import com.bervan.asynctask.AsyncTaskService;
import com.bervan.shstat.queue.RefreshViewService;
import com.bervan.shstat.service.DiscountsViewService;
import com.bervan.shstat.service.ProductSearchService;
import com.bervan.shstat.view.AbstractBestOffersView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Deprecated
@Route(value = AbstractBestOffersView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
public class BestOffersView extends AbstractBestOffersView {

    public BestOffersView(RefreshViewService refreshViewService, AsyncTaskService asyncTaskService, DiscountsViewService discountsViewService, ProductSearchService productSearchService) {
        super(discountsViewService, refreshViewService, asyncTaskService, productSearchService);
    }
}
