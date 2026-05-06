package com.bervan.toolsapp.views.learninglanguage.es;

import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.es.AbstractSpanishLearningAppHomeView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = AbstractSpanishLearningAppHomeView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
@Deprecated
public class SpanishLearningAppHomeView extends AbstractSpanishLearningAppHomeView {

    public SpanishLearningAppHomeView(TranslationRecordService translationRecordsService) {
        super(translationRecordsService);
    }

}
