package ch.gianlucafrei.nellygateway.config.configuration;

import ch.gianlucafrei.nellygateway.config.ErrorValidation;
import ch.gianlucafrei.nellygateway.services.login.drivers.InvalidProviderSettingsException;
import ch.gianlucafrei.nellygateway.services.login.drivers.LoginDriver;
import ch.gianlucafrei.nellygateway.services.login.drivers.LoginDriverLoader;
import lombok.*;
import org.springframework.context.ApplicationContext;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginProvider implements ErrorValidation {

    private String type;
    private LoginProviderSettings with;

    @Override
    public List<String> getErrors(ApplicationContext context) {

        var errors = new ArrayList<String>();

        if (type == null)
            errors.add("LoginProvider: No type defined");
        if (with == null)
            errors.add("LoginProvider: No settings defined");

        if (!errors.isEmpty())
            return errors;

        // Check if we can load the driver
        LoginDriverLoader loader = context.getBean(LoginDriverLoader.class);
        try {
            LoginDriver loginDriver = loader.loadDriverByKey(type, URI.create("/callback"), with);
        } catch (InvalidProviderSettingsException e) {
            var settingErrors = e.getSettingErrors();
            errors.addAll(settingErrors);
        } catch (Exception e) {
            errors.add("Could not load driver implementation for type '" + type + "'");
        }

        return errors;
    }
}
