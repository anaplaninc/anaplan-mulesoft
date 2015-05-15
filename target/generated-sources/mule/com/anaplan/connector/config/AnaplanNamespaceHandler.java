
package com.anaplan.connector.config;

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
@Generated(value = "Mule DevKit Version 3.6.0", date = "2015-04-20T09:44:38-07:00", comments = "Build UNNAMED.2363.ef5c8a7")
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
            this.registerBeanDefinitionParser("basic-auth-connection", new AnaplanConnectorBasicAuthConnectionStrategyConfigDefinitionParser());
        } catch (NoClassDefFoundError ex) {
            handleException("basic-auth-connection", "@Config", ex);
        }
        try {
            this.registerBeanDefinitionParser("cert-auth-connection", new AnaplanConnectorCertAuthConnectionStrategyConfigDefinitionParser());
        } catch (NoClassDefFoundError ex) {
            handleException("cert-auth-connection", "@Config", ex);
        }
        try {
            this.registerBeanDefinitionParser("import-to-model", new ImportToModelDefinitionParser());
        } catch (NoClassDefFoundError ex) {
            handleException("import-to-model", "@Processor", ex);
        }
        try {
            this.registerBeanDefinitionParser("export-from-model", new ExportFromModelDefinitionParser());
        } catch (NoClassDefFoundError ex) {
            handleException("export-from-model", "@Processor", ex);
        }
        try {
            this.registerBeanDefinitionParser("delete-from-model", new DeleteFromModelDefinitionParser());
        } catch (NoClassDefFoundError ex) {
            handleException("delete-from-model", "@Processor", ex);
        }
        try {
            this.registerBeanDefinitionParser("run-process", new RunProcessDefinitionParser());
        } catch (NoClassDefFoundError ex) {
            handleException("run-process", "@Processor", ex);
        }
    }

}
