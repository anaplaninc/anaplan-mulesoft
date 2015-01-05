
package org.mule.modules.anaplan.connector.processors;

import java.lang.reflect.Type;
import javax.annotation.Generated;


/**
 * Interface used to unify all message processors (those which use (or not) pagination) from the ManagedConnectionProcessInterceptor
 * 
 */
@Generated(value = "Mule DevKit Version 3.5.2", date = "2015-01-05T02:40:12-08:00", comments = "Build UNNAMED.2039.0541b23")
public interface ConnectivityProcessor {


    public Object getUsername();

    public Object getPassword();

    public Object getUrl();

    public Object getWorkspaceId();

    public Object getModelId();

    public Object getProxyHost();

    public Object getProxyUser();

    public Object getProxyPass();

    /**
     * Retrieves the concrete java.lang.reflect.Type of a connectivity argument, needed for the @Connect
     * 
     * @param fieldName
     *     Name of the field to look for
     * @return
     *     The {@link java.lang.reflect.Type} associated with the field {@code fieldName}
     * @throws NoSuchFieldException
     *     Thrown when the {@code fieldName} is not a field from the current class
     */
    public Type typeFor(String fieldName)
        throws NoSuchFieldException
    ;

}
