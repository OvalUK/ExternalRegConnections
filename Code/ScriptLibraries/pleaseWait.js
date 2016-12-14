function fnRefreshStart() { 
         if(this.fullStandby==null) { 
                 this.fullStandby = new dojox.widget.Standby({ 
                         target: document.forms[0] // it was dojo.body() which creates a problem in some dojo versions. 
                 }); 
                 document.body.appendChild(fullStandby.domNode); 
                 fullStandby.startup(); 
         } 
         fullStandby.show(); 
 } 
 
 function fnRefreshComplete() { 
         if(this.fullStandby!=null) fullStandby.hide(); 
 }
 
 
 var f = function(){
	// Hijack the partial refresh
	XSP._inheritedPartialRefresh = XSP._partialRefresh;
	XSP._partialRefresh = function( method, form, refreshId, options ){ 
	// Publish init
	dojo.publish( 'partialrefresh-init', [ method, form, refreshId, options ]);
	this._inheritedPartialRefresh( method, form, refreshId, options );
	}

	// Publish start, complete and error states 
	dojo.subscribe( 'partialrefresh-init', function( method, form, 
	refreshId, options ){
	if( options ){
	options.onStart = function(){
	dojo.publish( 'partialrefresh-start', [method, form, refreshId, options]);
	};

	options.onComplete = function(){
	dojo.publish( 'partialrefresh-complete', 
	[method, form, refreshId, options]);
	};
	options.onError = function(){
	dojo.publish( 'partialrefresh-error', [ method, form, refreshId, options]);
	};
	}
	});
	}

	XSP.addOnLoad(f);
	
		dojo.subscribe( 'partialrefresh-start', null, function( method, form, refreshId ){
			fnRefreshStart();
		} );

		dojo.subscribe( 'partialrefresh-complete', null, function( method, form, refreshId ){
			fnRefreshComplete();
		} );

		dojo.subscribe( 'partialrefresh-error', null, function( method, form, refreshId ){
			fnRefreshComplete();
		} );