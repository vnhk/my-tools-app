package com.bervan.toolsapp.views.logsapp;

import com.bervan.common.MenuNavigationComponent;
import com.vaadin.flow.component.icon.VaadinIcon;

@Deprecated
public final class LogsAppPageLayout extends MenuNavigationComponent {

    public LogsAppPageLayout(String currentRouteName) {
        super(currentRouteName);

        addButtonIfVisible(menuButtonsRow, LogItemsTableView.ROUTE_NAME, "Logs", VaadinIcon.FILE_TEXT.create());
        addButtonIfVisible(menuButtonsRow, LogItemsTableView.ROUTE_NAME, "Trackers", VaadinIcon.TASKS.create());

        add(menuButtonsRow);

    }
}
