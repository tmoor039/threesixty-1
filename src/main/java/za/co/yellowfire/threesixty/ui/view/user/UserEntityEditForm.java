package za.co.yellowfire.threesixty.ui.view.user;

import com.github.markash.ui.component.button.ButtonBuilder;
import com.github.markash.ui.component.notification.NotificationBuilder;
import com.github.markash.ui.security.CurrentUserProvider;
import com.github.markash.ui.view.AbstractEntityEditForm;
import com.vaadin.data.StatusChangeEvent;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;
import za.co.yellowfire.threesixty.domain.organization.Identity;
import za.co.yellowfire.threesixty.domain.user.*;
import za.co.yellowfire.threesixty.ui.I8n;
import za.co.yellowfire.threesixty.ui.component.ByteArrayStreamResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("serial")
public class UserEntityEditForm extends AbstractEntityEditForm<String, User> {

	private List<User> reportsTo = new ArrayList<>();
	private ListDataProvider<User> reportsToProvider = new ListDataProvider<>(reportsTo);
	private Image pictureField = new Image(null, new ThemeResource("img/profile-pic-300px.jpg"));
	private Window pictureWindow = new Window(I8n.Profile.PICTURE, new PictureSelectionForm(this::onSelectedPicture));
    private boolean pictureChanged = false;

    private final CurrentUserProvider<User> currentUserProvider;
    private final UserService userService;

	UserEntityEditForm(
            final UserService userService,
            final CurrentUserProvider<User> currentUserProvider) {

		super(User.class, String.class);

		this.userService = userService;
		this.currentUserProvider = currentUserProvider;

		this.getIdField().setCaption("User Name");
        this.getIdField().setWidth(100, Unit.PERCENTAGE);
//        this.getIdField().setReadOnly(false);
//        this.getIdField().setRequiredIndicatorVisible(true);

        ComboBox<String> salutationField = new ComboBox<>("Salutation", this.userService.findSalutations());
        salutationField.setWidth(100, Unit.PERCENTAGE);

        ComboBox<String> genderField = new ComboBox<>("Gender", this.userService.findGenders());
        genderField.setWidth(100, Unit.PERCENTAGE);

        ComboBox<Country> countryField = new ComboBox<>("Country", this.userService.findCountries());
        countryField.setWidth(100, Unit.PERCENTAGE);

        ComboBox<Role> roleField = new ComboBox<>("Role", this.userService.findRoles());
        roleField.setWidth(100, Unit.PERCENTAGE);

        ComboBox<User> reportsToField = new ComboBox<>("Reports To");
		reportsToField.setDataProvider(this.reportsToProvider);
        reportsToField.setWidth(100, Unit.PERCENTAGE);

        ComboBox<Position>positionField = new ComboBox<>("Position", this.userService.findPositions());
        positionField.setWidth(100, Unit.PERCENTAGE);

        ComboBox<Identity> departmentField = new ComboBox<>("Department", this.userService.findDepartments());
        departmentField.setWidth(100, Unit.PERCENTAGE);

        TextField firstNameField = new TextField("Name");
        firstNameField.setWidth(100, Unit.PERCENTAGE);

        TextField lastNameField = new TextField("Last Name");
        lastNameField.setWidth(100, Unit.PERCENTAGE);

        TextField emailField = new TextField("Email");
        emailField.setWidth(100, Unit.PERCENTAGE);

        TextField phoneField = new TextField("Phone");
        phoneField.setWidth(100, Unit.PERCENTAGE);

        TextField websiteField = new TextField("Website");
        websiteField.setWidth(100, Unit.PERCENTAGE);

        RichTextArea bioField = new RichTextArea("Bio");
        bioField.setWidth(100, Unit.PERCENTAGE);

        this.pictureField.setStyleName("profile-image");

        Button pictureButton = ButtonBuilder.CHANGE(this::onChangePicture);

        getBinder().forField(firstNameField).asRequired(I8n.User.Validation.FIRST_NAME_REQUIRED).bind(User.FIELD_FIRST_NAME);
        getBinder().forField(lastNameField).asRequired(I8n.User.Validation.LAST_NAME_REQUIRED).bind(User.FIELD_LAST_NAME);
        getBinder().forField(salutationField).bind(User.FIELD_TITLE);
        getBinder().forField(genderField).bind(User.FIELD_GENDER);
        getBinder().forField(emailField).bind(User.FIELD_EMAIL);
        getBinder().forField(phoneField).bind(User.FIELD_PHONE);
        getBinder().forField(websiteField).bind(User.FIELD_WEBSITE);
        getBinder().forField(bioField).bind(User.FIELD_BIO);
        getBinder().forField(departmentField).bind(User.FIELD_DEPARTMENT);
        getBinder().forField(positionField).bind(User.FIELD_POSITION);
        getBinder().forField(reportsToField).bind(User.FIELD_REPORTS_TO);
        getBinder().forField(countryField).bind(User.FIELD_LOCATION);
        getBinder().forField(roleField).bind(User.FIELD_ROLE);

        firstNameField.setRequiredIndicatorVisible(true);
        lastNameField.setRequiredIndicatorVisible(true);

        MHorizontalLayout picture =
                new MHorizontalLayout()
                        .withMargin(false)
                        .withFullWidth()
                        .withComponents(
                                new MVerticalLayout(pictureField, pictureButton).withMargin(false),
                                new MVerticalLayout()
                                        .withMargin(false)
                                        .with(
                                                getIdField(),
                                                firstNameField,
                                                lastNameField,
                                                new MHorizontalLayout()
                                                        .withMargin(false)
                                                        .with(salutationField, genderField)
                                        )
                        );

        MVerticalLayout left =
                new MVerticalLayout()
                        .withMargin(false)
                        .with(
                                picture,
                                departmentField,
                                positionField,
                                new MHorizontalLayout(roleField, reportsToField).withMargin(false)
                        );

        MVerticalLayout right =
                new MVerticalLayout()
                        .withMargin(false)
                        .with(
                                emailField,
                                phoneField,
                                websiteField,
                                countryField,
                                bioField
                        );

        HorizontalLayout details =
                new MHorizontalLayout()
                        .withMargin(false)
                        .withFullSize()
                        .with(
                                left,
                                right
                        );

        addComponent(details);
	}

