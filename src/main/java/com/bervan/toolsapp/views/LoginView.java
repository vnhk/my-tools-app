package com.bervan.toolsapp.views;

import com.bervan.common.component.BervanButton;
import com.bervan.common.component.BervanButtonStyle;
import com.bervan.common.service.AuthService;
import com.bervan.common.view.AbstractPageView;
import com.bervan.logging.JsonLogger;
import com.bervan.toolsapp.security.OtpAuthenticationToken;
import com.bervan.toolsapp.security.QRLoginService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import static com.bervan.toolsapp.security.OTPService.CODE_LENGTH;

@Route("login")
@PageTitle("Login")
@PermitAll
@Deprecated
public class LoginView extends AbstractPageView {
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "my-tools-app");

    private final AuthenticationManager authenticationManager;

    public LoginView(AuthenticationManager authenticationManager,
                     QRLoginService qrLoginService) {

        this.authenticationManager = authenticationManager;

        setSizeFull();
        addClassName("login-page");

        // --- Login form ---
        LoginForm loginForm = new LoginForm();
        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);

        // Card-like wrapper
        VerticalLayout loginCard = new VerticalLayout();
        loginCard.addClassName("login-card");
        loginCard.setPadding(true);
        loginCard.setSpacing(false);
        loginCard.setAlignItems(Alignment.CENTER);
        loginCard.add(new H2("Welcome"), loginForm);

        // --- OTP button ---
        Button otpLoginButton = new BervanButton(
                "Login via OTP",
                event -> openOtpDialog(),
                BervanButtonStyle.WARNING
        );

        // --- QR login block (hidden on mobile) ---
        QRLoginView qrLoginView = new QRLoginView(qrLoginService, null);
        VerticalLayout rightPanel = new VerticalLayout(qrLoginView, otpLoginButton);
        rightPanel.addClassName("login-qr-panel");
        rightPanel.setPadding(false);
        rightPanel.setSpacing(true);
        rightPanel.setAlignItems(Alignment.CENTER);

        // --- Page layout ---
        // Use VerticalLayout wrapper for responsive stacking on mobile
        VerticalLayout content = new VerticalLayout();
        content.setSizeUndefined();
        content.setSpacing(true);
        content.setAlignItems(Alignment.CENTER);
        content.addClassName("login-content");
        content.add(loginCard, rightPanel);

        add(content);
    }

    private void openOtpDialog() {
        Dialog otpDialog = new Dialog();
        otpDialog.setHeaderTitle("Enter OTP Code");

        VerticalLayout dialogLayout = new VerticalLayout();
        H3 instruction = new H3("Please enter the " + CODE_LENGTH + "-digit OTP code:");
        TextField otpField = new TextField("OTP Code");
        otpField.setMaxLength(CODE_LENGTH);
        otpField.setWidthFull();

        otpField.addValueChangeListener(event -> {
            String otpCode = event.getValue();
            if (otpCode.length() == CODE_LENGTH) {
                try {
                    loginViaOTP(otpCode);
                    otpDialog.close();
                } catch (Exception e) {
                    log.error("Could not login via OTP!", e);
                    showErrorNotification("Incorrect code!");
                    otpField.clear();
                }
            }
        });

        dialogLayout.add(instruction, otpField);
        otpDialog.add(dialogLayout);

        Button cancelButton = new Button("Cancel", e -> otpDialog.close());
        otpDialog.getFooter().add(cancelButton);

        otpDialog.open();
    }

    private void loginViaOTP(String otpCode) {
        OtpAuthenticationToken authReq = new OtpAuthenticationToken(
                otpCode,
                null,
                otpCode
        );

        Authentication authResult = authenticationManager.authenticate(authReq);
        SecurityContextHolder.getContext().setAuthentication(authResult);
        VaadinSession.getCurrent().getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );

        if (AuthService.getUserRole().equals("ROLE_STREAMING")) {
            getUI().ifPresent(ui -> ui.navigate("/streaming-platform"));
        } else {
            getUI().ifPresent(ui -> ui.navigate("/generate-otp"));
        }
    }
}