/*-
 * ============LICENSE_START=======================================================
 * ONAP CLAMP
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.onap.clamp.clds.client.req.sdc;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.codec.DecoderException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.clamp.clds.client.req.tca.TcaRequestFormatter;
import org.onap.clamp.clds.model.CldsSdcResource;
import org.onap.clamp.clds.model.CldsSdcServiceDetail;
import org.onap.clamp.clds.model.prop.Global;
import org.onap.clamp.clds.model.prop.ModelProperties;
import org.onap.clamp.clds.model.prop.Tca;
import org.onap.clamp.clds.model.refprop.RefProp;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Construct a Sdc request given CLDS objects.
 */
public class SdcReq {
    protected static final EELFLogger logger        = EELFManager.getInstance().getLogger(SdcReq.class);
    protected static final EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();
    @Autowired
    protected RefProp                 refProp;

    /**
     * Format the Blueprint from a Yaml
     *
     * @param prop
     *            The ModelProperties describing the clds model
     * @param docText
     *            The Yaml file that must be converted
     *
     * @return A String containing the BluePrint
     * @throws JsonParseException
     *             In case of issues
     * @throws JsonMappingException
     *             In case of issues
     * @throws IOException
     *             In case of issues
     */
    public String formatBlueprint(ModelProperties prop, String docText)
            throws JsonParseException, JsonMappingException, IOException {
        String yamlvalue = getYamlvalue(docText);
        String updatedBlueprint = "";
        Tca tca = prop.getType(Tca.class);
        if (tca.isFound()) {
            updatedBlueprint = TcaRequestFormatter.updatedBlueprintWithConfiguration(refProp, prop, yamlvalue);
        }
        logger.info("value of blueprint:" + updatedBlueprint);
        return updatedBlueprint;
    }

    /**
     * Format the SDC Locations Request in the JSON Format
     *
     * @param prop
     *            The ModelProperties describing the clds model
     * @param artifactName
     *            The name of the artifact
     *
     * @return SDC Locations request in the JSON Format
     */
    public String formatSdcLocationsReq(ModelProperties prop, String artifactName) {
        ObjectMapper objectMapper = new ObjectMapper();
        Global global = prop.getGlobal();
        List<String> locationsList = global.getLocation();
        ArrayNode locationsArrayNode = objectMapper.createArrayNode();
        ObjectNode locationObject = objectMapper.createObjectNode();
        for (String currLocation : locationsList) {
            locationsArrayNode.add(currLocation);
        }
        locationObject.put("artifactName", artifactName);
        locationObject.putPOJO("locations", locationsArrayNode);
        String locationJsonFormat = locationObject.toString();
        logger.info("Value of location Json Artifact:" + locationsArrayNode);
        return locationJsonFormat;
    }

    /**
     * Format the SDC Request
     *
     * @param payloadData
     *            The ModelProperties describing the clds model
     * @param artifactName
     *            The name of the artifact
     * @param artifactLabel
     *            The Label of the artifact
     * @param artifactType
     *            The type of the artifact
     * @return formatted SDC Request
     * @throws IOException
     *             In case of issues
     */
    public String formatSdcReq(String payloadData, String artifactName, String artifactLabel, String artifactType)
            throws IOException {
        logger.info("artifact=" + payloadData);
        String base64Artifact = Base64.getEncoder().encodeToString(payloadData.getBytes(StandardCharsets.UTF_8));
        return "{ \n" + "\"payloadData\" : \"" + base64Artifact + "\",\n" + "\"artifactLabel\" : \"" + artifactLabel
                + "\",\n" + "\"artifactName\" :\"" + artifactName + "\",\n" + "\"artifactType\" : \"" + artifactType
                + "\",\n" + "\"artifactGroupType\" : \"DEPLOYMENT\",\n" + "\"description\" : \"from CLAMP Cockpit\"\n"
                + "} \n";
    }

    /**
     * To get List of urls for all vfresources
     *
     * @param prop
     * @param baseUrl
     * @param sdcCatalogServices
     * @param execution
     * @return
     * @throws GeneralSecurityException
     *             In case of issues when decrypting the password
     * @throws DecoderException
     *             In case of issues when decoding the Hex String
     */
    public List<String> getSdcReqUrlsList(ModelProperties prop, String baseUrl, SdcCatalogServices sdcCatalogServices,
            DelegateExecution execution) throws GeneralSecurityException, DecoderException {
        // TODO : refact and regroup with very similar code
        List<String> urlList = new ArrayList<>();
        Global globalProps = prop.getGlobal();
        if (globalProps != null) {
            if (globalProps.getService() != null) {
                String serviceInvariantUUID = globalProps.getService();
                execution.setVariable("serviceInvariantUUID", serviceInvariantUUID);
                List<String> resourceVfList = globalProps.getResourceVf();
                String serviceUUID = sdcCatalogServices.getServiceUuidFromServiceInvariantId(serviceInvariantUUID);
                String sdcServicesInformation = sdcCatalogServices.getSdcServicesInformation(serviceUUID);
                CldsSdcServiceDetail cldsSdcServiceDetail = sdcCatalogServices
                        .getCldsSdcServiceDetailFromJson(sdcServicesInformation);
                if (cldsSdcServiceDetail != null && resourceVfList != null) {
                    List<CldsSdcResource> cldsSdcResourcesList = cldsSdcServiceDetail.getResources();
                    if (cldsSdcResourcesList != null && !cldsSdcResourcesList.isEmpty()) {
                        for (CldsSdcResource CldsSdcResource : cldsSdcResourcesList) {
                            if (CldsSdcResource != null && CldsSdcResource.getResoucreType() != null
                                    && CldsSdcResource.getResoucreType().equalsIgnoreCase("VF")
                                    && resourceVfList.contains(CldsSdcResource.getResourceInvariantUUID())) {
                                String normalizedResourceInstanceName = normalizeResourceInstanceName(
                                        CldsSdcResource.getResourceInstanceName());
                                String svcUrl = baseUrl + "/" + serviceUUID + "/resourceInstances/"
                                        + normalizedResourceInstanceName + "/artifacts";
                                urlList.add(svcUrl);
                            }
                        }
                    }
                }
            }
        }
        return urlList;
    }

    /**
     * "Normalize" the resource instance name: - Remove spaces, underscores,
     * dashes, and periods. - make lower case This is required by SDC when using
     * the resource instance name to upload an artifact.
     *
     * @param inText
     * @return
     */
    public String normalizeResourceInstanceName(String inText) {
        return inText.replace(" ", "").replace("-", "").replace(".", "").toLowerCase();
    }

    /**
     * Method to get yaml/template properties value from json
     *
     * @param docText
     * @return
     * @throws IOException
     */
    public String getYamlvalue(String docText) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String yamlFileValue = "";
        ObjectNode root = objectMapper.readValue(docText, ObjectNode.class);
        Iterator<Entry<String, JsonNode>> entryItr = root.fields();
        while (entryItr.hasNext()) {
            Entry<String, JsonNode> entry = entryItr.next();
            String key = entry.getKey();
            if (key != null && key.equalsIgnoreCase("global")) {
                ArrayNode arrayNode = (ArrayNode) entry.getValue();
                for (JsonNode anArrayNode : arrayNode) {
                    ObjectNode node = (ObjectNode) anArrayNode;
                    ArrayNode arrayValueNode = (ArrayNode) node.get("value");
                    JsonNode jsonNode = arrayValueNode.get(0);
                    yamlFileValue = jsonNode.asText();
                    logger.info("value:" + yamlFileValue);
                }
                break;
            }
        }
        return yamlFileValue;
    }
}