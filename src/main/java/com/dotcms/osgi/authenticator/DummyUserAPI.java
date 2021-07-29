package com.dotcms.osgi.authenticator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// this one will be the thirdparty user resolver
public class DummyUserAPI {

    private static final String EMAIL = "dotcms.dummy@dotcms.com";
    public Optional<Map<String, String>> findUserByEmail (final String emailAddress) {

        if (EMAIL.equals(emailAddress)) {

            final Map<String, String> userMap = new HashMap<>();
            userMap.put("userId", "dummy.12345");
            userMap.put("password", "dotcms123");
            userMap.put("emailAddress", EMAIL);
            userMap.put("lastName", "Dummy User");
            userMap.put("firstName", "Dot Dummy User");
            userMap.put("roles", "DOTCMS_BACK_END_USER,CMS Administrator");
            return Optional.ofNullable(userMap);
        }

        return Optional.empty(); // not found
    }
}
