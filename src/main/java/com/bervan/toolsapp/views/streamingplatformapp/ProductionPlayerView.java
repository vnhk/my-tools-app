package com.bervan.toolsapp.views.streamingplatformapp;

import com.bervan.asynctask.AsyncTaskService;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.englishtextstats.service.WordService;
import com.bervan.filestorage.model.BervanMockMultiPartFile;
import com.bervan.filestorage.model.Metadata;
import com.bervan.filestorage.service.FileServiceManager;
import com.bervan.languageapp.service.AddFlashcardService;
import com.bervan.logging.JsonLogger;
import com.bervan.streamingapp.VideoManager;
import com.vaadin.flow.component.Component;
import com.bervan.streamingapp.config.ProductionData;
import com.bervan.streamingapp.config.StreamingConfigLoader;
import com.bervan.streamingapp.view.player.AbstractProductionPlayerView;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

@Route(value = AbstractProductionPlayerView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed({"USER", "STREAMING"})
@Deprecated
public class ProductionPlayerView extends AbstractProductionPlayerView {

    private final JsonLogger log = JsonLogger.getLogger(getClass(), "my-tools-app");
    private final VideoManager videoManager;
    private final FileServiceManager fileServiceManager;
    private final StreamingConfigLoader streamingConfigLoader;

    public ProductionPlayerView(VideoManager videoManager,
                                WordService wordService,
                                FileServiceManager fileServiceManager,
                                AsyncTaskService asyncTaskService,
                                AddFlashcardService addAsFlashcardService,
                                BervanViewConfig bervanViewConfig,
                                StreamingConfigLoader streamingConfigLoader,
                                Map<String, ProductionData> streamingProductionData) {
        super(videoManager, streamingProductionData);
        this.videoManager = videoManager;
        this.fileServiceManager = fileServiceManager;
        this.streamingConfigLoader = streamingConfigLoader;
    }

    @Override
    protected Component buildSubtitleSettingsContent(String videoId, Metadata video) {
        Span header = new Span("Upload Subtitle");
        header.getStyle()
                .set("font-weight", "600")
                .set("font-size", "var(--lumo-font-size-m)");

        Select<String> langSelect = new Select<>();
        langSelect.setLabel("Language (select if not in filename)");
        langSelect.setItems(VideoManager.EN, VideoManager.PL, VideoManager.ES);
        langSelect.setPlaceholder("Auto-detect from filename");

        Checkbox reloadConfigCheckbox = new Checkbox("Reload Config");
        reloadConfigCheckbox.setValue(false);

        FileBuffer buffer = new FileBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".vtt", ".srt");
        upload.setMaxFiles(1);

        upload.addSucceededListener(event -> {
            try {
                String originalFilename = event.getFileName();
                String targetPath = video.getPath() + video.getFilename() + File.separator;

                Optional<String> detected = videoManager.detectSubtitleLanguage(originalFilename);
                String finalFilename;
                if (detected.isPresent()) {
                    finalFilename = originalFilename;
                } else {
                    String selectedLang = langSelect.getValue();
                    if (selectedLang == null || selectedLang.isBlank()) {
                        showErrorNotification("Language not detected in filename and no language selected. Please select a language and re-upload.");
                        upload.clearFileList();
                        return;
                    }
                    finalFilename = insertLangIntoFilename(originalFilename, selectedLang);
                }

                InputStream inputStream = buffer.getInputStream();
                BervanMockMultiPartFile mockFile = new BervanMockMultiPartFile(
                        finalFilename, finalFilename, null, inputStream
                );
                fileServiceManager.save(mockFile, "", targetPath);

                if (Boolean.TRUE.equals(reloadConfigCheckbox.getValue())) {
                    Map<String, ProductionData> newData = streamingConfigLoader.getStringProductionDataMap();
                    streamingProductionData.clear();
                    streamingProductionData.putAll(newData);
                    showSuccessNotification("Subtitle uploaded as \"" + finalFilename + "\" and config reloaded!");
                } else {
                    showSuccessNotification("Subtitle uploaded as \"" + finalFilename + "\"!");
                }

                langSelect.clear();
            } catch (Exception e) {
                log.error("Failed to upload subtitle", e);
                showErrorNotification("Failed to upload subtitle!");
            }
        });

        VerticalLayout uploadSection = new VerticalLayout(header, langSelect, upload, reloadConfigCheckbox);
        uploadSection.setSpacing(true);
        uploadSection.setPadding(false);
        return uploadSection;
    }

    /**
     * Inserts language code before the file extension: "movie.vtt" → "movie_en.vtt"
     */
    private String insertLangIntoFilename(String filename, String lang) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            return filename + "." + lang;
        }
        return filename.substring(0, dotIndex) + "_" + lang + filename.substring(dotIndex);
    }
}
