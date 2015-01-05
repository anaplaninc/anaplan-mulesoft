
package org.mule.modules.anaplan.connector.connectivity;

import javax.annotation.Generated;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.devkit.ProcessInterceptor;
import org.mule.api.devkit.ProcessTemplate;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.routing.filter.Filter;
import org.mule.modules.anaplan.connector.adapters.AnaplanConnectorConnectionIdentifierAdapter;
import org.mule.modules.anaplan.connector.connection.ConnectionManager;
import org.mule.modules.anaplan.connector.process.AnaplanConnectorManagedConnectionProcessInterceptor;
import org.mule.security.oauth.callback.ProcessCallback;
import org.mule.security.oauth.process.ProcessCallbackProcessInterceptor;
import org.mule.security.oauth.process.RetryProcessInterceptor;

@Generated(value = "Mule DevKit Version 3.5.2", date = "2015-01-05T02:40:12-08:00", comments = "Build UNNAMED.2039.0541b23")
public class AnaplanConnectorManagedConnectionProcessTemplate<P >implements ProcessTemplate<P, AnaplanConnectorConnectionIdentifierAdapter>
{

    private final ProcessInterceptor<P, AnaplanConnectorConnectionIdentifierAdapter> processInterceptor;

    public AnaplanConnectorManagedConnectionProcessTemplate(ConnectionManager<AnaplanConnectorConnectionKey, AnaplanConnectorConnectionIdentifierAdapter> connectionManager, MuleContext muleContext) {
        ProcessInterceptor<P, AnaplanConnectorConnectionIdentifierAdapter> processCallbackProcessInterceptor = new ProcessCallbackProcessInterceptor<P, AnaplanConnectorConnectionIdentifierAdapter>();
        ProcessInterceptor<P, AnaplanConnectorConnectionIdentifierAdapter> managedConnectionProcessInterceptor = new AnaplanConnectorManagedConnectionProcessInterceptor<P>(processCallbackProcessInterceptor, connectionManager, muleContext);
        ProcessInterceptor<P, AnaplanConnectorConnectionIdentifierAdapter> retryProcessInterceptor = new RetryProcessInterceptor<P, AnaplanConnectorConnectionIdentifierAdapter>(managedConnectionProcessInterceptor, muleContext, connectionManager.getRetryPolicyTemplate());
        processInterceptor = retryProcessInterceptor;
    }

    public P execute(ProcessCallback<P, AnaplanConnectorConnectionIdentifierAdapter> processCallback, MessageProcessor messageProcessor, MuleEvent event)
        throws Exception
    {
        return processInterceptor.execute(processCallback, null, messageProcessor, event);
    }

    public P execute(ProcessCallback<P, AnaplanConnectorConnectionIdentifierAdapter> processCallback, Filter filter, MuleMessage message)
        throws Exception
    {
        return processInterceptor.execute(processCallback, null, filter, message);
    }

}
