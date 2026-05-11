package com.bervan.toolsapp.views;


import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.service.AuthService;
import com.bervan.pocketapp.pocket.PocketService;
import com.bervan.pocketapp.pocketitem.PocketItemService;
import com.bervan.toolsapp.ProfileUtils;
import com.bervan.toolsapp.views.asynctask.AsyncTaskListView;
import com.bervan.toolsapp.views.canvasapp.CanvasPagesView;
import com.bervan.toolsapp.views.englishepub.NotLearnedWordsView;
import com.bervan.toolsapp.views.filestorage.FileStorageView;
import com.bervan.toolsapp.views.interview.InterviewHomeView;
import com.bervan.toolsapp.views.investtrackapp.BudgetDashboardView;
import com.bervan.toolsapp.views.learninglanguage.en.EnglishLearningAppHomeView;
import com.bervan.toolsapp.views.learninglanguage.es.SpanishLearningAppHomeView;
import com.bervan.toolsapp.views.logsapp.LogItemsTableView;
import com.bervan.toolsapp.views.lowcode.LowCodeGeneratorView;
import com.bervan.toolsapp.views.otpview.OTPGenerateView;
import com.bervan.toolsapp.views.pocketapp.PocketSideMenuView;
import com.bervan.toolsapp.views.pocketapp.PocketTableView;
import com.bervan.toolsapp.views.cookbook.DietView;
import com.bervan.toolsapp.views.cookbook.RecipeListView;
import com.bervan.toolsapp.views.projectmgmtapp.ProjectListView;
import com.bervan.toolsapp.views.shopapp.ProductsView;
import com.bervan.toolsapp.views.spreadsheetapp.SpreadsheetsView;
import com.bervan.toolsapp.views.streamingplatformapp.ProductionListView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.shared.Registration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The main view is a top-level placeholder for other views.
 */
@JsModule("./theme-changer.js")
@Deprecated
public class MainLayout extends AppLayout {

    private static Div sideMenu;
    private static Registration clickListenerRegistration;

    private final PocketItemService pocketItemService;
    private H1 viewTitle;

    public MainLayout(PocketService pocketService, PocketItemService pocketItemService, BervanViewConfig bervanViewConfig) {

        this.pocketItemService = pocketItemService;

        setPrimarySection(Section.DRAWER);
        addToNavbar(true, createHeaderContent());
        addToDrawer(createDrawerContent());
        PocketSideMenuView pocketSideMenu = new PocketSideMenuView(this.pocketItemService, pocketService, bervanViewConfig);
        sideMenu = createSideMenu(pocketSideMenu);
        Button pocketMenuButton = new Button(VaadinIcon.CLIPBOARD.create());
        pocketMenuButton.addClassName("option-button");
        pocketMenuButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        pocketMenuButton.getStyle().set("position", "fixed").set("top", "10px").set("right", "10px");
        pocketMenuButton.addClickListener(event -> {
            pocketSideMenu.reloadItems();
            toggleMenuVisibility();
        });

        if (AuthService.getUserRole().equals("ROLE_USER")) {
            addToNavbar(sideMenu);
            addToNavbar(pocketMenuButton);
        }
    }

    private static void showMenu() {
        sideMenu.addClassName("visible");
        registerClickListener();
    }

    private static void hideMenu() {
        sideMenu.removeClassName("visible");
        unregisterClickListener();
    }

    private static void registerClickListener() {
        clickListenerRegistration = UI.getCurrent().getCurrentView().getElement().addEventListener("click", event -> {

            List<Component> collect = sideMenu.getChildren().collect(Collectors.toList());
            boolean isPocketClicked = isPocketClicked(collect, event.getSource());

            if (!isPocketClicked && !sideMenu.getElement().equals(event.getSource())) {
                hideMenu();
            }
        });
    }

