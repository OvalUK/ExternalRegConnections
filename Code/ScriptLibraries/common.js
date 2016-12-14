function x$(idTag, param){ //Updated 18 Feb 2012
	   idTag=idTag.replace(/:/gi, "\\:")+(param ? param : "");
	   return($("#"+idTag));
	}

function fnOnDialogLoad(id) {
	dialogObj = dijit.byId(id);  
	dialogObj._fadeOutDeferred = true;	
}

function fnShowPleaseWait() {
	if(this.fullStandby==null) { 
        this.fullStandby = new dojox.widget.Standby({ 
                target: document.forms[0], // it was dojo.body() which creates a problem in some dojo versions. 
                zIndex: "2000"
        }); 
        document.body.appendChild(fullStandby.domNode); 
        fullStandby.startup(); 
	} 
	fullStandby.show(); 
}
function fnHidePleaseWait() {
	if(this.fullStandby!=null) fullStandby.hide(); 
}


