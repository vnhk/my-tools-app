package com.bervan.toolsapp.views.learninglanguage.es;

import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.es.AbstractSpanishLearningView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = AbstractSpanishLearningView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
@Deprecated
public class SpanishLearningAppLearningView extends AbstractSpanishLearningView {

    public SpanishLearningAppLearningView(TranslationRecordService translatorRecordService) {
        super(translatorRecordService);
    }

}
