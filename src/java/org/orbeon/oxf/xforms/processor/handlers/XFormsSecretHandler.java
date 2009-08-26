/**
 * Copyright (C) 2009 Orbeon, Inc.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * The full text of the license is available at http://www.gnu.org/copyleft/lesser.html
 */
package org.orbeon.oxf.xforms.processor.handlers;

import org.orbeon.oxf.xforms.control.XFormsSingleNodeControl;
import org.orbeon.oxf.xforms.control.controls.XFormsSecretControl;
import org.orbeon.oxf.xml.ContentHandlerHelper;
import org.orbeon.oxf.xml.XMLConstants;
import org.orbeon.oxf.xml.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Handle xforms:secret.
 */
public class XFormsSecretHandler extends XFormsControlLifecyleHandler {

    private static final String HIDDEN_PASSWORD = "********";

    public XFormsSecretHandler() {
        super(false);
    }

    protected void handleControlStart(String uri, String localname, String qName, Attributes attributes, String staticId, String effectiveId, XFormsSingleNodeControl xformsControl) throws SAXException {

        final XFormsSecretControl secretControl = (XFormsSecretControl) xformsControl;
        final ContentHandler contentHandler = handlerContext.getController().getOutput();

        final AttributesImpl containerAttributes = getContainerAttributes(uri, localname, attributes, effectiveId, secretControl);

        // Create xhtml:input
        {
            final String xhtmlPrefix = handlerContext.findXHTMLPrefix();
            if (!isStaticReadonly(secretControl)) {
                final String inputQName = XMLUtils.buildQName(xhtmlPrefix, "input");
                containerAttributes.addAttribute("", "type", "type", ContentHandlerHelper.CDATA, "password");
                containerAttributes.addAttribute("", "name", "name", ContentHandlerHelper.CDATA, effectiveId);
                containerAttributes.addAttribute("", "value", "value", ContentHandlerHelper.CDATA,
                        handlerContext.isTemplate() || secretControl == null || secretControl.getExternalValue(pipelineContext) == null ? "" : secretControl.getExternalValue(pipelineContext));

                // Handle accessibility attributes on <input>
                handleAccessibilityAttributes(attributes, containerAttributes);

                contentHandler.startElement(XMLConstants.XHTML_NAMESPACE_URI, "input", inputQName, containerAttributes);
                contentHandler.endElement(XMLConstants.XHTML_NAMESPACE_URI, "input", inputQName);
            } else {
                final String spanQName = XMLUtils.buildQName(xhtmlPrefix, "span");
                contentHandler.startElement(XMLConstants.XHTML_NAMESPACE_URI, "span", spanQName, containerAttributes);
                final String value = secretControl.getValue(pipelineContext);
                // TODO: Make sure that Ajax response doesn't send the value back
                if (value != null && value.length() > 0)
                    contentHandler.characters(HIDDEN_PASSWORD.toCharArray(), 0, HIDDEN_PASSWORD.length());
                contentHandler.endElement(XMLConstants.XHTML_NAMESPACE_URI, "span", spanQName);
            }
        }
    }

    private AttributesImpl getContainerAttributes(String uri, String localname, Attributes attributes, String effectiveId, XFormsSecretControl secretControl) {
        final AttributesImpl containerAttributes;
        if (handlerContext.isNewXHTMLLayout()) {
            reusableAttributes.clear();
            containerAttributes = reusableAttributes;
            containerAttributes.addAttribute("", "id", "id", ContentHandlerHelper.CDATA, getLHHACId(effectiveId, LLHAC.CONTROL));
        } else {
            final StringBuilder classes = getInitialClasses(uri, localname, attributes, secretControl);
            handleMIPClasses(classes, getPrefixedId(), secretControl);
            containerAttributes = getAttributes(attributes, classes.toString(), effectiveId);
            handleReadOnlyAttribute(containerAttributes, containingDocument, secretControl);

            if (secretControl != null) {
                // Output extension attributes in no namespace
                secretControl.addExtensionAttributes(containerAttributes, "");
            }
        }
        return containerAttributes;
    }
}
