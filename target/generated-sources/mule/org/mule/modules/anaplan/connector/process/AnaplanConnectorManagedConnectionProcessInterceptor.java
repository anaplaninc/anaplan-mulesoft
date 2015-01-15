
package org.mule.modules.anaplan.connector.process;

import java.util.List;
import javax.annotation.Generated;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.devkit.ProcessInterceptor;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.routing.filter.Filter;
import org.mule.devkit.processor.ExpressionEvaluatorSupport;
import org.mule.modules.anaplan.connector.adapters.AnaplanConnectorConnectionIdentifierAdapter;
import org.mule.modules.anaplan.connector.connection.ConnectionManager;
import org.mule.modules.anaplan.connector.connection.UnableToAcquireConnectionException;
import org.mule.modules.anaplan.connector.connection.UnableToReleaseConnectionException;
import org.mule.modules.anaplan.connector.connectivity.AnaplanConnectorConnectionKey;
import org.mule.modules.anaplan.connector.processors.ConnectivityProcessor;
import org.mule.security.oauth.callback.ProcessCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Generated(value = "Mule DevKit Version 3.5.2", date = "2015-01-14T04:21:07-08:00", comments = "Build UNNAMED.2039.0541b23")
public class AnaplanConnectorManagedConnectionProcessInterceptor<T >
    extends ExpressionEvaluatorSupport
    implements ProcessInterceptor<T, AnaplanConnectorConnectionIdentifierAdapter>
{

    private static Logger logger = LoggerFactory.getLogger(AnaplanConnectorManagedConnectionProcessInterceptor.class);
    private final ConnectionManager<AnaplanConnectorConnectionKey, AnaplanConnectorConnectionIdentifierAdapter> connectionManager;
    private final MuleContext muleContext;
    private final ProcessInterceptor<T, AnaplanConnectorConnectionIdentifierAdapter> next;

    public AnaplanConnectorManagedConnectionProcessInterceptor(ProcessInterceptor<T, AnaplanConnectorConnectionIdentifierAdapter> next, ConnectionManager<AnaplanConnectorConnectionKey, AnaplanConnectorConnectionIdentifierAdapter> connectionManager, MuleContext muleContext) {
        this.next = next;
        this.connectionManager = connectionManager;
        this.muleContext = muleContext;
    }

    @Override
    public T execute(ProcessCallback<T, AnaplanConnectorConnectionIdentifierAdapter> processCallback, AnaplanConnectorConnectionIdentifierAdapter object, MessageProcessor messageProcessor, MuleEvent event)
        throws Exception
    {
        AnaplanConnectorConnectionIdentifierAdapter connection = null;
        AnaplanConnectorConnectionKey key = null;
        if (hasConnectionKeysOverride(messageProcessor)) {
            ConnectivityProcessor connectivityProcessor = ((ConnectivityProcessor) messageProcessor);
            final String _transformedUsername = ((String) evaluateAndTransform(muleContext, event, connectivityProcessor.typeFor("_usernameType"), null, connectivityProcessor.getUsername()));
            if (_transformedUsername == null) {
                throw new UnableToAcquireConnectionException("Parameter username in method connect can't be null because is not @Optional");
            }
            final String _transformedPassword = ((String) evaluateAndTransform(muleContext, event, connectivityProcessor.typeFor("_passwordType"), null, connectivityProcessor.getPassword()));
            if (_transformedPassword == null) {
                throw new UnableToAcquireConnectionException("Parameter password in method connect can't be null because is not @Optional");
            }
            final String _transformedUrl = ((String) evaluateAndTransform(muleContext, event, connectivityProcessor.typeFor("_urlType"), null, connectivityProcessor.getUrl()));
            final String _transformedProxyHost = ((String) evaluateAndTransform(muleContext, event, connectivityProcessor.typeFor("_proxyHostType"), null, connectivityProcessor.getProxyHost()));
            final String _transformedProxyUser = ((String) evaluateAndTransform(muleContext, event, connectivityProcessor.typeFor("_proxyUserType"), null, connectivityProcessor.getProxyUser()));
            final String _transformedProxyPass = ((String) evaluateAndTransform(muleContext, event, connectivityProcessor.typeFor("_proxyPassType"), null, connectivityProcessor.getProxyPass()));
            key = new AnaplanConnectorConnectionKey(_transformedUsername, _transformedPassword, _transformedUrl, _transformedProxyHost, _transformedProxyUser, _transformedProxyPass);
        } else {
            key = connectionManager.getEvaluatedConnectionKey(event);
        }
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(("Attempting to acquire connection using "+ key.toString()));
            }
            connection = connectionManager.acquireConnection(key);
            if (connection == null) {
                throw new UnableToAcquireConnectionException();
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug((("Connection has been acquired with [id="+ connection.getConnectionIdentifier())+"]"));
                }
            }
            return next.execute(processCallback, connection, messageProcessor, event);
        } catch (Exception e) {
            if (processCallback.getManagedExceptions()!= null) {
                for (Class exceptionClass: ((List<Class<? extends Exception>> ) processCallback.getManagedExceptions())) {
                    if (exceptionClass.isInstance(e)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug((((("An exception ( "+ exceptionClass.getName())+") has been thrown. Destroying the connection with [id=")+ connection.getConnectionIdentifier())+"]"));
                        }
                        try {
                            if (connection!= null) {
                                connectionManager.destroyConnection(key, connection);
                                connection = null;
                            }
                        } catch (Exception innerException) {
                            logger.error(innerException.getMessage(), innerException);
                        }
                    }
                }
            }
            throw e;
        } finally {
            try {
                if (connection!= null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug((("Releasing the connection back into the pool [id="+ connection.getConnectionIdentifier())+"]"));
                    }
                    connectionManager.releaseConnection(key, connection);
                }
            } catch (Exception e) {
                throw new UnableToReleaseConnectionException(e);
            }
        }
    }

    /**
     * Validates that the current message processor has changed any of its connection parameters at processor level. If so, a new AnaplanConnectorConnectionKey must be generated
     * 
     * @param messageProcessor
     *     the message processor to test against the keys
     * @return
     *     true if any of the parameters in @Connect method annotated with @ConnectionKey was override in the XML, false otherwise  
     */
    private Boolean hasConnectionKeysOverride(MessageProcessor messageProcessor) {
        if ((messageProcessor == null)||(!(messageProcessor instanceof ConnectivityProcessor))) {
            return false;
        }
        ConnectivityProcessor connectivityProcessor = ((ConnectivityProcessor) messageProcessor);
        if (connectivityProcessor.getUsername()!= null) {
            return true;
        }
        return false;
    }

    public T execute(ProcessCallback<T, AnaplanConnectorConnectionIdentifierAdapter> processCallback, AnaplanConnectorConnectionIdentifierAdapter object, Filter filter, MuleMessage message)
        throws Exception
    {
        throw new UnsupportedOperationException();
    }

}
