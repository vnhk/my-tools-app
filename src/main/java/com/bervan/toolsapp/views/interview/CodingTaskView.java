package com.bervan.toolsapp.views.interview;

import com.bervan.common.config.BervanViewConfig;
import com.bervan.interviewapp.codingtask.CodingTaskService;
import com.bervan.interviewapp.view.AbstractCodingTaskView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = AbstractCodingTaskView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
@Deprecated
public class CodingTaskView extends AbstractCodingTaskView {
    public CodingTaskView(CodingTaskService service, BervanViewConfig bervanViewConfig) {
        super(service, bervanViewConfig);
    }

}
