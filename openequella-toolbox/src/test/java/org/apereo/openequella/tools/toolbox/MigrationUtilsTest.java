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

package org.apereo.openequella.tools.toolbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apereo.openequella.tools.toolbox.utils.MigrationUtils;
import org.junit.Test;
import org.xml.sax.SAXException;

public class MigrationUtilsTest {
	@Test 
	public void testSomeLibraryMethod() {
		try {
			
			String xml = "<xml><item version=\"1\"><rating/><badurls/><history/><moderation/><test1>qwefasdfwefasd</test1></item><metadata><keywords>these,are,keywords</keywords><keywords>these,are,other,keywords</keywords></metadata></xml>";
			assertEquals("these,are,keywords", MigrationUtils.findFirstOccurrenceInXml(xml, "/xml/metadata/keywords/text()"));
			
			
//			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//			Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
//			
//			NodeList nList = doc.getElementsByTagName("test1");
//			int length = nList.getLength();
//			for(int i = 0; i < length; i++) {
//				System.out.println("i="+ i + ".  Number of nodes=" + nList.getLength());
//				if(i == 0) {
//					nList.item(0).setTextContent("NIFTY!");
//				} else {
//					System.out.println("Removing Test1: " + nList.item(1).getTextContent());
//					nList.item(1).getParentNode().removeChild(nList.item(1));
//				}
//			}
//			Transformer transformer = TransformerFactory.newInstance().newTransformer();
//			StringWriter result = new StringWriter();
//		    transformer.transform(new DOMSource(doc), new StreamResult(result));
//		    String newXml = result.toString();
			//System.out.println("Reswizzling complete." + ml);
		} catch (SAXException e) {
			fail(e.getMessage());e.printStackTrace();
		} catch (IOException e) {
			fail(e.getMessage());
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			fail(e.getMessage());e.printStackTrace();
		} catch (XPathExpressionException e) {
			fail(e.getMessage());e.printStackTrace();
		}
    }
}
