
package org.mule.modules.anaplan.connector.config;

import javax.annotation.Generated;
import org.mule.config.MuleManifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;


/**
 * Registers bean definitions parsers for handling elements in <code>http://www.mulesoft.org/schema/mule/anaplan</code>.
 * 
 */
@Generated(value = "Mule DevKit Version 3.5.2", date = "2015-01-08T02:50:03-08:00", comments = "Build UNNAMED.2039.0541b23")
public class AnaplanNamespaceHandler
    extends NamespaceHandlerSupport
{

    private static Logger logger = LoggerFactory.getLogger(AnaplanNamespaceHandler.class);

    private void handleException(String beanName, String beanScope, NoClassDefFoundError noClassDefFoundError) {
        String muleVersion = "";
        try {
            muleVersion = MuleManifest.getProductVersion();
        } catch (Exception _x) {
            logger.error("Problem while reading mule version");
        }
        logger.error(((((("Cannot launch the mule app, the  "+ beanScope)+" [")+ beanName)+"] within the connector [anaplan] is not supported in mule ")+ muleVersion));
        throw new FatalBeanException(((((("Cannot launch the mule app, the  "+ beanScope)+" [")+ beanName)+"] within the connector [anaplan] is not supported in mule ")+ muleVersion), noClassDefFoundError);
    }

    /**
     * Invoked by the {@link DefaultBeanDefinitionDocumentReader} after construction but before any custom elements are parsed. 
     * @see NamespaceHandlerSupport#registerBeanDefinitionParser(String, BeanDefinitionParser)
     * 
     */
    public void init() {
        try {
            this.registerBeanDefinitionParser("config", new AnaplanConnectorConfigDefinitionParser());
        } catch (NoClassDefFoundError ex) {
            handleException("config", "@Config", ex);
        }
        try {
            this.registerBeanDefinitionParser("create", new CreateDefinitionParser());
        } catch (NoClassDefFoundError ex) {
            handleException("create", "@Processor", ex);
        }
        try {
            this.registerBeanDefinitionParser("read", new ReadDefinitionParser());
        } catch (NoClassDefFoundError ex) {
            handleException("read", "@Processor", ex);
        }
        try {
            this.registerBeanDefinitionParser("update", new UpdateDefinitionParser());
        } catch (NoClassDefFoundError ex) {
            handleException("update", "@Processor", ex);
        }
        try {
            this.registerBeanDefinitionParser("delete", new DeleteDefinitionParser());
        } catch (NoClassDefFoundError ex) {
            handleException("delete", "@Processor", ex);
        }
        try {
            this.registerBeanDefinitionParser("my-processor", new MyProcessorDefinitionParser());
        } catch (NoClassDefFoundError ex) {
            handleException("my-processor", "@Processor", ex);
        }
    }

}
