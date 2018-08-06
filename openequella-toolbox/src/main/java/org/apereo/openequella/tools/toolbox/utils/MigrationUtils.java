/*
 * Copyright 2018 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apereo.openequella.tools.toolbox.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.kaltura.client.types.MediaEntry;

public class MigrationUtils {
	private static Logger LOGGER = LogManager.getLogger(MigrationUtils.class);

	public static JSONObject convertKalturaToEquellaAttachment(MediaEntry kalturaSource, String eqKalturaId) {
		JSONObject eqAtt = new JSONObject();
		eqAtt.put("uuid", UUID.randomUUID().toString());
		eqAtt.put("type", "kaltura");
		eqAtt.put("description", kalturaSource.getDescription());
		eqAtt.put("preview", false);
		eqAtt.put("restricted", false);
		eqAtt.put("mediaId", kalturaSource.getId());
		eqAtt.put("title", kalturaSource.getName());
		eqAtt.put("uploadedDate", kalturaSource.getCreatedAt()); // TODO this might not be correct
		eqAtt.put("thumbUrl", kalturaSource.getThumbnailUrl());
		eqAtt.put("kalturaServer", eqKalturaId);
		eqAtt.put("tags", kalturaSource.getTags());
		eqAtt.put("duration", kalturaSource.getDuration());
		return eqAtt;
	}

	// Replaces the value of the first xml node matching nodeName with newValue,
	// and then removes the rest of the xml nodes matching nodeName.
	// Returns the modified XML as a String.
	public static String cullAndReplaceXmlNodes(String xml, String nodeName, String newValue) throws Exception {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));

			NodeList nList = doc.getElementsByTagName(nodeName);
			int length = nList.getLength();
			for (int i = 0; i < length; i++) {
				if (i == 0) {
					nList.item(0).setTextContent(newValue);
				} else {
					nList.item(1).getParentNode().removeChild(nList.item(1));
				}
			}
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StringWriter result = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(result));
			String newXml = result.toString();
			return newXml;
		} catch (Exception e) {
			throw new Exception("Unable to cull and replace the xml.  node names=[" + nodeName + "], new value=[" + newValue + "] - " + e.getMessage());
		}
	}
	
	// Returns the value of the first xml node matching the xpath,
	public static String findFirstOccurrenceInXml(String xml, String xpathStr) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile(xpathStr);
		String result = (String) expr.evaluate(doc, XPathConstants.STRING); 
		LOGGER.trace("findFirstOccurrenceInXml(): result=[{}], xpath=[{}], xml=[{}]", result, xpathStr, xml);
		return result;
	}
}
