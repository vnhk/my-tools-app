package com.bervan.toolsapp.views.interview;

import com.bervan.interviewapp.view.AbstractInterviewHomeView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = AbstractInterviewHomeView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
@Deprecated
public class InterviewHomeView extends AbstractInterviewHomeView {

    public InterviewHomeView() {
        super();
    }
}
