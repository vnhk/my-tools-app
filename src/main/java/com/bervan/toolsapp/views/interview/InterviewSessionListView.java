package com.bervan.toolsapp.views.interview;

import com.bervan.interviewapp.view.AbstractInterviewSessionListView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = AbstractInterviewSessionListView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
@Deprecated
public class InterviewSessionListView extends AbstractInterviewSessionListView {

    public InterviewSessionListView() {
        super();
    }
}
