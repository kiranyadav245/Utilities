package com.utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XMLServiceUtility {
	public static void main(String[] args) throws Exception {
		XMLServiceUtility serviceUtility = new XMLServiceUtility();
		Scanner scanner = new Scanner(new File("src/request/request.xml"));
		//file to String
		String xml = scanner
				.useDelimiter("\\Z").next();
		//string to org.w3c.dom.Document
		Document document = serviceUtility.convertStringToDocument(xml);

		// xpath to find in org.w3c.dom.Document object
		String elementName = "ns0225:ExternalKey_Ext";
//		String xPath = "/soapenv:Envelope/soapenv:Body/hig:createOrUpdateDraftSubmission[0]/hig:draftSubmissionRequestMessage/req:DraftSubmissionRequestTransaction/ns0282:PolicyPeriod/ns0271:Account/ns0255:AccountContacts/ns0255:Entry/ns0224:Contact/ns0225:ExternalKey_Ext";
		String xPath="Envelope/Header[2]/authentication/password";
		System.out.println(serviceUtility.getElementByXPath(document, xPath));
		document = serviceUtility.setElementByXPath(document, xPath, "kiran");
		
		String documentToString2 = serviceUtility.convertDocumentToString(document);
		System.out.println(documentToString2);
		
//		getting element value from org.w3c.dom.Document using element id
		String nodeValueByElementName = serviceUtility.getNodeValueByElementName(document, elementName);
		System.out.println(nodeValueByElementName);
//		setting element value in org.w3c.dom.Document using element id
		document = serviceUtility.setNodeValueByElementName(document, elementName, "Kiran");
//		converting org.w3c.dom.Document to String 
		String documentToString = serviceUtility.convertDocumentToString(document);
		System.out.println(documentToString);
//		writing updated org.w3c.dom.Document to file
		File f = serviceUtility.convertStringToFile(documentToString);
		System.out.println(f);

	}
	
	public String getElementByXPath(Document document,
			String path) throws Exception {
		XPath xPath =  XPathFactory.newInstance().newXPath();
		String value = xPath.compile(path).evaluate(document);
		return value;
		
	}
	
	public Document setElementByXPath(Document document,
			String path,String newContent) throws Exception {
		XPath xPath =  XPathFactory.newInstance().newXPath();
		Node n = (Node) xPath.compile(path).evaluate(document, XPathConstants.NODE);
		n.setTextContent(newContent);
		return document;
		
	}
	

	public File convertStringToFile(String documentToString) {
		File fnew = new File("src/request/request1.xml");
		System.out.println(documentToString);

		try {
			FileWriter f2 = new FileWriter(fnew, false);
			f2.write(documentToString);
			f2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fnew;
	}

	public Document setNodeValueByElementName(Document document,
			String elementName, String value) {
		Node elementByTag = getElementByTagName(document, elementName);
		if (elementByTag != null) {
			elementByTag.setTextContent(value);
		}
		return document;
	}

	public String getNodeValueByElementName(Document document,
			String elementName) {
		Node elementByTag = getElementByTagName(document, elementName);
		if (elementByTag != null) {
			return elementByTag.getTextContent();
		}
		return null;
	}
	
	public String getElementValue(org.w3c.dom.Node node) {
		return node.getTextContent();
	}
	
	
	//temporary method - not working
	public org.w3c.dom.Node getElementByXPaths(Document document,
			String xPath) {
		if(xPath != null){
			String[] xPaths = xPath.split("/");
			Node node = null;
			for(int index=0; index < xPaths.length;index++){
				String tagName = getTagNameOnly(xPaths[index]);
				Integer position = null;
				if(tagName != null){
					position = getIndexPosition(xPaths[index]);
				}
				if(index == 0)
					node = getElementByTagName(document , tagName);
				else {
					node = getElementByTagName(node, tagName, position);
				}
				if(index == xPaths.length-1){
					return node;
				}
				
				
			}
		}
		return null;
	}
	
	private String getTagNameOnly(String tag) {
		if(tag != null && tag.contains("[")){
			return tag.substring(0, tag.indexOf("["));
		}
		return tag;
	}

	private Integer getIndexPosition(String tag) {
		if(tag != null && tag.contains("[")){
			String position = tag.substring(tag.indexOf("[")+1, tag.indexOf("]"));
			try{
				return Integer.parseInt(position);
			}catch(Exception e){
				System.out.println("Invalid XPath");
			}
		}
		return null;
	}

	public org.w3c.dom.Node getElementByTagName(Document document,
			String elementName) {
		NodeList nodeList = document.getElementsByTagName("*");
		for (int i = 0; i < nodeList.getLength(); i++) {
			org.w3c.dom.Node node = nodeList.item(i);
			System.out.println(node.getNodeName());
			if (elementName.equalsIgnoreCase(node.getNodeName())) {
				return node;
			}
		}
		return null;
	}
	
	public org.w3c.dom.Node getElementByTagName(Node node,
			String elementName, Integer index) {
		NodeList nodeList = node.getChildNodes();
		int count = -1;
		for (int i = 0; i < nodeList.getLength(); i++) {
			org.w3c.dom.Node childNode = nodeList.item(i);
			if(!"#text".equals(childNode.getNodeName()))
				count++;
			else
				continue;
			System.out.println(childNode.getNodeName());
			if (elementName.equalsIgnoreCase(childNode.getNodeName())) {
				if(index == null)
					return node;
				if(index != null && index == count)
					return node;
			}
		}
		return null;
	}

	public String convertDocumentToString(Document doc) {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = tf.newTransformer();
			// below code to remove XML declaration
			// transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
			// "yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			String output = writer.getBuffer().toString();
			return output;
		} catch (TransformerException e) {
			e.printStackTrace();
		}

		return null;
	}

	public Document convertStringToDocument(String xmlStr) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(
					xmlStr)));
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
