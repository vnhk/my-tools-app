package com.bervan.toolsapp.views.learninglanguage.en;

import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.en.AbstractEnglishLearningAppHomeView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = AbstractEnglishLearningAppHomeView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
@Deprecated
public class EnglishLearningAppHomeView extends AbstractEnglishLearningAppHomeView {

    public EnglishLearningAppHomeView(TranslationRecordService translationRecordService) {
        super(translationRecordService);
    }

}
