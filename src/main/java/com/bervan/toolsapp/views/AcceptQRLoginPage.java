package com.bervan.toolsapp.views;

import com.bervan.common.component.BervanComboBox;
import com.bervan.common.service.AuthService;
import com.bervan.common.user.User;
import com.bervan.common.user.UserToUserRelation;
import com.bervan.common.view.AbstractPageView;
import com.bervan.toolsapp.security.QRLoginService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.*;
import java.util.stream.Stream;

@Route("accept-login")
@PageTitle("Accept Login")
@PermitAll
@Deprecated
public class AcceptQRLoginPage extends AbstractPageView implements HasUrlParameter<String> {

    private final QRLoginService qrLoginService;
    private String uuid;
    private QRLoginService.QRLoginData qrLoginData;

    public AcceptQRLoginPage(QRLoginService qrLoginService) {
        this.qrLoginService = qrLoginService;
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        this.uuid = parameter;
        this.qrLoginData = qrLoginService.getQRLoginData(uuid);

        if (qrLoginData == null || qrLoginData.isExpired() || qrLoginData.isUsed()) {
            showExpiredView();
        } else {
            showLoginView();
        }
    }

    private void showExpiredView() {
        removeAll();
        addClassName("login-page");

        H2 title = new H2("Session Expired");
        Paragraph message = new Paragraph("This QR code has expired or been used already.");
        Button backButton = new Button("Back to Login", e ->
                getUI().ifPresent(ui -> ui.navigate("/login")));
        backButton.addClassName("accept-login-btn");

        VerticalLayout card = new VerticalLayout(title, message, backButton);
        card.addClassName("accept-login-card");
        card.setAlignItems(Alignment.CENTER);
        card.setJustifyContentMode(JustifyContentMode.CENTER);

        add(card);
    }

    private void showLoginView() {
        removeAll();
        addClassName("login-page");

        H2 title = new H2("Confirm Login");
        title.addClassName("accept-login-title");

        Paragraph instruction = new Paragraph("Enter the number from your QR code:");
        instruction.addClassName("accept-login-instruction");

        IntegerField numberField = new IntegerField("Number");
        numberField.setMin(1);
        numberField.setMax(99);
        numberField.addClassName("accept-login-number-field");

        Optional<User> loggedUser = AuthService.getLoggedUser();
        if (loggedUser.isEmpty()) {
            throw new IllegalStateException("User is not logged in!");
        }
        User user = loggedUser.get();
        Set<UserToUserRelation> childrenRelations = user.getChildrenRelations();
        List<String> availableAccountsList = new ArrayList<>(childrenRelations.stream().map(UserToUserRelation::getChild).map(e -> e.getUsername() + ":" + e.getRole()).toList());
        availableAccountsList.add(user.getUsername() + ":" + user.getRole());
        BervanComboBox<String> availableAccounts = new BervanComboBox<>(availableAccountsList);
        availableAccounts.setValue(availableAccountsList.get(0));
        availableAccounts.addClassName("accept-login-combo");
        availableAccounts.setPlaceholder("Select account");

        Button loginButton = new Button("Login", e -> {
            Integer enteredNumber = numberField.getValue();
            if (enteredNumber == null) {
                showErrorNotification("Please enter a number");
                return;
            }

            Set<User> users = new HashSet<>();
            for (UserToUserRelation childrenRelation : childrenRelations) {
                users.add(childrenRelation.getChild());
                users.add(childrenRelation.getParent());
            }

            Stream<User> userStream = users.stream();

            if (qrLoginService.validateAndAuthenticateQRLogin(uuid, enteredNumber, userStream.filter(u -> (u.getUsername() + ":" + u.getRole()).equals(availableAccounts.getValue())).findAny().get())) {
                showPrimaryNotification("Authentication successful! You can now close this page.");
            } else {
                showErrorNotification("Invalid number. Please try again.");
                numberField.clear();
            }
        });
        loginButton.addClassName("accept-login-btn");
        loginButton.addClassName("primary");

        Button cancelButton = new Button("Cancel", e ->
                getUI().ifPresent(ui -> ui.navigate("/login")));
        cancelButton.addClassName("accept-login-btn");

        VerticalLayout buttonLayout = new VerticalLayout(loginButton, cancelButton);
        buttonLayout.addClassName("accept-login-buttons");
        buttonLayout.setSpacing(true);
        buttonLayout.setPadding(false);
        buttonLayout.setAlignItems(Alignment.STRETCH);

        VerticalLayout card = new VerticalLayout(title, instruction, availableAccounts, numberField, buttonLayout);
        card.addClassName("accept-login-card");
        card.setAlignItems(Alignment.CENTER);
        card.setJustifyContentMode(JustifyContentMode.CENTER);
        card.setSpacing(true);

        add(card);
    }
}