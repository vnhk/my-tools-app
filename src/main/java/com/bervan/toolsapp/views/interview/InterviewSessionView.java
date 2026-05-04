package com.bervan.toolsapp.views.interview;

import com.bervan.interviewapp.view.AbstractInterviewSessionView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = AbstractInterviewSessionView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
@Deprecated
public class InterviewSessionView extends AbstractInterviewSessionView {

    public InterviewSessionView() {
        super();
    }
}
