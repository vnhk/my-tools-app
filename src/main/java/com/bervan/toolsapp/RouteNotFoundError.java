package com.bervan.toolsapp;

import com.bervan.common.view.AbstractPageNotFoundErrorView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpServletResponse;

@AnonymousAllowed
@Deprecated
public class RouteNotFoundError extends VerticalLayout
        implements HasErrorParameter<NotFoundException> {

    @Override
    public int setErrorParameter(BeforeEnterEvent event,
                                 ErrorParameter<NotFoundException> parameter) {

        AbstractPageNotFoundErrorView abstractPageNotFoundError = new AbstractPageNotFoundErrorView() {
        };
        add(abstractPageNotFoundError);
        return HttpServletResponse.SC_NOT_FOUND;
    }
}