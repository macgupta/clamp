/*-
 * ============LICENSE_START=======================================================
 * ONAP CLAMP
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights
 *                             reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 * ===================================================================
 * 
 */

package org.onap.clamp.clds.model.properties;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provide base ModelElement functionality. Perform base parsing of properties
 * for a ModelElement (such as, VesCollector, Policy, Tca, Holmes, ...)
 */
public abstract class AbstractModelElement {

    protected static final EELFLogger logger = EELFManager.getInstance().getLogger(AbstractModelElement.class);
    protected static final EELFLogger auditLogger = EELFManager.getInstance().getAuditLogger();
    private final String type;
    private final ModelBpmn modelBpmn;
    private final String id;
    protected String topicPublishes;
    protected final JsonNode modelElementJsonNode;
    private boolean isFound;
    private final ModelProperties modelProp;
    private static final String LOG_ELEMENT_NOT_FOUND = "Value '{}' for key 'name' not found in JSON";
    private static final String LOG_ELEMENT_NOT_FOUND_IN_JSON = "Value '{}' for key 'name' not found in JSON {}";

    /**
     * Perform base parsing of properties for a ModelElement (such as,
     * VesCollector, Policy and Tca)
     */
    protected AbstractModelElement(String type, ModelProperties modelProp, ModelBpmn modelBpmn, JsonNode modelJson) {
        this.type = type;
        this.modelProp = modelProp;
        this.modelBpmn = modelBpmn;
        this.id = modelBpmn.getId(type);
        this.modelElementJsonNode = modelJson.get(id);
        this.isFound = modelBpmn.isModelElementTypeInList(type);
    }

    /**
     * topicSubscribes is the topicPublishes of the from Model Element (the
     * previous one in the chain).
     *
     * @return the topicSubscribes
     */
    public String getTopicSubscribes() {
        // get fromId for this type
        String fromId = modelBpmn.getFromId(type);
        // find the type of the from model element
        String fromType = modelBpmn.getType(fromId);
        // get the model element for the type
        AbstractModelElement me = modelProp.getModelElementByType(fromType);
        // get the topic publishes for the model element
        return me.topicPublishes;
    }

    /**
     * @return the topicPublishes
     */
    public String getTopicPublishes() {
        return topicPublishes;
    }

    /**
     * Return the value field of the json node element that has a name field
     * equals to the given name.
     */
    public static String getValueByName(JsonNode nodeIn, String name) {
        String value = null;
        if (nodeIn != null) {
            for (JsonNode node : nodeIn) {
                if (node.path("name").asText().equals(name)) {
                    JsonNode vnode = node.path("value");
                    if (vnode.isArray()) {
                        // if array, assume value is in first element
                        value = vnode.path(0).asText();
                    } else {
                        // otherwise, just return text
                        value = vnode.asText();
                    }
                }
            }
        }
        if (value == null || value.length() == 0) {
            logger.warn(LOG_ELEMENT_NOT_FOUND, name);
        } else {
            logger.debug(LOG_ELEMENT_NOT_FOUND_IN_JSON, name, nodeIn.toString());
        }
        return value;
    }

    /**
     * Return the Json value field of the json node element that has a name
     * field equals to the given name.
     */
    public static JsonNode getJsonNodeByName(JsonNode nodeIn, String name) {
        JsonNode vnode = null;
        if (nodeIn != null) {
            for (JsonNode node : nodeIn) {
                if (node.path("name").asText().equals(name)) {
                    vnode = node.path("value");
                }
            }
        }
        if (vnode == null) {
            logger.warn(LOG_ELEMENT_NOT_FOUND, name);
        } else {
            logger.debug(LOG_ELEMENT_NOT_FOUND_IN_JSON, name, nodeIn.toString());
        }
        return vnode;
    }

    /**
     * Return the value field of the json node element that has a name field
     * that equals the given name.
     */
    public static String getNodeValueByName(JsonNode nodeIn, String name) {
        String value = null;
        if (nodeIn != null) {
            value = nodeIn.path(name).asText();
        }
        if (value == null || value.length() == 0) {
            logger.warn(LOG_ELEMENT_NOT_FOUND, name);
        } else {
            logger.debug(LOG_ELEMENT_NOT_FOUND_IN_JSON, name, nodeIn.toString());
        }
        return value;
    }

    /**
     * Return the value field of the json node element that has a name field
     * that equals the given name.
     */
    public static List<String> getNodeValuesByName(JsonNode nodeIn, String name) {
        List<String> values = new ArrayList<>();
        if (nodeIn != null) {
            for (JsonNode node : nodeIn) {
                if (node.path("name").asText().equals(name)) {
                    JsonNode vnode = node.path("value");
                    if (vnode.isArray()) {
                        // if array, assume value is in first element
                        values.add(vnode.path(0).asText());
                    } else {
                        // otherwise, just return text
                        values.add(vnode.asText());
                    }
                }
            }
        }
        return values;
    }

    /**
     * Return the int value field of the json node element that has a name field
     * equals to the given name.
     */
    public static Integer getIntValueByName(JsonNode nodeIn, String name) {
        String value = getValueByName(nodeIn, name);
        return Integer.valueOf(value);
    }

    /**
     * Return an array of values for the field of the json node element that has
     * a name field equals to the given name.
     */
    public static List<String> getValuesByName(JsonNode nodeIn, String name) {
        List<String> values = null;
        if (nodeIn != null) {
            for (JsonNode node : nodeIn) {
                if (node.path("name").asText().equals(name)) {
                    values = getValuesList(node);
                }
            }
        }
        if (values == null || values.isEmpty()) {
            logger.warn(LOG_ELEMENT_NOT_FOUND, name);
        } else {
            logger.debug(LOG_ELEMENT_NOT_FOUND_IN_JSON, name, nodeIn.toString());
        }
        return values;
    }

    /**
     * Return an array of String values.
     */
    public static List<String> getValuesList(JsonNode nodeIn) {
        ArrayList<String> al = new ArrayList<>();
        if (nodeIn != null) {
            Iterator<JsonNode> itr = nodeIn.path("value").elements();
            while (itr.hasNext()) {
                JsonNode node = itr.next();
                al.add(node.asText());
            }
        }
        return al;
    }

    /**
     * Return the value field of the json node element that has a name field
     * equals to the given name.
     */
    public String getValueByName(String name) {
        return getValueByName(modelElementJsonNode, name);
    }

    /**
     * Return the int value field of the json node element that has a name field
     * equals to the given name.
     */
    public Integer getIntValueByName(String name) {
        return getIntValueByName(modelElementJsonNode, name);
    }

    /**
     * Return an array of values for the field of the json node element that has
     * a name field equals to the given name.
     */
    public List<String> getValuesByName(String name) {
        return getValuesByName(modelElementJsonNode, name);
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the isFound
     */
    public boolean isFound() {
        return isFound;
    }
}
