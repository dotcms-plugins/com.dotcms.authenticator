package com.dotcms.osgi.authenticator;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.NoSuchUserException;
import com.dotmarketing.business.Role;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.util.Logger;
import com.liferay.portal.auth.AuthException;
import com.liferay.portal.auth.Authenticator;
import com.liferay.portal.model.User;
import com.liferay.util.StringPool;
import io.vavr.control.Try;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class DummyAuthenticator implements Authenticator {

    private final DummyUserAPI dummyUserAPI = new DummyUserAPI();

    @Override
    public int authenticateByEmailAddress(final String companyId, final String emailAddress,
                                          final String password) throws AuthException {

        try {

            Logger.info(this, "DummyAuthenticator: authenticateByEmailAddress = " + emailAddress );
            final Optional<Map<String, String>> myUserOpt = dummyUserAPI.findUserByEmail(emailAddress);

            // if the user exists on the external API and the password match
            if (myUserOpt.isPresent() && myUserOpt.get().get("password").equals(password)) {

                if (null == Try.of(()->APILocator.getUserAPI()
                        .loadByUserByEmail(emailAddress, APILocator.systemUser(), false)).getOrNull()) {

                    saveUser(emailAddress, myUserOpt); // if the user does not exists, so create it
                }

                return SUCCESS;
            }
        } catch (DotSecurityException | DotDataException e) {

            Logger.info(this, "Could not authenticate the user: " + emailAddress);
        }

        return FAILURE;
    }

    private void saveUser(final String emailAddress, final Optional<Map<String, String>> myUserOpt)
            throws DotDataException, DotSecurityException {

        final User dotUser = new User();
        dotUser.setUserId(myUserOpt.get().get("userId"));
        dotUser.setPassword(myUserOpt.get().get("password"));
        dotUser.setEmailAddress(myUserOpt.get().get("emailAddress"));
        dotUser.setLastName(myUserOpt.get().get("lastName"));
        dotUser.setFirstName(myUserOpt.get().get("firstName"));
        dotUser.setCompanyId(APILocator.getCompanyAPI().getDefaultCompany().getCompanyId());
        dotUser.setActive(true);
        dotUser.setCreateDate(new Date());

        Logger.info(this, "Creating the user: " + emailAddress);
        APILocator.getUserAPI().save(dotUser, APILocator.systemUser(), false);
        // safe roles
        Stream.of(myUserOpt.get().get("roles").split(StringPool.COMMA))
                .forEach(role -> Try.run(()->this.addRole(dotUser, role.trim()))
                        .onFailure(e -> Logger.error(this, "Error adding role: " + role, e)));
    }

    private void addRole(final User user, final String roleKey)
            throws DotDataException {

        final Role role = APILocator.getRoleAPI().loadRoleByKey(roleKey);

        if (null != role) {

            if (!APILocator.getRoleAPI().doesUserHaveRole(user, role)) {

                APILocator.getRoleAPI().addRoleToUser(role, user);
                Logger.debug(this, "Role named '" + role.getName() + "' has been added to user: " + user.getEmailAddress());
            } else {

                Logger.debug(this,
                        "User '" + user.getEmailAddress() + "' already has the role '" + role + "'. Skipping assignment...");
            }
        } else {

            Logger.debug(this, "Role named '" + roleKey + "' does NOT exists in dotCMS. Ignoring it...");
        }
    }

    @Override
    public int authenticateByUserId(final String companyId, final String userId, final String password) throws AuthException {

        // todo: similar to the email but by id
        Logger.info(this, "DummyAuthenticator: authenticateByUserId = " + userId );
        return "dummy.123".equals(userId) && "dotcms123".equals(password)?SUCCESS:FAILURE;
    }
}
