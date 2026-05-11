package com.bervan.toolsapp.views;

import com.bervan.budget.AbstractBudgetView;
import com.bervan.budget.BudgetGridService;
import com.bervan.budget.entry.BudgetEntryService;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.investtrack.service.BudgetChartDataService;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Deprecated
@Route(value = AbstractBudgetView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
public class BudgetView extends AbstractBudgetView {

    public BudgetView(BudgetGridService service, BervanViewConfig bervanViewConfig,
                      BudgetEntryService budgetEntryService,
                      BudgetChartDataService chartDataService) {
        super(service, bervanViewConfig, budgetEntryService, chartDataService);
    }
}
