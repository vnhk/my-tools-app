package com.bervan.toolsapp.views.learninglanguage.en;

import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.en.AbstractEnglishLearningView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = AbstractEnglishLearningView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
@Deprecated
public class EnglishLearningAppLearningView extends AbstractEnglishLearningView {

    public EnglishLearningAppLearningView(TranslationRecordService translatorRecordService) {
        super(translatorRecordService);
    }

}
