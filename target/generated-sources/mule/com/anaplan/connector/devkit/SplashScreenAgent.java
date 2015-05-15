
package com.anaplan.connector.devkit;

import javax.annotation.Generated;


/**
 * Marks DevKit {@link org.mule.api.agent.Agent} implementations to take care of logging information at Mule app level mainly for troubleshooting purposes.
 * 
 */
@Generated(value = "Mule DevKit Version 3.6.0", date = "2015-04-20T09:44:38-07:00", comments = "Build UNNAMED.2363.ef5c8a7")
public interface SplashScreenAgent {

      /**
     * Print version information for all the modules used by the application
     */
    void splash();

    /**
     * Retrieve the count of modules used by the application
     *
     * @return
     */
    int getExtensionsCount();
}
