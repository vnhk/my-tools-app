package com.bervan.toolsapp.views.learninglanguage.es;

import com.bervan.languageapp.service.ExampleOfUsageService;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.es.AbstractSpanishQuizView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = AbstractSpanishQuizView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
@Deprecated
public class SpanishLearningAppQuizView extends AbstractSpanishQuizView {

    public SpanishLearningAppQuizView(TranslationRecordService translatorRecordService, ExampleOfUsageService exampleOfUsageService) {
        super(translatorRecordService, exampleOfUsageService);
    }

}
