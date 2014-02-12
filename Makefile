submitClaim:
	curl 0.0.0.0:9876/api -d '{ "msgType":"submitClaim", "content":{ "verificationToken": "aksf-989jhadoiuhdfaisf", "claim": "Hippos may not taste good, but their hides are pliable and strong.", "verifier": { "source": "alias://NODE_ENDPOINT_HOST/aliasC", "label": "NODE_C2V_LABEL", "target": "alias://A_ENDPOINT_HOST/aliasV" }, "relyingParty": { "source": "alias://NODE_ENDPOINT_HOST/aliasC", "label": "NODE_C2R_LABEL", "target": "alias://A_ENDPOINT_HOST/aliasR" }}}'

produceClaim:
	curl 0.0.0.0:9876/api -d '{ "msgType":"produceClaim", "content":{ "verificationToken": "aksf-989jhadoiuhdfaisf", "claim": "Hippos may not taste good, but their hides are pliable and strong.", "claimant": { "source":  "alias://NODE_ENDPOINT_HOST/aliasR", "label": "NODE_R2C_LABEL", "target":  "alias://NODE_ENDPOINT_HOST/aliasC" }, "verifier": { "source":  "alias://NODE_ENDPOINT_HOST/aliasR", "label": "NODE_R2V_LABEL", "target": "alias://NODE_ENDPOINT_HOST/aliasV" }}}'

validateClaim:
	curl 0.0.0.0:9876/api -d '{ "msgType":"validateClaim", "content":{ "verificationToken": "aksf-989jhadoiuhdfaisf", "claim": "Hippos may not taste good, but their hides are pliable and strong.", "response": true, "claimant": { "source": "alias://NODE_ENDPOINT_HOST/aliasV", "label": "NODE_V2C_LABEL", "target": "alias://NODE_ENDPOINT_HOST/aliasC" }, "relyingParty": { "source": "alias://NODE_ENDPOINT_HOST/aliasV", "label": "NODE_V2R_LABEL", "target": "alias://NODE_ENDPOINT_HOST/aliasR" }}}'

completeClaim: 
	curl 0.0.0.0:9876/api -d '{ "msgType":"completeClaim", "content":{ "verificationToken": "aksf-989jhadoiuhdfaisf", "claim": "Hippos may not taste good, but their hides are pliable and strong.", "verifier": { "source": "alias://NODE_ENDPOINT_HOST/aliasR", "label": "NODE_R2V_LABEL", "target": "alias://NODE_ENDPOINT_HOST/aliasV" }, "claimant": { "source": "alias://NODE_ENDPOINT_HOST/aliasR", "label": "NODE_R2C_LABEL", "target": "alias://NODE_ENDPOINT_HOST/aliasC" }}}'
