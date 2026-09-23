package com.bervan.toolsapp.views.englishepub;

import com.bervan.common.search.SearchService;
import com.bervan.englishtextstats.KnownWord;
import com.bervan.englishtextstats.service.ExtractedEbookTextRepository;
import com.bervan.englishtextstats.service.KnownWordRepository;
import com.bervan.englishtextstats.service.TextNotKnownWordsService;
import com.bervan.languageapp.service.TranslationRecordService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

import com.bervan.englishtextstats.service.ExtractedEbookTextService;

@Primary
@Service
public class IntegratedTextNotKnownWordsService extends TextNotKnownWordsService {
    private final TranslationRecordService translationRecordService;

    public IntegratedTextNotKnownWordsService(TranslationRecordService translationRecordService,
                                              ExtractedEbookTextRepository extractedEbookTextRepository,
                                              ExtractedEbookTextService extractedEbookTextService,
                                              KnownWordRepository knownWordRepository,
                                              SearchService searchService,
                                              @Value("${file.service.storage.folder.main}") String pathToFileStorage) {
        super(extractedEbookTextRepository, extractedEbookTextService, knownWordRepository, searchService, pathToFileStorage);
        this.translationRecordService = translationRecordService;
    }

    @Override
    public void loadIntoMemory() {
        super.loadIntoMemory();
        updateInMemoryWords(translationRecordService.load(Pageable.ofSize(1000000))
                .stream()
                .filter(e -> e != null && e.getSourceText() != null && !e.getSourceText().isBlank())
                .map(e -> {
                    KnownWord k = new KnownWord();
                    k.setValue(e.getSourceText());
                    return k;
                }).collect(Collectors.toList()));
    }
}
