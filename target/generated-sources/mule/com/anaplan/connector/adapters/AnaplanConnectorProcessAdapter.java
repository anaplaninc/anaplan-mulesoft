
package com.anaplan.connector.adapters;

import javax.annotation.Generated;
import com.anaplan.connector.AnaplanConnector;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.devkit.ProcessAdapter;
import org.mule.api.devkit.ProcessTemplate;
import org.mule.api.devkit.ProcessTemplate;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.routing.filter.Filter;
import org.mule.security.oauth.callback.ProcessCallback;


/**
 * A <code>AnaplanConnectorProcessAdapter</code> is a wrapper around {@link AnaplanConnector } that enables custom processing strategies.
 * 
 */
@Generated(value = "Mule DevKit Version 3.6.0", date = "2015-04-20T09:44:38-07:00", comments = "Build UNNAMED.2363.ef5c8a7")
public class AnaplanConnectorProcessAdapter
    extends AnaplanConnectorLifecycleAdapter
    implements ProcessAdapter<AnaplanConnectorCapabilitiesAdapter>
{


    public<P >ProcessTemplate<P, AnaplanConnectorCapabilitiesAdapter> getProcessTemplate() {
        final AnaplanConnectorCapabilitiesAdapter object = this;
        return new ProcessTemplate<P,AnaplanConnectorCapabilitiesAdapter>() {


            @Override
            public P execute(ProcessCallback<P, AnaplanConnectorCapabilitiesAdapter> processCallback, MessageProcessor messageProcessor, MuleEvent event)
                throws Exception
            {
                return processCallback.process(object);
            }

            @Override
            public P execute(ProcessCallback<P, AnaplanConnectorCapabilitiesAdapter> processCallback, Filter filter, MuleMessage message)
                throws Exception
            {
                return processCallback.process(object);
            }

        }
        ;
    }

}
