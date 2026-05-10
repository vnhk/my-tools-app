package com.bervan.toolsapp.views.spreadsheetapp;

import com.bervan.spreadsheet.service.SpreadsheetService;
import com.bervan.spreadsheet.view.AbstractSpreadsheetView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Deprecated
@Route(value = AbstractSpreadsheetView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
public class SpreadsheetView extends AbstractSpreadsheetView implements HasUrlParameter<String> {

    public SpreadsheetView(SpreadsheetService service) {
        super(service);
    }
}