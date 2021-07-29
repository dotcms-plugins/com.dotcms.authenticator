package com.dotcms.osgi.authenticator;

import com.dotmarketing.loggers.Log4jUtil;
import com.dotmarketing.osgi.GenericBundleActivator;
import com.dotmarketing.util.Config;
import com.liferay.portal.util.PropsUtil;
import com.liferay.util.InstancePool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;

public class Activator extends GenericBundleActivator {

    private LoggerContext pluginLoggerContext;

    @Override
    public void start ( BundleContext bundleContext ) throws Exception {

        //Initializing log4j...
        LoggerContext dotcmsLoggerContext = Log4jUtil.getLoggerContext();
        //Initialing the log4j context of this plugin based on the dotCMS logger context
        pluginLoggerContext = (LoggerContext) LogManager
                .getContext(this.getClass().getClassLoader(),
                        false,
                        dotcmsLoggerContext,
                        dotcmsLoggerContext.getConfigLocation());

        //Initializing services...
        initializeServices( bundleContext );

        // Overrides the Configuration to use the Custom Authenticator
        final String authenticatorClassName = DummyAuthenticator.class.getName();
        final List<String> authenticationClasses = new ArrayList<>();
        authenticationClasses.add(authenticatorClassName);
        Config.setProperty(PropsUtil.AUTH_PIPELINE_PRE, authenticationClasses);
        // Finally Add the instance to the Pool
        InstancePool.put(authenticatorClassName, new DummyAuthenticator());
    }

    public void stop(BundleContext context) throws Exception {

        //Unregister all the bundle services
        unregisterServices(context);

        //Shutting down log4j in order to avoid memory leaks
        Log4jUtil.shutdown(pluginLoggerContext);
    }

}