    @Override
    public boolean isModified() {
        return super.isModified() || this.pictureChanged;
    }

    /**
     * Provide a hook for subclasses to update dependant fields
     */
    @Override
    protected void updateDependentFields() {

        updateReportsTo();
        updatePicture();
    }

    @SuppressWarnings("unused")
    private void onChangePicture(
            Button.ClickEvent event) {

	    UI.getCurrent().addWindow(pictureWindow);
    }


    void resetPassword() {

	    Optional<User> principal = this.currentUserProvider.get();
        if (principal.isPresent()) {

            String currentUserId = principal.get().getId();
            this.userService.resetPassword(getValue(), currentUserId);
            if (getValue().getId().equalsIgnoreCase(currentUserId)) {
                NotificationBuilder.showNotification(
                        "Password reset",
                        "Your password has been reset and can be changed on next logon.",
                        5000);
            } else {
                NotificationBuilder.showNotification(
                        "Password reset",
                        "User's password has been reset and can be changed on next logon.",
                        5000);
            }
        } else {
            NotificationBuilder.showNotification(
                    "Unable to reset password",
                    "Unable to determine the current user session.",
                    5000);
        }
    }

    private void onSelectedPicture(
            final PictureSelectionForm.FileEvent event) {

	    try {
            if (getValue() != null) {
                getValue().setPicture(event.getFile());
                updatePicture();

                this.pictureChanged = true;
                getEventRouter().fireEvent(new StatusChangeEvent(getBinder(), false));

            }
        } catch (IOException e) {
            Notification.show("Error changing profile picture", e.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    private void updateReportsTo() {

        this.reportsTo.clear();
        this.currentUserProvider
                .get()
                .map(userService::findUsersExcept)
                .ifPresent(reportsTo::addAll);
        this.reportsToProvider.refreshAll();
    }

    private void updatePicture() {

        if (getValue().hasPicture()) {
            this.pictureField.setSource(new ByteArrayStreamResource(getValue().getPictureContent(), getValue().getPictureName()));
        }
    }
}
