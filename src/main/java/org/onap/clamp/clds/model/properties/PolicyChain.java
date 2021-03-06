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
 * Parse Policy json properties.
 *
 * Example json:
 * {"Policy_1e33tn8":{"PolicyTest1":[{"name":"pname","value":"PolicyTest1"},{
 * "name":"pid","value":"1"},{"name":"timeout","value":"345"},{
 * "policyConfigurations":[[{"name":"recipe","value":["restart"]},{"name":
 * "maxRetries","value":["3"]},{"name":"retryTimeLimit","value":["180"]},{"name"
 * :"_id","value":["q2JmHD5"]},{"name":"parentPolicy","value":[""]}],[{"name":
 * "recipe","value":["rebuild"]},{"name":"maxRetries","value":["3"]},{"name":
 * "retryTimeLimit","value":["180"]},{"name":"_id","value":["0ZqHdrR"]},{"name":
 * "parentPolicy","value":[""]},{"name":
 * "targetResourceId","value":["Eace933104d443b496b8.nodes.heat.vpg"]}]]}],
 * "PolicyTest2":[{"name":"pname","value":
 * "PolicyTest2"},{"name":"pid","value":"2"},{"name":"timeout","value":"345"},{
 * "policyConfigurations":[[{"name":"recipe","value":["restart"]},{"name":
 * "maxRetries","value":["3"]},{"name":"retryTimeLimit","value":["180"]},{"name"
 * :"_id","value":["q2JmHD5"]},{"name":"parentPolicy","value":[""]}],[{"name":
 * "recipe","value":["rebuild"]},{"name":"maxRetries","value":["3"]},{"name":
 * "retryTimeLimit","value":["180"]},{"name":"_id","value":["0ZqHdrR"]},{"name":
 * "parentPolicy","value":[""]},{"name":
 * "targetResourceId","value":["Eace933104d443b496b8.nodes.heat.vpg"]}]]}]}} f
 *
 */
public class PolicyChain {

    protected static final EELFLogger logger      = EELFManager.getInstance().getLogger(PolicyChain.class);
    protected static final EELFLogger auditLogger = EELFManager.getInstance().getAuditLogger();

    private String                    policyId;
    private Integer                   timeout;
    private List<PolicyItem>          policyItems;
    private String                    policyType;

    public PolicyChain(JsonNode node) {

        policyId = AbstractModelElement.getValueByName(node, "pid");
        timeout = AbstractModelElement.getIntValueByName(node, "timeout");
        policyType = AbstractModelElement.getValueByName(node, "policyType");
    
        if(node != null && node.size() > 0) {    	   	
	        JsonNode policyNode = node.get(node.size() - 1).get("policyConfigurations");
	        if(policyNode != null) {
		        Iterator<JsonNode> itr = policyNode.elements();
		        policyItems = new ArrayList<>();
		        while (itr.hasNext()) {
		            policyItems.add(new PolicyItem(itr.next()));
		        }
	    
	         } 
	      }  
    }
    /**
     * @return the policyId
     */
    public String getPolicyId() {
        return policyId;
    }

    /**
     * @return the timeout
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * @return the policyItems
     */
    public List<PolicyItem> getPolicyItems() {
        return policyItems;
    }
    
    /**
     * @return the policyType
     */
    public String getPolicyType() {
        return policyType;
    }

}
