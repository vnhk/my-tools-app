package com.bervan.toolsapp.views.asynctask;

import com.bervan.asynctask.AsyncTask;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.service.BaseService;
import com.bervan.common.view.AbstractAsyncTaskList;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.UUID;


@Deprecated
@Route(value = AbstractAsyncTaskList.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed({"USER"})
public class AsyncTaskListView extends AbstractAsyncTaskList {
    public AsyncTaskListView(BaseService<UUID, AsyncTask> service, BervanViewConfig bervanViewConfig) {
        super(service, bervanViewConfig);
    }
}
