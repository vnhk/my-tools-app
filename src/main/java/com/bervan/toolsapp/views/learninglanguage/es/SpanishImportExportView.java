package com.bervan.toolsapp.views.learninglanguage.es;

import com.bervan.common.config.BervanViewConfig;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.es.AbstractSpanishImportExportView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = AbstractSpanishImportExportView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
@Deprecated
public class SpanishImportExportView extends AbstractSpanishImportExportView {

    public SpanishImportExportView( TranslationRecordService translationRecordService, BervanViewConfig bervanViewConfig) {
        super( translationRecordService, bervanViewConfig);
    }
}
