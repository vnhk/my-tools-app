package com.bervan.toolsapp.views;

import com.bervan.common.user.User;
import com.bervan.common.view.AbstractPageView;
import com.bervan.logging.JsonLogger;
import com.bervan.toolsapp.security.QRLoginService;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.io.ByteArrayInputStream;
import java.util.Timer;
import java.util.TimerTask;

@Deprecated
public class QRLoginView extends AbstractPageView {
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "my-tools-app");

    private final QRLoginService qrLoginService;
    private final Dialog dialog;
    private VerticalLayout qrLayout;
    private Timer pollTimer;
    private String currentSessionId;

    public QRLoginView(QRLoginService qrLoginService, Dialog dialog) {
        this.dialog = dialog;
        this.qrLoginService = qrLoginService;
        this.currentSessionId = VaadinSession.getCurrent().getSession().getId();

        addClassName("qr-login-view");
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        initializeView();
    }

    private void initializeView() {
        H2 title = new H2("QR Code Login");
        Paragraph instruction = new Paragraph("Scan the QR code with your phone and enter the number shown below:");

        qrLayout = new VerticalLayout();
        qrLayout.setAlignItems(Alignment.CENTER);

        Button generateButton = new Button("Generate New QR Code", e -> generateQRCode());
        generateButton.addClassName("primary");

        Div buttonLayout = new Div(generateButton);
        buttonLayout.addClassName("button-layout");

        add(title, instruction, qrLayout, buttonLayout);

        // Generate initial QR code
        generateQRCode();
    }

    private void generateQRCode() {
        try {
            // Stop previous polling
            stopPolling();

            QRLoginService.QRLoginData qrData = qrLoginService.generateQRLogin();
            String url = qrLoginService.buildFullUrl(qrData.getUuid());
            byte[] qrCodeBytes = qrLoginService.generateQRCode(url);

            StreamResource resource = new StreamResource("qr-code.png",
                    () -> new ByteArrayInputStream(qrCodeBytes));

            Image qrImage = new Image(resource, "QR Code");
            qrImage.setWidth("200px");
            qrImage.setHeight("200px");

            Paragraph numberLabel = new Paragraph(String.format("Number: %02d", qrData.getNumber()));
            numberLabel.getStyle().set("font-size", "24px");
            numberLabel.getStyle().set("font-weight", "bold");
            numberLabel.getStyle().set("color", "var(--lumo-primary-color)");

            Paragraph statusLabel = new Paragraph("Waiting for authentication...");
            statusLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");

            qrLayout.removeAll();
            qrLayout.add(qrImage, new Text(url), numberLabel);

            // Start polling for authentication
            startPolling();

        } catch (Exception e) {
            showErrorNotification("Error generating QR code: " + e.getMessage());
            log.error("Error generating QR code!", e);
        }
    }

    private void startPolling() {
        pollTimer = new Timer();
        pollTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getUI().ifPresent(ui -> ui.access(() -> {
                    try {
                        checkAuthenticationStatus(ui);
                        ui.push(); // Force UI update
                    } catch (Exception e) {
                        log.error("Error checking authentication status", e);
                    }
                }));
            }
        }, 1000, 1000); // Check every second
    }

    private void stopPolling() {
        if (pollTimer != null) {
            pollTimer.cancel();
            pollTimer = null;
        }
    }

    private void checkAuthenticationStatus(UI ui) {
        QRLoginService.QRSessionWaiting waitingSession = qrLoginService.getWaitingSession(currentSessionId);

        if (waitingSession != null && waitingSession.isCompleted()) {
            stopPolling();

            User authenticatedUser = waitingSession.getAuthenticatedUser();
            if (authenticatedUser != null) {
                // Close dialog first if exists
                if (dialog != null) {
                    dialog.close();
                }

                // Small delay to ensure dialog closes before navigation
                Timer delayTimer = new Timer();
                delayTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ui.access(() -> {
                            performLogin(authenticatedUser, ui);
                            qrLoginService.cleanupSession(currentSessionId);
                            ui.push();
                        });
                    }
                }, 100); // 100ms delay
            }
        }
    }


    private void performLogin(User user, UI ui) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);
        VaadinSession.getCurrent().getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );

        showPrimaryNotification("Login successful!");

        // Redirect based on user role
        if (user.getRole().equals("ROLE_STREAMING")) {
            ui.navigate("/streaming-platform");
        } else {
            ui.navigate("/generate-otp");
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        stopPolling();
        qrLoginService.cleanupSession(currentSessionId);
    }
}