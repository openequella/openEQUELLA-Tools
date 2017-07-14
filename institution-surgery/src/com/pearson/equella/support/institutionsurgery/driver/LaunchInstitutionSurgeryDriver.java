package com.pearson.equella.support.institutionsurgery.driver;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LaunchInstitutionSurgeryDriver {
	private static final Logger logger = LogManager.getLogger(LaunchInstitutionSurgeryDriver.class);

	private static HashMap<String, CollectionBundle> collections = new HashMap<String, CollectionBundle>();

	public static void main(final String[] args) throws Exception {
		logger.info(String.format("### Checking configs from institution-surgery.properties..."));
		if (!Config.initConfig()) {
			logger.info("### Config check failed.  Exiting...");
			System.exit(0);
		}
		logger.info("### Config check passed.");
		if (Config.getSurgeryType().equals("TEST_NPE_WORKFLOW_ON_ITEMS")) {
			logger.info(String.format("### Performing TEST_NPE_WORKFLOW_ON_ITEMS surgery on [%s].",
					Config.getUnzippedInstitution()));
			handleTestNpeWorkflowOnItems();
		} else {
			logger.info(String.format("### Unknown surgery.type of [%s].", Config.getSurgeryType()));
		}
	}

	private static void handleTestNpeWorkflowOnItems() {
		// Display all collections and their workflows
		handleTestNpeWorkflowOnItemsFindCollectionsAndAssociatedWorkflows();
		handleTestNpeWorkflowOnItemsFindItemsAndAssociatedWorkflowStatus();
	}

	private static void handleTestNpeWorkflowOnItemsFindCollectionsAndAssociatedWorkflows() {
		// Display all collections and their workflows
		File inst = new File(Config.getUnzippedInstitution() + "/itemdefinition");
		Iterator<File> it = FileUtils.iterateFilesAndDirs(inst, new SuffixFileFilter(".xml"), TrueFileFilter.INSTANCE);
		while (it.hasNext()) {
			File f = it.next();
			if (f.isFile()) {
				CollectionBundle cb;
				try {
					cb = handleTestNpeWorkflowOnItemsRetrieveCollectionsAndAssociatedWorkflows(f);
					collections.put(cb.getUuid(), cb);
					logger.info(String.format("Collection found: %s", cb.toString()));
				} catch (Exception e) {
					logger.info(String.format("Unable to fully inflate the collection at: %s -%s", f.getAbsolutePath(),
							e.getMessage()));
					e.printStackTrace();
				}
			}
		}
	}

	private static void handleTestNpeWorkflowOnItemsFindItemsAndAssociatedWorkflowStatus() {
		int issues = 0;
		File inst = new File(Config.getUnzippedInstitution() + "/items");
		Iterator<File> it = FileUtils.iterateFilesAndDirs(inst, new SuffixFileFilter(".xml"), TrueFileFilter.INSTANCE);
		while (it.hasNext()) {
			File f = it.next();
			if (f.isFile() && !f.getName().equals("item.xml") && !f.getName().equals("taskhistory.xml")) {
				ItemBundle ib;
				try {
					ib = handleTestNpeWorkflowOnItemsRetrieveItemAndWorkflowStatus(f);
					// Check collection / workflow mapping.
					CollectionBundle itemDef = collections.get(ib.getCollectionUuid());
					if ((ib.getAssociatedWorkflow() != null)
							&& (!ib.getAssociatedWorkflow().equals("NO_MODERATION_STATUS"))
							&& !itemDef.getWorkflowUuid().equals(ib.getAssociatedWorkflow())) {
						issues++;
						logger.warn(String.format("Inconsistent item found: [%s]!=[%s] - %s - %s - %s",
								itemDef.getWorkflowUuid(), ib.getAssociatedWorkflow(), ib, itemDef,
								f.getAbsolutePath()));
					}
					logger.info(String.format("Item found: %s", ib.toString()));
				} catch (Exception e) {
					logger.warn(String.format("Unable to fully inflate the item at: %s -%s", f.getAbsolutePath(),
							e.getMessage()));
					e.printStackTrace();
				}
			}
		}
		logger.warn(String.format("Number of inconsistent items found: %d", issues));
	}

	/**
	 * 
	 * @param file
	 *            item definition xml
	 * @return
	 * @throws Exception
	 */
	private static CollectionBundle handleTestNpeWorkflowOnItemsRetrieveCollectionsAndAssociatedWorkflows(File file)
			throws Exception {
		CollectionBundle cb = new CollectionBundle();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		Node docElem = doc.getDocumentElement();
		if (!docElem.getNodeName().equals("com.tle.beans.entity.itemdef.ItemDefinition"))
			throw new Exception(String.format("Parent XML node name is not expected:  %s", docElem.getNodeName()));
		Element uuidNode = findSubNode("uuid", docElem);
		cb.setUuid(uuidNode.getTextContent());
		try {
			Element workflowNode = findSubNode("workflow", docElem);
			if (!workflowNode.getAttribute("entityclass").equals("com.tle.common.workflow.Workflow"))
				throw new Exception(String.format("Unexpected workflow.entityclass value of [%s]",
						workflowNode.getAttribute("entityclass")));
			cb.setWorkflowUuid(workflowNode.getAttribute("uuid"));
		} catch (Exception e) {
			if (e.getMessage().equals("Could not find node workflow")) {
				// No workflow
				cb.setWorkflowUuid("NO_WORKFLOW");
			} else {
				throw e;
			}

		}
		String[] path = { "name", "strings", "entry", "com.tle.beans.entity.LanguageString", "text" };
		Element textNode = findSubNodeRecursively(path, docElem);
		cb.setName(textNode.getTextContent());

		return cb;
	}

	/**
	 * 
	 * @param file
	 *            item definition xml
	 * @return
	 * @throws Exception
	 */
	private static ItemBundle handleTestNpeWorkflowOnItemsRetrieveItemAndWorkflowStatus(File file) throws Exception {
		ItemBundle ib = new ItemBundle();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		Node docElem = doc.getDocumentElement();
		if (!docElem.getNodeName().equals("com.tle.beans.item.Item"))
			throw new Exception(String.format("Parent XML node name is not expected:  %s", docElem.getNodeName()));
		Element uuidNode = findSubNode("uuid", docElem);
		ib.setUuid(uuidNode.getTextContent());

		Element versionNode = findSubNode("version", docElem);
		ib.setVersion(versionNode.getTextContent());

		Element itemDefNode = findSubNode("itemDefinition", docElem);
		ib.setCollectionUuid(itemDefNode.getAttribute("uuid"));

		try {
			String[] path = { "moderation", "statuses" };
			Element modStatusesNode = findSubNodeRecursively(path, docElem);
			NodeList list = modStatusesNode.getChildNodes();
			String workflowUuid = null;
			for (int i = 0; i < list.getLength(); i++) {
				Node subnode = list.item(i);
				if ((subnode.getNodeType() == Node.ELEMENT_NODE)
						&& subnode.getNodeName().equals("com.tle.common.workflow.WorkflowNodeStatus")) {
					// Check if there's a 'node' element:
					try {
						Element nodeNode = findSubNode("node", subnode);
						String temp = nodeNode.getAttribute("workflow");
						if (workflowUuid == null) {
							workflowUuid = temp;
						} else if (workflowUuid.equals(temp)) {
							// Good - as expected.
						} else {
							throw new Exception(String.format(
									"Found a discrepancy in the workflow uuids for item moderation statuses: prev=[%s] current=[%s] file=[%s]",
									workflowUuid, temp, file.getAbsolutePath()));
						}
					} catch (Exception e) {
						if (!e.getMessage().equals("Could not find node node") && !e.getMessage().equals(
								"Node [com.tle.common.workflow.WorkflowNodeStatus] has no children to search")) {
							throw e;
						}
						// Otherwise, don't worry about the node.
					}
				}
			}
			ib.setAssociatedWorkflow(workflowUuid);
		} catch (Exception e) {
			if (e.getMessage().equals("Could not find node moderation")
					|| e.getMessage().equals("Could not find node statuses")
					|| e.getMessage().equals("Node [statuses] has no children to search")) {
				// No workflow
				ib.setAssociatedWorkflow("NO_MODERATION_STATUS");
			} else {
				throw e;
			}

		}
		try {
			String[] path = { "name", "strings", "entry", "com.tle.beans.entity.LanguageString", "text" };
			Element textNode = findSubNodeRecursively(path, docElem);
			ib.setName(textNode.getTextContent());
		} catch (Exception e) {
			// Don't worry about it.
			ib.setName("NO_NAME");
		}

		return ib;
	}

	public static Element findSubNodeRecursively(String[] hierarchy, Node n) throws Exception {
		Node newN = n;
		for (int i = 0; i < hierarchy.length; i++) {
			newN = findSubNode(hierarchy[i], newN);
		}
		if (newN.equals(n))
			throw new Exception("String array must have at least one element in it.");
		return (Element) newN;
	}

	public static Element findSubNode(String name, Node node) throws Exception {
		if (node.getNodeType() != Node.ELEMENT_NODE)
			throw new Exception("Node is not an ELEMENT_NODE");
		if (!node.hasChildNodes())
			throw new Exception(String.format("Node [%s] has no children to search", node.getNodeName()));

		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node subnode = list.item(i);
			if (subnode.getNodeType() == Node.ELEMENT_NODE) {
				if (subnode.getNodeName().equals(name))
					return (Element) subnode;
			}
		}
		throw new Exception(String.format("Could not find node %s", name));
	}
}
