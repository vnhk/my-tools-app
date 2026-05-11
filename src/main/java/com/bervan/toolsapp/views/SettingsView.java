package com.bervan.toolsapp.views;

import com.bervan.common.view.AbstractPageView;
import com.bervan.common.component.BervanButton;
import com.bervan.common.MenuNavigationComponent;
import com.bervan.common.service.AuthService;
import com.bervan.common.user.User;
import com.bervan.common.user.UserRepository;
import com.google.common.base.Strings;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.RolesAllowed;

import static com.bervan.toolsapp.views.SettingsView.ROUTE_NAME;

@Route(value = ROUTE_NAME, layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@RolesAllowed({"USER", "STREAMING"})
@Deprecated
public class SettingsView extends AbstractPageView {
    public final static String ROUTE_NAME = "settings";
    private final UserRepository userRepository;

    public SettingsView(UserRepository userRepository) {
        this.userRepository = userRepository;

        add(new MenuNavigationComponent("") {
        });

        if (AuthService.getUserRole().equals("ROLE_USER")) {
            Div dataCipherForm = getDataCipherForm();
            add(dataCipherForm);
        }

        ComboBox<String> themeSelect = getThemeSelect();
        add(themeSelect);
    }

    private Div getDataCipherForm() {
        Div dataCipherForm = new Div();

        PasswordField dataCipherInput = new PasswordField();
        dataCipherInput.setHeight("25px");
        dataCipherInput.setWidth("200px");
        dataCipherInput.setPlaceholder("Enter data cipher");
        BervanButton checkButton = new BervanButton("Check", click -> {
            String cipher = dataCipherInput.getValue();
            if (cipher.equals(AuthService.getLoggedUser().get().getDataCipherPassword())) {
                showPrimaryNotification("Data cipher password is correct!");
            } else {
                showErrorNotification("Data cipher password is incorrect!");
            }
        });

        BervanButton saveButton = new BervanButton("Save", click -> {
            String cipher = dataCipherInput.getValue();
            if (!Strings.isNullOrEmpty(AuthService.getLoggedUser().get().getDataCipherPassword())) {
                ConfirmDialog confirmDialog = new ConfirmDialog("WARNING!", "Setting a new password may result in data loss. Data encrypted with a different password will not be decrypted.",
                        "I understand that this may result in data loss.", (confirmEvent -> {
                    if (Strings.isNullOrEmpty(cipher) || cipher.length() < 5) {
                        showErrorNotification("Data cipher password must be at least 5 characters long!");
                        return;
                    } else {
                        saveDataCipherPassword(cipher);
                    }
                }), "Cancel", null);

                confirmDialog.open();
            } else {
                saveDataCipherPassword(cipher);
            }

        });

        dataCipherForm.add(dataCipherInput, new Hr(), new HorizontalLayout(checkButton, saveButton));

        return dataCipherForm;
    }

    private void saveDataCipherPassword(String cipher) {
        User user = AuthService.getLoggedUser().get();
        user.setDataCipherPassword(cipher);
        userRepository.save(user);
        showPrimaryNotification("Data cipher password saved!");
    }

    private ComboBox<String> getThemeSelect() {
        ComboBox<String> themeSelect = new ComboBox<>("Select Theme");
        themeSelect.setItems("cyberpunk", "bloodmoon", "ocean", "sunset", "darkula", "intellij", "earth", "frost", "blossom");
        themeSelect.addValueChangeListener(event -> {
            String selectedTheme = event.getValue();
            if (selectedTheme != null) {
                setTheme(selectedTheme);
            }
        });
        return themeSelect;
    }

    private void setTheme(String themeName) {
        getElement().executeJs("localStorage.setItem('theme', '" + themeName + "');")
                .then(e -> UI.getCurrent().getPage().reload());
    }
}