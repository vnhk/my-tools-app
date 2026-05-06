package com.bervan.toolsapp.views.learninglanguage.en;

import com.bervan.common.config.BervanViewConfig;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.en.AbstractEnglishImportExportView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = AbstractEnglishImportExportView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
@Deprecated
public class EnglishImportExportView extends AbstractEnglishImportExportView {

    public EnglishImportExportView( TranslationRecordService translationRecordService, BervanViewConfig bervanViewConfig) {
        super( translationRecordService, bervanViewConfig);
    }
}
