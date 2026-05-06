package com.bervan.toolsapp.views.learninglanguage.es;

import com.bervan.asynctask.AsyncTaskService;
import com.bervan.common.search.SearchService;
import com.bervan.languageapp.service.ExampleOfUsageService;
import com.bervan.languageapp.service.TextToSpeechService;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.service.TranslatorService;
import com.bervan.languageapp.view.es.AbstractSpanishFastImportView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

//@Route(value = AbstractSpanishFastImportView.ROUTE_NAME, layout = MainLayout.class)
//@RolesAllowed("USER")
@Deprecated
public class SpanishFastImportView extends AbstractSpanishFastImportView {

    public SpanishFastImportView(TranslationRecordService translationRecordService, TextToSpeechService textToSpeechService, SearchService searchService, ExampleOfUsageService exampleOfUsageService, TranslatorService translatorService, AsyncTaskService asyncTaskService) {
        super(translationRecordService, textToSpeechService, searchService, exampleOfUsageService, translatorService, asyncTaskService);
    }
}
