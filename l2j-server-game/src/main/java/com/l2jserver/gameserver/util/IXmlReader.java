package com.l2jserver.gameserver.util;

import com.l2jserver.gameserver.util.file.filter.XMLFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.l2jserver.gameserver.config.Configuration.server;

public interface IXmlReader {

  Logger LOG = LogManager.getLogger(IXmlReader.class);

  String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

  String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

  /** The default file filter, ".xml" files only. */
  XMLFilter XML_FILTER = new XMLFilter();

  /**
   * This method can be used to load/reload the data.<br>
   * It's highly recommended to clear the data storage, either the list or map.
   */
  void load();

  /**
   * Wrapper for {@link #parseFile(File)} method.
   *
   * @param path the relative path to the datapack root of the XML file to parse.
   */
  default void parseDatapackFile(String path) {
    parseFile(new File(server().getDatapackRoot(), path));
  }

  /**
   * Parses a single XML file.<br>
   * If the file was successfully parsed, call {@link #parseDocument(Document, File)} for the parsed
   * document.<br>
   * <b>Validation is enforced.</b>
   *
   * @param f the XML file to parse.
   */
  default void parseFile(File f) {
    if (!getCurrentFileFilter().accept(f)) {
      LOG.warn(
          "{}: Could not parse {} is not a file or it doesn't exist!",
          getClass().getSimpleName(),
          f.getName());
      return;
    }

    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    dbf.setValidating(true);
    dbf.setIgnoringComments(true);
    try {
      dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
      final DocumentBuilder db = dbf.newDocumentBuilder();
      db.setErrorHandler(new XMLErrorHandler());
      parseDocument(db.parse(f), f);
    } catch (SAXParseException e) {
      LOG.warn(
          "{}: Could not parse file {} at line {}, column {}",
          getClass().getSimpleName(),
          f.getName(),
          e.getLineNumber(),
          e.getColumnNumber(),
          e);
    } catch (Exception e) {
      LOG.warn("{}: Could not parse file {}", getClass().getSimpleName(), f.getName(), e);
    }
  }

  default boolean parseDirectory(Path path) {
    return parseDirectory(path, false);
  }

  default boolean parseDirectory(Path strDir, boolean recursive) {
    if (!Files.exists(strDir)) {
      LOG.warn("Folder {} doesn't exist!", strDir);
      return false;
    }

    final File[] files = strDir.toFile().listFiles();
    if (files != null) {
      for (File f : files) {
        if (recursive && f.isDirectory()) {
          parseDirectory(Path.of(f.getAbsolutePath()), recursive);
        } else if (getCurrentFileFilter().accept(f)) {
          parseFile(f);
        }
      }
    }
    return true;
  }

  default boolean parseDatapackDirectory(String path, boolean recursive) {
    return parseDirectory(Path.of(path), recursive);
  }

  default boolean parseDatapackDirectory(String path) {
    return parseDirectory(Path.of(path), false);
  }

  /**
   * Abstract method that when implemented will parse the current document.<br>
   * Is expected to be call from {@link #parseFile(File)}.
   *
   * @param doc the current document to parse
   * @param f the current file
   */
  default void parseDocument(Document doc, File f) {
    parseDocument(doc);
  }

  /**
   * Abstract method that when implemented will parse the current document.<br>
   * Is expected to be call from {@link #parseFile(File)}.
   *
   * @param doc the current document to parse
   */
  default void parseDocument(Document doc) {
    LOG.error("{}: Parser not implemented!", getClass().getSimpleName());
  }

  /**
   * Parses a boolean value.
   *
   * @param node the node to parse
   * @param defaultValue the default value
   * @return if the node is not null, the value of the parsed node, otherwise the default value
   */
  default Boolean parseBoolean(Node node, Boolean defaultValue) {
    return node != null ? Boolean.valueOf(node.getNodeValue()) : defaultValue;
  }

  /**
   * Parses a boolean value.
   *
   * @param node the node to parse
   * @return if the node is not null, the value of the parsed node, otherwise null
   */
  default Boolean parseBoolean(Node node) {
    return parseBoolean(node, null);
  }

