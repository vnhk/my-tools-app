package com.bervan.toolsapp.views.logsapp;

import com.bervan.common.component.BervanComboBox;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.model.Operator;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.user.UserRepository;
import com.bervan.common.view.AbstractBervanTableView;
import com.bervan.logging.LogEntity;
import com.bervan.logging.LogService;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Deprecated
@Route(value = TrackersTableView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
public class TrackersTableView extends AbstractBervanTableView<Long, LogEntity> {
    public static final String ROUTE_NAME = "logs-app/trackers";
    private final LogService logService;
    private final UserRepository userRepository;
    private String appName = "";
    private ComboBox<String> logSelector;
    private boolean showLastPage = true;

    public TrackersTableView(LogService logService, UserRepository userRepository, BervanViewConfig bervanViewConfig) {
        super(new LogsAppPageLayout(ROUTE_NAME), logService, bervanViewConfig, LogEntity.class);
        this.logService = logService;
        this.userRepository = userRepository;

        checkboxesColumnsEnabled = false;
        pageSize = 500;
        addClassName("logs-item-view");

        Set<String> appsName = logService.loadAppsName();
        logSelector = new BervanComboBox<>(appsName);
        if (!appsName.isEmpty()) {
            appName = appsName.iterator().next();
            logSelector.setValue(appName);
        }
        logSelector.addValueChangeListener(comboBoxStringComponentValueChangeEvent -> {
            appName = comboBoxStringComponentValueChangeEvent.getValue();
            this.showLastPage = true;
            this.loadData();
            this.refreshData();
        });

        renderCommonComponents();
        newItemButton.setVisible(false);

        topLayout.add(logSelector);
        filtersLayout.removeFilterableFields("fullLog");
        filtersLayout.addFilterableFields("message");
        filtersLayout.addFilterableFields("className");
        filtersLayout.addFilterableFields("methodName");
    }

    @Override
    protected Grid<LogEntity> getGrid() {
        Grid<LogEntity> grid = new Grid<>(LogEntity.class, false);
        grid.addClassName("flex-grid");
        buildGridAutomatically(grid);
        for (Grid.Column<LogEntity> column : grid.getColumns()) {
            column.setAutoWidth(true).setFlexGrow(1)
                    .setClassNameGenerator(item -> "wrap-text-grid");
        }

        return grid;
    }

    @Override
    protected void customizePreLoad(SearchRequest request) {
        request.addCriterion("APP_NAME_EQ_CRITERION", Operator.OR_OPERATOR, LogEntity.class,
                "applicationName", SearchOperation.EQUALS_OPERATION, appName);

        request.setAddOwnerCriterion(false);

        if (showLastPage) {
            allFound = countAll(request, new ArrayList<>());
            maxPages = (int) Math.ceil((double) allFound / pageSize);
            pageNumber = maxPages - 1;
            if (pageNumber < 0) {
                pageNumber = 0;
            }
            showLastPage = false;
        }
    }

    @Override
    protected List<LogEntity> loadData() {
        List<LogEntity> logEntities = super.loadData();
        logEntities.forEach(e -> e.setFullLog(e.getFullLog()));
        return logEntities;
    }
}