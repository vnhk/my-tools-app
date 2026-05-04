package com.bervan.toolsapp.views.interview;

import com.bervan.interviewapp.view.AbstractStartInterviewView;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.bervan.toolsapp.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = AbstractStartInterviewView.ROUTE_NAME, layout = MainLayout.class)

@RolesAllowed("USER")
@Deprecated
public class StartInterviewView extends AbstractStartInterviewView {

    public StartInterviewView() {
        super();
    }

}