  /**
   * Parses a boolean value.
   *
   * @param attrs the attributes
   * @param name the name of the attribute to parse
   * @return if the node is not null, the value of the parsed node, otherwise null
   */
  default Boolean parseBoolean(NamedNodeMap attrs, String name) {
    return parseBoolean(attrs.getNamedItem(name));
  }

  /**
   * Parses a boolean value.
   *
   * @param attrs the attributes
   * @param name the name of the attribute to parse
   * @param defaultValue the default value
   * @return if the node is not null, the value of the parsed node, otherwise the default value
   */
  default Boolean parseBoolean(NamedNodeMap attrs, String name, Boolean defaultValue) {
    return parseBoolean(attrs.getNamedItem(name), defaultValue);
  }

  /**
   * Parses a byte value.
   *
   * @param node the node to parse
   * @param defaultValue the default value
   * @return if the node is not null, the value of the parsed node, otherwise the default value
   */
  default Byte parseByte(Node node, Byte defaultValue) {
    return node != null ? Byte.valueOf(node.getNodeValue()) : defaultValue;
  }

  /**
   * Parses a byte value.
   *
   * @param node the node to parse
   * @return if the node is not null, the value of the parsed node, otherwise null
   */
  default Byte parseByte(Node node) {
    return parseByte(node, null);
  }

  /**
   * Parses a byte value.
   *
   * @param attrs the attributes
   * @param name the name of the attribute to parse
   * @return if the node is not null, the value of the parsed node, otherwise null
   */
  default Byte parseByte(NamedNodeMap attrs, String name) {
    return parseByte(attrs.getNamedItem(name));
  }

  /**
   * Parses a byte value.
   *
   * @param attrs the attributes
   * @param name the name of the attribute to parse
   * @param defaultValue the default value
   * @return if the node is not null, the value of the parsed node, otherwise the default value
   */
  default Byte parseByte(NamedNodeMap attrs, String name, Byte defaultValue) {
    return parseByte(attrs.getNamedItem(name), defaultValue);
  }

  /**
   * Parses a short value.
   *
   * @param node the node to parse
   * @param defaultValue the default value
   * @return if the node is not null, the value of the parsed node, otherwise the default value
   */
  default Short parseShort(Node node, Short defaultValue) {
    return node != null ? Short.valueOf(node.getNodeValue()) : defaultValue;
  }

  /**
   * Parses a short value.
   *
   * @param node the node to parse
   * @return if the node is not null, the value of the parsed node, otherwise null
   */
  default Short parseShort(Node node) {
    return parseShort(node, null);
  }

  /**
   * Parses a short value.
   *
   * @param attrs the attributes
   * @param name the name of the attribute to parse
   * @return if the node is not null, the value of the parsed node, otherwise null
   */
  default Short parseShort(NamedNodeMap attrs, String name) {
    return parseShort(attrs.getNamedItem(name));
  }

  /**
   * Parses a short value.
   *
   * @param attrs the attributes
   * @param name the name of the attribute to parse
   * @param defaultValue the default value
   * @return if the node is not null, the value of the parsed node, otherwise the default value
   */
  default Short parseShort(NamedNodeMap attrs, String name, Short defaultValue) {
    return parseShort(attrs.getNamedItem(name), defaultValue);
  }

  /**
   * Parses an int value.
   *
   * @param node the node to parse
   * @param defaultValue the default value
   * @return if the node is not null, the value of the parsed node, otherwise the default value
   */
  default int parseInt(Node node, Integer defaultValue) {
    return node != null ? Integer.parseInt(node.getNodeValue()) : defaultValue;
  }

  /**
   * Parses an int value.
   *
   * @param node the node to parse
   * @return if the node is not null, the value of the parsed node, otherwise the default value
   */
  default int parseInt(Node node) {
    return parseInt(node, -1);
  }

  /**
   * Parses an integer value.
   *
   * @param node the node to parse
   * @param defaultValue the default value
   * @return if the node is not null, the value of the parsed node, otherwise the default value
   */
  default Integer parseInteger(Node node, Integer defaultValue) {
    return node != null ? Integer.valueOf(node.getNodeValue()) : defaultValue;
  }

  /**
   * Parses an integer value.
   *
   * @param node the node to parse
   * @return if the node is not null, the value of the parsed node, otherwise null
   */
  default Integer parseInteger(Node node) {
    return parseInteger(node, null);
  }

