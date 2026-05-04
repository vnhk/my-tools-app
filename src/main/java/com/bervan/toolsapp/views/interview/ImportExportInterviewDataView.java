package com.bervan.toolsapp.views.interview;

import com.bervan.common.model.PersistableTableOwnedData;
import com.bervan.common.service.BaseService;
import com.bervan.interviewapp.view.AbstractImportExportView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.UUID;

@Route(value = AbstractImportExportView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
@Deprecated
public class ImportExportInterviewDataView extends AbstractImportExportView {

    public ImportExportInterviewDataView(List<BaseService<UUID, ? extends PersistableTableOwnedData<?>>> dataServices) {
        super(dataServices);
    }
}
