package top.wdcc.freeswitch.common.utils;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlUtils {
    /**
     * 获取XML节点下的元素
     * @param parent
     * @param tagName
     * @return
     */
    public static List<Element> getElementsByTag(Element parent, String tagName){
        return parent.elements(tagName);
    }

    /**
     * 获取XML节点值
     * @param parent
     * @param tag
     * @return
     */
    public static String getXmlValue(Element parent, String tag){
        return parent.element(tag).getText();
    }

    /**
     * 通过路径获取XML节点值
     * @param parent
     * @param path
     * @return
     */
    public static String getValueByXmlPath(Element parent, String path){
        return getElementByXmlPath(parent,path).getText();
    }

    public static Element getElementByXmlPath(Element parent, String path){
        try{
            if(!path.startsWith("/")){
                path = path + "/";
            }
            String[] pathList = path.split("/");
            if(parent == null || path == null || pathList == null || pathList.length <= 0){
                return null;
            }

            Element element = null;
            for (String each: pathList){
                if (element != null) {
                    element = element.element(each);
                }else {
                    element = parent.element(each);
                }
            }
            return element;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static List<Element> getElementsByXmlPath(Element parent, String path){
        try{
            if(!path.startsWith("/")){
                path = path + "/";
            }
            String[] pathList = path.split("/");
            if(parent == null || path == null || pathList == null || pathList.length <= 0){
                return null;
            }

            Element element = null;
            for (String each: pathList){
                if (element != null) {
                    element = element.element(each);
                }else {
                    element = parent.element(each);
                }
            }
            return element.elements();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, String> xml2Map(Document xml, String path) {
        Map<String, String> map = new HashMap<>();
        try {
            Element rootElement = xml.getRootElement();
            for (Element e : getElementsByXmlPath(rootElement, path)) {
                map.put(e.getName(), URLDecoder.decode(e.getText(), "UTF-8"));
            }
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return map;
    }

    public static String getXmlValueByPath(String xml, String path) {
        Document document = null;
        try {
            document = DocumentHelper.parseText(xml);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        Element rootElement = document.getRootElement();
        return getElementByXmlPath(rootElement, path).getText();
    }

    public static Document parse(String xml) {
        Document document = null;
        try {
            document = DocumentHelper.parseText(xml);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return document;
    }


}
