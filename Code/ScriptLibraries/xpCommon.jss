function clearMap( map:Map ){
 // Get iterator for the keys
 var iterator = map.keySet().iterator();
 
 // Remove all items
 while( iterator.hasNext() ){
  map.remove( iterator.next() );
 }
}

function fnCanEdit(docXSP:NotesXspDocument) {
	/*
	
	if (!userBean.isAuthenticated) return false;
	if (!userBean.isAuthor(doc.getDocument(), "DocAuthors")) return false;
	return true;
	*/
	if (docXSP.isEditable()) return false;
	var ctx = com.ibm.domino.xsp.module.nsf.NotesContext.getCurrent();
	return ctx.isDocEditable(docXSP.getDocument());
}
function fnAddFacesMessage( message, component ){
 try {  
  if( typeof component === 'string' ){
   component = getComponent( component );
  }
  
  var clientId = null;
  if( component ){
   clientId = component.getClientId( facesContext );
  }
  
  facesContext.addMessage( clientId, 
    new javax.faces.application.FacesMessage( message ) );
 } catch(e){ dBar.error(e) }
}

//Fetch messages for specified components
function getFacesMessages( components ){
 try { 
  if( typeof components !== 'array' ){ components = [ components ]; } 
  var clientId, component, messages = [], msgIterator;  
  for( var i = 0; i < components.length; i++ ){
   component = components[i];
   if( typeof component === 'string' ){
    clientId = getClientId( component );
   } else {
    clientId = component.getClientId( facesContext );
   }
   
   msgIterator = facesContext.getMessages( clientId );
   if( !msgIterator ){ continue; }   
   while( msgIterator.hasNext() ){
    messages.push( msgIterator.next().getSummary() );
   }
  }
  return messages;  
 } catch( e ){ /*Debug.logException( e );*/ }
}