  /**
   * Parses an integer value.
   *
   * @param attrs the attributes
   * @param name the name of the attribute to parse
   * @return if the node is not null, the value of the parsed node, otherwise null
   */
  default Integer parseInteger(NamedNodeMap attrs, String name) {
    return parseInteger(attrs.getNamedItem(name));
  }

  /**
   * Parses an integer value.
   *
   * @param attrs the attributes
   * @param name the name of the attribute to parse
   * @param defaultValue the default value
   * @return if the node is not null, the value of the parsed node, otherwise the default value
   */
  default Integer parseInteger(NamedNodeMap attrs, String name, Integer defaultValue) {
    return parseInteger(attrs.getNamedItem(name), defaultValue);
  }

  /**
   * Parses a long value.
   *
   * @param node the node to parse
   * @param defaultValue the default value
   * @return if the node is not null, the value of the parsed node, otherwise the default value
   */
  default Long parseLong(Node node, Long defaultValue) {
    return node != null ? Long.valueOf(node.getNodeValue()) : defaultValue;
  }

  /**
   * Parses a long value.
   *
   * @param node the node to parse
   * @return if the node is not null, the value of the parsed node, otherwise null
   */
  default Long parseLong(Node node) {
    return parseLong(node, null);
  }

  /**
   * Parses a long value.
   *
   * @param attrs the attributes
   * @param name the name of the attribute to parse
   * @return if the node is not null, the value of the parsed node, otherwise null
   */
  default Long parseLong(NamedNodeMap attrs, String name) {
    return parseLong(attrs.getNamedItem(name));
  }

  /**
   * Parses a long value.
   *
   * @param attrs the attributes
   * @param name the name of the attribute to parse
   * @param defaultValue the default value
   * @return if the node is not null, the value of the parsed node, otherwise the default value
   */
  default Long parseLong(NamedNodeMap attrs, String name, Long defaultValue) {
    return parseLong(attrs.getNamedItem(name), defaultValue);
  }

  /**
   * Parses a float value.
   *
   * @param node the node to parse
   * @param defaultValue the default value
   * @return if the node is not null, the value of the parsed node, otherwise the default value
   */
  default Float parseFloat(Node node, Float defaultValue) {
    return node != null ? Float.valueOf(node.getNodeValue()) : defaultValue;
  }

  /**
   * Parses a float value.
   *
   * @param node the node to parse
   * @return if the node is not null, the value of the parsed node, otherwise null
   */
  default Float parseFloat(Node node) {
    return parseFloat(node, null);
  }

  /**
   * Parses a float value.
   *
   * @param attrs the attributes
   * @param name the name of the attribute to parse
   * @return if the node is not null, the value of the parsed node, otherwise null
   */
  default Float parseFloat(NamedNodeMap attrs, String name) {
    return parseFloat(attrs.getNamedItem(name));
  }

  /**
   * Parses a float value.
   *
   * @param attrs the attributes
   * @param name the name of the attribute to parse
   * @param defaultValue the default value
   * @return if the node is not null, the value of the parsed node, otherwise the default value
   */
  default Float parseFloat(NamedNodeMap attrs, String name, Float defaultValue) {
    return parseFloat(attrs.getNamedItem(name), defaultValue);
  }

  /**
   * Parses a double value.
   *
   * @param node the node to parse
   * @param defaultValue the default value
   * @return if the node is not null, the value of the parsed node, otherwise the default value
   */
  default Double parseDouble(Node node, Double defaultValue) {
    return node != null ? Double.valueOf(node.getNodeValue()) : defaultValue;
  }

  /**
   * Parses a double value.
   *
   * @param node the node to parse
   * @return if the node is not null, the value of the parsed node, otherwise null
   */
  default Double parseDouble(Node node) {
    return parseDouble(node, null);
  }

  /**
   * Parses a double value.
   *
   * @param attrs the attributes
   * @param name the name of the attribute to parse
   * @return if the node is not null, the value of the parsed node, otherwise null
   */
  default Double parseDouble(NamedNodeMap attrs, String name) {
    return parseDouble(attrs.getNamedItem(name));
  }

