# Anaplan Connector for Mulesoft Release Notes
30-APRIL-2015

Release Notes for Version 2 of the Anaplan Connector for Mulesoft. These Release Notes accompany the [Anaplan Connector for MuleSoft Guide](https://github.com/anaplaninc/anaplan-mulesoft/raw/develop_3.6/doc/AnaplanConnectorforMuleSoft-Guide.pdf)

## Compatibility
The Anaplan Connector for Mulesoft is compatible with:
- Mule Version 3.5 and higher
- Anypoint Studio 5 or higher

## Features
For building a flow that performs any of the following:
- Export from Anaplan
- Import into Anaplan
- Execute Action (a delete)
- Process (multiple model-to-model Import actions, or multiple Delete actions)

## Fixed in this Release
Added support for Process, which was not present in Version 1.

## Known Issues
A Process has the following limitations: 
- Multiple imports must be model-to-model rather than from a source or file that is external to Anaplan. 
- Multiple exports are not supported.
The workaround for these limitations is to use Anaplan Connect.

## Support Resources
- [Anaplan REST API for Integration and User Administration](https://community.anaplan.com/anapedia/integrations/data-integration/anaplan-api-guide)

- [Anaplan REST API for Integration and User Administration](http://docs.anaplan.apiary.io/#)

- We welcome enhancement requests and bug reports at [https://github.com/anaplaninc/anaplan-mulesoft/issues](https://github.com/anaplaninc/anaplan-mulesoft/issues).