    private static boolean isPocketClicked(List<Component> collect, Element source) {
        for (Component component : collect) {
            if (component.getElement().equals(source)) {
                return true;
            } else {
                if (isPocketClicked(component.getChildren().collect(Collectors.toList()), source)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void unregisterClickListener() {
        if (clickListenerRegistration != null) {
            clickListenerRegistration.remove();
            clickListenerRegistration = null;
        }
    }

    private void toggleMenuVisibility() {
        if (sideMenu.getClassNames().contains("visible")) {
            hideMenu();
        } else {
            showMenu();
        }
    }

    private Div createSideMenu(Component pocketSideMenu) {
        Div sideMenu = new Div();
        sideMenu.addClassName("side-menu");
        VerticalLayout menuLayout = new VerticalLayout();
        menuLayout.add(pocketSideMenu);

        sideMenu.add(menuLayout);
        return sideMenu;
    }

    private Component createHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassNames("view-toggle");
        toggle.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames("view-title");

        Header header = new Header(toggle, viewTitle);
        header.addClassNames("view-header");
        return header;
    }

    private Component createDrawerContent() {
        H2 appName = new H2("Tools");
        appName.addClassNames("app-name");

        com.vaadin.flow.component.html.Section section = new com.vaadin.flow.component.html.Section(appName, createNavigation(), createFooter());
        section.addClassNames("drawer-section");
        return section;
    }

    private Nav createNavigation() {
        Nav nav = new Nav();
        nav.addClassNames("menu-item-container");
        nav.getElement().setAttribute("aria-labelledby", "views");

        // Wrap the links in a list; improves accessibility
        UnorderedList list = new UnorderedList();
        list.addClassNames("navigation-list");
        nav.add(list);

        for (MenuItemInfo menuItem : createMenuItems()) {
            list.add(menuItem);

        }
        return nav;
    }

    private MenuItemInfo[] createMenuItems() {
        if (AuthService.getUserRole().equals("ROLE_USER")) {
            return new MenuItemInfo[]{ //
                    new MenuItemInfo("Home", "la la-home", MainHome.class), //
                    new MenuItemInfo("OTP", "la la-barcode", OTPGenerateView.class), //
                    new MenuItemInfo("Interview", "la la-desktop", InterviewHomeView.class), //
                    new MenuItemInfo("Pocket", "la la-get-pocket", PocketTableView.class), //
                    new MenuItemInfo("Ebook English Words", "la la-book", NotLearnedWordsView.class), //
                    new MenuItemInfo("English Learning \uD83C\uDDFA\uD83C\uDDF8", "la la-language", EnglishLearningAppHomeView.class), //
                    new MenuItemInfo("Spanish Learning \uD83C\uDDEA\uD83C\uDDF8", "la la-language", SpanishLearningAppHomeView.class), //
                    new MenuItemInfo("Project Management", "la la-project-diagram", ProjectListView.class), //
                    new MenuItemInfo("File Storage", "la la-cloud-upload", FileStorageView.class), //
                    new MenuItemInfo("Spreadsheets", "las la-file-excel", SpreadsheetsView.class), //
                    new MenuItemInfo("Budget", "la la-money", BudgetDashboardView.class, BudgetDashboardView.subMenu()), //
                    new MenuItemInfo("Notepad", "las la-chalkboard", CanvasPagesView.class), //
                    new MenuItemInfo("Streaming", "las la-video", ProductionListView.class), //
                    new MenuItemInfo("Cook Book", "la la-utensils", RecipeListView.class), //
                    new MenuItemInfo("Diet", "la la-heartbeat", DietView.class), //
                    new MenuItemInfo("Shopping", "la la-shopping-cart", ProductsView.class), //
                    new MenuItemInfo("Logs", "la la-database", LogItemsTableView.class), //
                    new MenuItemInfo("Async Tasks", "las la-wind", AsyncTaskListView.class), //
                    new MenuItemInfo("Low Code", "las la-code", LowCodeGeneratorView.class, isLocal()), //
                    new MenuItemInfo("Settings", "las la-cog", SettingsView.class), //
            };
        } else if (AuthService.getUserRole().equals("ROLE_STREAMING")) {
            return new MenuItemInfo[]{ //
                    new MenuItemInfo("Home", "la la-home", MainHome.class), //
                    new MenuItemInfo("Streaming", "las la-video", ProductionListView.class), //
                    new MenuItemInfo("Settings", "las la-cog", SettingsView.class), //
            };
        } else {
            return new MenuItemInfo[]{ //
                    new MenuItemInfo("Login", "las la-login", LoginView.class) //
            };
        }
    }

    private boolean isLocal() {
        return ProfileUtils.isLocal();
    }

    private Footer createFooter() {
        Footer layout = new Footer();
        layout.addClassNames("footer");
        layout.add(new Span("| Designed by Bervan ©"));

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        setDrawerOpened(false);
        viewTitle.setText(getCurrentPageTitle());

        // Clear navigation buttons only if the new page doesn't have MenuNavigationComponent
        // Pages with MenuNavigationComponent handle cleanup themselves in their init()
        Component content = getContent();
        boolean hasMenuNavigation = hasMenuNavigationComponent(content);

        if (!hasMenuNavigation) {
            UI.getCurrent().getPage().executeJs(
                "document.querySelectorAll('navigation-buttons').forEach(el => el.remove());"
            );
        }
    }

    private boolean hasMenuNavigationComponent(Component component) {
        if (component == null) return false;
        if (component instanceof com.bervan.common.MenuNavigationComponent) return true;

        // Check children recursively
        return component.getChildren().anyMatch(this::hasMenuNavigationComponent);
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }

    public static class MenuItemInfo extends ListItem {

        private final Class<? extends Component> view;

        public MenuItemInfo(String title, String icon, Class<? extends Component> view, boolean visible, MenuItemInfo... children) {
            this(title, icon, view, children);
            setVisible(visible);
        }

        public MenuItemInfo(String title, String icon, Class<? extends Component> view, MenuItemInfo... children) {
            this.view = view;

            RouterLink link = new RouterLink();
            link.addClassNames("menu-item-link");
            link.setRoute(view);

            Span text = new Span(title);
            text.addClassNames("menu-item-text");

            link.add(new LineAwesomeIcon(icon), text);
            add(link);

            // SUBMENU
            if (children != null && children.length > 0) {
                UnorderedList subList = new UnorderedList();
                subList.addClassName("submenu");

                for (MenuItemInfo child : children) {
                    subList.add(child);
                }
                add(subList);
            }

            addClickListener(e -> hideMenu());
        }
    }

    @NpmPackage(value = "line-awesome", version = "1.3.0")
    public static class LineAwesomeIcon extends Span {
        public LineAwesomeIcon(String lineAwesomeClassnames) {
            addClassNames("menu-item-icon");
            if (!lineAwesomeClassnames.isEmpty()) {
                addClassNames(lineAwesomeClassnames);
            }
        }
    }
}
