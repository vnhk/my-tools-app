package com.bervan.toolsapp.views.learninglanguage.en;

import com.bervan.asynctask.AsyncTaskService;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.search.SearchService;
import com.bervan.languageapp.service.ExampleOfUsageService;
import com.bervan.languageapp.service.TextToSpeechService;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.service.TranslatorService;
import com.bervan.languageapp.view.en.AbstractEnglishLearningTableView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = AbstractEnglishLearningTableView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
@Deprecated
public class EnglishLearningTableView extends AbstractEnglishLearningTableView {

    public EnglishLearningTableView(TranslationRecordService translatorRecordService,
                                    ExampleOfUsageService exampleOfUsageService,
                                    TextToSpeechService textToSpeechService,
                                    TranslatorService translatorService,
                                    SearchService searchService,
                                    AsyncTaskService asyncTaskService,
                                    BervanViewConfig bervanViewConfig) {
        super(translatorRecordService, exampleOfUsageService, textToSpeechService, translatorService,
                searchService, asyncTaskService, bervanViewConfig);
    }

}
