package com.bervan.toolsapp.views.englishepub;

import com.bervan.common.config.BervanViewConfig;
import com.bervan.englishtextstats.service.ExtractedEbookTextRepository;
import com.bervan.englishtextstats.service.TextNotKnownWordsService;
import com.bervan.englishtextstats.service.WordService;
import com.bervan.englishtextstats.view.AbstractNotLearnedWordsView;
import com.bervan.languageapp.service.AddFlashcardService;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Deprecated
@Route(value = AbstractNotLearnedWordsView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
public class NotLearnedWordsView extends AbstractNotLearnedWordsView {
    public NotLearnedWordsView(WordService service, TextNotKnownWordsService textNotKnownWordsService, ExtractedEbookTextRepository extractedEbookTextRepository, AddFlashcardService addAsFlashcardService, BervanViewConfig bervanViewConfig) {
        super(service, extractedEbookTextRepository, textNotKnownWordsService, addAsFlashcardService, bervanViewConfig);
    }

}
