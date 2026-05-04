package com.bervan.toolsapp.views.interview;

import com.bervan.common.onevalue.OneValueService;
import com.bervan.interviewapp.view.AbstractInterviewPlanView;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import com.bervan.toolsapp.views.MainLayout;

@Route(value = AbstractInterviewPlanView.ROUTE_NAME, layout = MainLayout.class)

@RolesAllowed("USER")
@Deprecated
public class InterviewPlanView extends AbstractInterviewPlanView {
    public InterviewPlanView(@Autowired OneValueService service) {
        super(service);
    }

}
