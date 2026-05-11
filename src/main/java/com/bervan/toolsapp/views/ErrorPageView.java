package com.bervan.toolsapp.views;

import com.bervan.common.view.AbstractPageNotFoundErrorView;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
@Route(value = "/application-error", layout = MainLayout.class)
@Deprecated
public class ErrorPageView extends AbstractPageNotFoundErrorView {
}
