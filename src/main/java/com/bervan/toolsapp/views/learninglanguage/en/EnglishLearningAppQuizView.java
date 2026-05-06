package com.bervan.toolsapp.views.learninglanguage.en;

import com.bervan.languageapp.service.ExampleOfUsageService;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.en.AbstractEnglishQuizView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = AbstractEnglishQuizView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
@Deprecated
public class EnglishLearningAppQuizView extends AbstractEnglishQuizView {

    public EnglishLearningAppQuizView(TranslationRecordService translatorRecordService, ExampleOfUsageService exampleOfUsageService) {
        super(translatorRecordService, exampleOfUsageService);
    }

}
