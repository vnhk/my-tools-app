package com.bervan.toolsapp.views;

import com.bervan.common.service.AuthService;
import com.bervan.common.view.AbstractHomePageView;
import com.bervan.toolsapp.views.asynctask.AsyncTaskListView;
import com.bervan.toolsapp.views.canvasapp.CanvasPagesView;
import com.bervan.toolsapp.views.englishepub.NotLearnedWordsView;
import com.bervan.toolsapp.views.filestorage.FileStorageView;
import com.bervan.toolsapp.views.interview.InterviewHomeView;
import com.bervan.toolsapp.views.investtrackapp.BudgetDashboardView;
import com.bervan.toolsapp.views.learninglanguage.en.EnglishLearningAppHomeView;
import com.bervan.toolsapp.views.learninglanguage.es.SpanishLearningAppHomeView;
import com.bervan.toolsapp.views.logsapp.LogItemsTableView;
import com.bervan.toolsapp.views.otpview.OTPGenerateView;
import com.bervan.toolsapp.views.pocketapp.PocketTableView;
import com.bervan.toolsapp.views.projectmgmtapp.ProjectListView;
import com.bervan.toolsapp.views.shopapp.ProductsView;
import com.bervan.toolsapp.views.spreadsheetapp.SpreadsheetsView;
import com.bervan.toolsapp.views.streamingplatformapp.ProductionListView;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@Route(value = "home", layout = MainLayout.class)
@RolesAllowed({"USER", "STREAMING"})
@Deprecated
public class MainHome extends AbstractHomePageView {
    public MainHome() {
        if (AuthService.getUserRole().equals("ROLE_USER")) {
            add(createQuickAccessSection(
                    List.of(
                            "OTP", "Interview", "Pocket", "Ebook English Words",
                            "English Learning 🇺🇸", "Spanish Learning 🇪🇸",
                            "Project Management", "File Storage", "Spreadsheets",
                            "Investments", "Notepad", "Streaming", "Shopping",
                            "Logs", "Async Tasks", "Settings"
                    ),
                    List.of(
                            "Generate one-time passwords.",
                            "Prepare for interviews.",
                            "Manage your saved content.",
                            "Review English words from ebooks.",
                            "Explore English learning tools.",
                            "Start learning Spanish.",
                            "Track your projects.",
                            "Upload and manage files.",
                            "Work with spreadsheets.",
                            "Monitor your investments.",
                            "Write and draw freely.",
                            "Manage video streaming content.",
                            "Shop and manage your products.",
                            "View system logs.",
                            "Track background processes.",
                            "Configure application settings."
                    ),
                    List.of(
                            VaadinIcon.BARCODE.create(),
                            VaadinIcon.DESKTOP.create(),
                            VaadinIcon.ADD_DOCK.create(),
                            VaadinIcon.BOOK.create(),
                            VaadinIcon.DIPLOMA_SCROLL.create(),
                            VaadinIcon.DIPLOMA_SCROLL.create(),
                            VaadinIcon.TASKS.create(),
                            VaadinIcon.CLOUD_UPLOAD.create(),
                            VaadinIcon.FILE_TABLE.create(),
                            VaadinIcon.MONEY.create(),
                            VaadinIcon.EDIT.create(),
                            VaadinIcon.FILE_MOVIE.create(),
                            VaadinIcon.CART.create(),
                            VaadinIcon.DATABASE.create(),
                            VaadinIcon.AIRPLANE.create(),
                            VaadinIcon.COG.create()
                    ),
                    List.of(
                            OTPGenerateView.ROUTE_NAME,
                            InterviewHomeView.ROUTE_NAME,
                            PocketTableView.ROUTE_NAME,
                            NotLearnedWordsView.ROUTE_NAME,
                            EnglishLearningAppHomeView.ROUTE_NAME,
                            SpanishLearningAppHomeView.ROUTE_NAME,
                            ProjectListView.ROUTE_NAME,
                            FileStorageView.ROUTE_NAME,
                            SpreadsheetsView.ROUTE_NAME,
                            BudgetDashboardView.ROUTE_NAME,
                            CanvasPagesView.ROUTE_NAME,
                            ProductionListView.ROUTE_NAME,
                            ProductsView.ROUTE_NAME,
                            LogItemsTableView.ROUTE_NAME,
                            AsyncTaskListView.ROUTE_NAME,
                            SettingsView.ROUTE_NAME
                    )
            ));
        } else if (AuthService.getUserRole().equals("ROLE_STREAMING")) {
            add(createQuickAccessSection(
                    List.of("Streaming", "Settings"),
                    List.of("Manage video content.", "Configure streaming settings."),
                    List.of(VaadinIcon.FILE_MOVIE.create(), VaadinIcon.COG.create()),
                    List.of(ProductionListView.ROUTE_NAME, SettingsView.ROUTE_NAME)
            ));
        }
    }
}