/*
 * MQTT.Cool - https://mqtt.cool
 *
 * Authentication and Authorization Demo
 *
 * Copyright (c) Lightstreamer Srl
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package cool.mqtt.examples.auth_hooks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Utility class for retrieving configuration parameters.
 */
public class Configuration {

  private final DocumentBuilderFactory documentBuilderFactory =
      DocumentBuilderFactory.newInstance();
  private final File configurationDir;

  Configuration(File configurationDir) {
    this.configurationDir = configurationDir;
  }

  Set<String> retrieveBrokerAddresses() {
    try {
      Document configurationFile = parseConfigurationFile(configurationDir);
      Stream<Node> nodes = streamOfParameters(configurationFile);
      return extractServerAddresses(nodes);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Document parseConfigurationFile(File configDir)
      throws ParserConfigurationException, SAXException, IOException {

    DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
    Path path = Paths.get(configDir.getPath(), "../conf/brokers_configuration.xml");
    Document configurationFile = builder.parse(path.toFile());
    return configurationFile;
  }

  private Stream<Node> streamOfParameters(Document configurationFile) {
    NodeList nodeList = configurationFile.getElementsByTagName("param");
    List<Node> nodes = new ArrayList<>(nodeList.getLength());
    for (int i = 0; i < nodeList.getLength(); i++) {
      nodes.add(nodeList.item(i));
    }
    return nodes.stream();
  }

  private Set<String> extractServerAddresses(Stream<Node> nodes) {
    return nodes.filter(node -> {
      Node nameAttribute = node.getAttributes().getNamedItem("name");
      return nameAttribute.getTextContent().contains(".server_address");
    }).map(Node::getTextContent).collect(Collectors.toSet());
  }
}
