package com.bervan.toolsapp.views.otpview;

import com.bervan.common.MenuNavigationComponent;
import com.bervan.common.service.AuthService;
import com.bervan.common.view.AbstractPageView;
import com.bervan.logging.JsonLogger;
import com.bervan.toolsapp.security.OTPService;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.UUID;

import static com.bervan.toolsapp.views.otpview.OTPGenerateView.ROUTE_NAME;

@Route(value = ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
@Deprecated
public class OTPGenerateView extends AbstractPageView {
    public static final String ROUTE_NAME = "generate-otp";
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "my-tools-app");
    private final OTPService otpService;
    private final H3 otpLabel;

    public OTPGenerateView(OTPService otpService) {
        this.otpService = otpService;
        this.otpLabel = new H3("Click the button to generate OTP");

        addClassName("otp-generate-view");
        setSpacing(true);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        //add dropdown (default STREAMING) STREAMING, READ, WRITE, that will generate code
        //and role and in authprovider we set role STREAMING_ONLY that means we can only access streaming app
        //it has to be done by method secr

        //generate otp as button visible in all views (like bucket) and code determines also url and after login
        //user is navigated there

        Button generateOtpButton = new Button("Generate OTP", event -> generateAndDisplayOTP());
        generateOtpButton.addClassName("option-button");
        add(new MenuNavigationComponent("") {
        }, otpLabel, generateOtpButton);
    }

    private void generateAndDisplayOTP() {
        try {
            UUID userId = AuthService.getLoggedUserId();
            String role = "ROLE_STREAMING";
            String otpCode = otpService.generateOTP(userId, role);
            otpLabel.setText("Your OTP code is: " + otpCode);
        } catch (Exception e) {
            log.error("Failed to generate OTP", e);
            showErrorNotification("Could not generate OTP token!");
        }

    }
}