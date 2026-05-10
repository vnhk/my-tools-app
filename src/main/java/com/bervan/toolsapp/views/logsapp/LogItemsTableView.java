package com.bervan.toolsapp.views.logsapp;

import com.bervan.common.component.BervanButton;
import com.bervan.common.component.BervanButtonStyle;
import com.bervan.common.component.BervanComboBox;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.search.SearchQueryOption;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.SearchService;
import com.bervan.common.search.model.Operator;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.search.model.SearchResponse;
import com.bervan.common.user.UserRepository;
import com.bervan.common.view.AbstractBervanTableView;
import com.bervan.logging.JsonLogger;
import com.bervan.logging.LogEntity;
import com.bervan.logging.LogService;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Deprecated
@Route(value = LogItemsTableView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
public class LogItemsTableView extends AbstractBervanTableView<Long, LogEntity> {
    public static final String ROUTE_NAME = "logs-app/all-logs";
    private final LogService logService;
    private final UserRepository userRepository;
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "my-tools-app");
    private final HorizontalLayout buttonsWithDateFilters = new HorizontalLayout();
    private final SearchService searchService;
    private String appName = "";
    private ComboBox<String> logSelector;
    private boolean showLastPage = true;
    private final BervanButton defaultLastHour1Button = new BervanButton("Last 1h", click -> {
        try {
            filtersLayout.getDateTimeFiltersMap().get(LogEntity.class.getDeclaredField("timestamp"))
                    .get("FROM").setValue(LocalDateTime.now().minusHours(1));
        } catch (NoSuchFieldException e) {
            log.error("Error searching last 1h logs", e);
            throw new RuntimeException(e);
        }
        showLastPage = true;
        refreshTable.click();
    }, BervanButtonStyle.PRIMARY);
    private boolean initSearch = true;

    public LogItemsTableView(LogService logService, UserRepository userRepository, BervanViewConfig bervanViewConfig, SearchService searchService) {
        super(new LogsAppPageLayout(ROUTE_NAME), logService, bervanViewConfig, LogEntity.class);
        this.processName = "LogsItemsTable";
        this.logService = logService;
        this.userRepository = userRepository;

        createFilterButtons();

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

        newItemButton.setVisible(false);
        createExportButton();

        topLayout.add(logSelector);
        topLayout.add(buttonsWithDateFilters);

        filtersLayout.removeFilterableFields("fullLog");
//        filtersLayout.addFilterableFields("message");
//        filtersLayout.addFilterableFields("className");
//        filtersLayout.addFilterableFields("methodName");


        renderCommonComponents();

        defaultLastHour1Button.click();
        this.searchService = searchService;
    }

    private void createExportButton() {
        BervanButton exportButton = new BervanButton(new Icon(VaadinIcon.DOWNLOAD),
                e -> exportLogsToTextFile(), BervanButtonStyle.PRIMARY);
        exportButton.addClassName("export-logs-btn");
        topLayout.add(exportButton);
    }

    private void exportLogsToTextFile() {
        try {
            List<LogEntity> logsToExport = getAllLogs();

            if (logsToExport.isEmpty()) {
                showErrorNotification("No logs to export");
                return;
            }

            // Create text content
            StringBuilder textContent = new StringBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

            for (LogEntity logEntity : logsToExport) {
                if (logEntity.getJson() != null) {
                    textContent.append(logEntity.getJson()).append("\n");
                    continue;
                }

                String logLine = String.format("[%s] [%s] [%s.%s:%d] %s",
                        logEntity.getTimestamp() != null ? logEntity.getTimestamp().format(formatter) : "N/A",
                        logEntity.getLogLevel() != null ? logEntity.getLogLevel() : "N/A",
                        logEntity.getClassName() != null ? logEntity.getClassName() : "N/A",
                        logEntity.getMethodName() != null ? logEntity.getMethodName() : "N/A",
                        logEntity.getLineNumber(),
                        logEntity.getMessage() != null ? logEntity.getMessage() : "N/A"
                );
                textContent.append(logLine).append("\n");
            }

            String filename = String.format("logs_%s_%s.txt",
                    appName.replaceAll("[^a-zA-Z0-9]", "_"),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")));

            StreamResource resource = new StreamResource(filename,
                    () -> new ByteArrayInputStream(textContent.toString().getBytes()));

            Anchor downloadAnchor = new Anchor(resource, "download_link");
            downloadAnchor.getElement().setAttribute("download", true);
            downloadAnchor.getStyle().set("display", "none");

            add(downloadAnchor);
            downloadAnchor.getElement().executeJs("this.click();");
//            remove(downloadAnchor);

            showSuccessNotification(String.format("Exporting %d logs to %s", logsToExport.size(), filename));

            log.info("Exported {} logs to file {}", logsToExport.size(), filename);

        } catch (Exception e) {
            log.error("Error exporting logs to text file", e);
            showErrorNotification("Error exporting logs: " + e.getMessage());
        }
    }

    private List<LogEntity> getAllLogs() {
        SearchRequest request = filtersLayout.buildCombinedFilters();
        SearchQueryOption options = new SearchQueryOption();
        options.setEntityToFind(LogEntity.class);
        options.setPageSize(Integer.MAX_VALUE);
        options.setSortField("timestamp");
        options.setSortDirection(com.bervan.common.search.model.SortDirection.ASC);
        request.addCriterion("APP_NAME_EQ_CRITERION", Operator.OR_OPERATOR, LogEntity.class,
                "applicationName", SearchOperation.EQUALS_OPERATION, appName);

        request.setAddOwnerCriterion(false);
        SearchResponse<LogEntity> searchResponse = searchService.search(request, options);
        return searchResponse.getResultList();
    }

    private void createFilterButtons() {
        buttonsWithDateFilters.addClassName("logs-toolbar");

        BervanButton last5m = new BervanButton("Last 5m", click -> {
            try {
                filtersLayout.getDateTimeFiltersMap().get(LogEntity.class.getDeclaredField("timestamp"))
                        .get("FROM").setValue(LocalDateTime.now().minusMinutes(5));
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            showLastPage = true;
            super.refreshTable.click();
        });
        last5m.addClassName("glass-btn");
        buttonsWithDateFilters.add(last5m);

        BervanButton last10m = new BervanButton("Last 10m", click -> {
            try {
                filtersLayout.getDateTimeFiltersMap().get(LogEntity.class.getDeclaredField("timestamp"))
                        .get("FROM").setValue(LocalDateTime.now().minusMinutes(10));
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            showLastPage = true;
            super.refreshTable.click();
        });
        last10m.addClassName("glass-btn");
        buttonsWithDateFilters.add(last10m);

        BervanButton last30m = new BervanButton("Last 30m", click -> {
            try {
                filtersLayout.getDateTimeFiltersMap().get(LogEntity.class.getDeclaredField("timestamp"))
                        .get("FROM").setValue(LocalDateTime.now().minusMinutes(30));
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            showLastPage = true;
            super.refreshTable.click();
        });
        last30m.addClassName("glass-btn");
        buttonsWithDateFilters.add(last30m);

        defaultLastHour1Button.addClassName("glass-btn");
        defaultLastHour1Button.addClassName("primary");
        buttonsWithDateFilters.add(defaultLastHour1Button);

        BervanButton last2h = new BervanButton("Last 2h", click -> {
            try {
                filtersLayout.getDateTimeFiltersMap().get(LogEntity.class.getDeclaredField("timestamp"))
                        .get("FROM").setValue(LocalDateTime.now().minusHours(2));
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            showLastPage = true;
            super.refreshTable.click();
        });
        last2h.addClassName("glass-btn");
        buttonsWithDateFilters.add(last2h);

        BervanButton last6h = new BervanButton("Last 6h", click -> {
            try {
                filtersLayout.getDateTimeFiltersMap().get(LogEntity.class.getDeclaredField("timestamp"))
                        .get("FROM").setValue(LocalDateTime.now().minusHours(6));
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            showLastPage = true;
            super.refreshTable.click();
        });
        last6h.addClassName("glass-btn");
        buttonsWithDateFilters.add(last6h);

        BervanButton last24h = new BervanButton("Last 24h", click -> {
            try {
                filtersLayout.getDateTimeFiltersMap().get(LogEntity.class.getDeclaredField("timestamp"))
                        .get("FROM").setValue(LocalDateTime.now().minusHours(24));
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            showLastPage = true;
            super.refreshTable.click();
        });
        last24h.addClassName("glass-btn");
        buttonsWithDateFilters.add(last24h);

        BervanButton last3d = new BervanButton("Last 3d", click -> {
            try {
                filtersLayout.getDateTimeFiltersMap().get(LogEntity.class.getDeclaredField("timestamp"))
                        .get("FROM").setValue(LocalDateTime.now().minusHours(24 * 3));
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            showLastPage = true;
            super.refreshTable.click();
        });
        last3d.addClassName("glass-btn");
        buttonsWithDateFilters.add(last3d);

        BervanButton last7d = new BervanButton("Last 7d (max)", click -> {
            try {
                filtersLayout.getDateTimeFiltersMap().get(LogEntity.class.getDeclaredField("timestamp"))
                        .get("FROM").setValue(LocalDateTime.now().minusHours(24 * 7));
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            showLastPage = true;
            super.refreshTable.click();
        });
        last7d.addClassName("glass-btn");
        buttonsWithDateFilters.add(last7d);
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
        sortField = "timestamp";
        sortDirection = SortDirection.ASCENDING;

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
    protected void refreshData() {
        if (initSearch) {
            initSearch = false;
            return;
        }
        super.refreshData();
    }

    @Override
    protected List<LogEntity> loadData() {
        if (initSearch) {
            return new ArrayList<>();
        }
        List<LogEntity> logEntities = super.loadData();
        logEntities.forEach(e -> e.setFullLog(e.getFullLog()));
        return logEntities;
    }
}