  /**
   * Parses a double value.
   *
   * @param attrs the attributes
   * @param name the name of the attribute to parse
   * @param defaultValue the default value
   * @return if the node is not null, the value of the parsed node, otherwise the default value
   */
  default Double parseDouble(NamedNodeMap attrs, String name, Double defaultValue) {
    return parseDouble(attrs.getNamedItem(name), defaultValue);
  }

  /**
   * Parses a string value.
   *
   * @param node the node to parse
   * @param defaultValue the default value
   * @return if the node is not null, the value of the parsed node, otherwise the default value
   */
  default String parseString(Node node, String defaultValue) {
    return node != null ? node.getNodeValue() : defaultValue;
  }

  /**
   * Parses a string value.
   *
   * @param node the node to parse
   * @return if the node is not null, the value of the parsed node, otherwise null
   */
  default String parseString(Node node) {
    return parseString(node, null);
  }

  /**
   * Parses a string value.
   *
   * @param attrs the attributes
   * @param name the name of the attribute to parse
   * @return if the node is not null, the value of the parsed node, otherwise null
   */
  default String parseString(NamedNodeMap attrs, String name) {
    return parseString(attrs.getNamedItem(name));
  }

  /**
   * Parses a string value.
   *
   * @param attrs the attributes
   * @param name the name of the attribute to parse
   * @param defaultValue the default value
   * @return if the node is not null, the value of the parsed node, otherwise the default value
   */
  default String parseString(NamedNodeMap attrs, String name, String defaultValue) {
    return parseString(attrs.getNamedItem(name), defaultValue);
  }

  /**
   * Parses an enumerated value.
   *
   * @param <T> the enumerated type
   * @param node the node to parse
   * @param clazz the class of the enumerated
   * @param defaultValue the default value
   * @return if the node is not null and the node value is valid the parsed value, otherwise the
   *     default value
   */
  default <T extends Enum<T>> T parseEnum(Node node, Class<T> clazz, T defaultValue) {
    if (node == null) {
      return defaultValue;
    }

    try {
      return Enum.valueOf(clazz, node.getNodeValue());
    } catch (IllegalArgumentException e) {
      LOG.warn(
          "Invalid value specified for node: {} specified value: {} should be enum value of \"{}\" using default value: {}",
          node.getNodeName(),
          node.getNodeValue(),
          clazz.getSimpleName(),
          defaultValue);
      return defaultValue;
    }
  }

  /**
   * Parses an enumerated value.
   *
   * @param <T> the enumerated type
   * @param node the node to parse
   * @param clazz the class of the enumerated
   * @return if the node is not null and the node value is valid the parsed value, otherwise null
   */
  default <T extends Enum<T>> T parseEnum(Node node, Class<T> clazz) {
    return parseEnum(node, clazz, null);
  }

  /**
   * Parses an enumerated value.
   *
   * @param <T> the enumerated type
   * @param attrs the attributes
   * @param clazz the class of the enumerated
   * @param name the name of the attribute to parse
   * @return if the node is not null and the node value is valid the parsed value, otherwise null
   */
  default <T extends Enum<T>> T parseEnum(NamedNodeMap attrs, Class<T> clazz, String name) {
    return parseEnum(attrs.getNamedItem(name), clazz);
  }

  /**
   * Parses an enumerated value.
   *
   * @param <T> the enumerated type
   * @param attrs the attributes
   * @param clazz the class of the enumerated
   * @param name the name of the attribute to parse
   * @param defaultValue the default value
   * @return if the node is not null and the node value is valid the parsed value, otherwise the
   *     default value
   */
  default <T extends Enum<T>> T parseEnum(
      NamedNodeMap attrs, Class<T> clazz, String name, T defaultValue) {
    return parseEnum(attrs.getNamedItem(name), clazz, defaultValue);
  }

  /**
   * Gets the current file filter.
   *
   * @return the current file filter
   */
  default FileFilter getCurrentFileFilter() {
    return XML_FILTER;
  }

  /**
   * Simple XML error handler.
   *
   * @author Zoey76
   */
  class XMLErrorHandler implements ErrorHandler {
    @Override
    public void warning(SAXParseException e) throws SAXParseException {
      throw e;
    }

    @Override
    public void error(SAXParseException e) throws SAXParseException {
      throw e;
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXParseException {
      throw e;
    }
  }
}
