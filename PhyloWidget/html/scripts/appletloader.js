
var loadingYahoo = false;
var ready = false;

/*
 * The codebase to use when accessing the applet archive and class files.
 * 
 * By default set to ${lib}.
 */
var codebase = '${lib}';
function setCodebase(url)
{
	codebase = url;
}

loadYahooCrap();

/*********************************************
 * PUBLIC FUNCTIONS: PLEASE USE THESE!!!
 *********************************************/
 
function loadApplet(div_id,propsRef)
{
	applet(div_id,propsRef);
}

function loadAppletObject(div_id,propsRef)
{
	// Load up the appletobject.js file.
	var mydiv = document.createElement("script");
	mydiv.id="script_loader";
	mydiv.src="scripts/appletobject.js";
	document.getElementsByTagName("head")[0].appendChild(mydiv);
	// Wait until the appletobject script is loaded, then continue.
	var myF = function() {appletObject(div_id,propsRef);};
	mydiv.onload = myF;
	mydiv.onreadystatechange= function () {
     if (this.readyState == 'complete') myF();
   }
}

function loadAppletPanel(propsRef)
{
	loadYahooCrap();
	if (!ready)
	{
		setTimeout(function(){loadAppletPanel(propsRef);},250);
		return;
	}
	var id = ''+new Date().getTime();
	if (propsRef.id)
	{
		id = propsRef.id;
	}
	loadSketchPanel(id);
	applet('applet_holder'+id,id,propsRef);
}

function loadAppletObjectPanel(propsRef)
{
	loadYahooCrap();
	if (!ready)
	{
		setTimeout(function(){loadAppletObjectPanel(propsRef);},250);
		return;
	}
	// Load up the appletobject.js file.
	var mydiv = document.createElement("script");
	mydiv.id="script_loader";
	mydiv.src="scripts/appletobject.js";
	var body = document.getElementsByTagName("body")[0]; 
	body.insertBefore(mydiv,body.firstChild);
	// Wait until the appletobject script is loaded, then continue.
	var myF = function() {
		var id = '' + new Date().getTime();
		loadSketchPanel(id);
		appletObject('applet_holder'+id,id,propsRef);
		};
	mydiv.onload = myF;
	mydiv.onreadystatechange= function () {
      if (this.readyState == 'complete') myF();
   }
}




/* ********************************************
 * NOT-SO-PUBLIC FUNCTIONS: JUST IGNORE THESE!!!
 *********************************************/


function loadSketchPanel(id)
{
  // Create a dummy DIV to hold the panel. 
  var mydiv = document.createElement("div");
  mydiv.id="panel_holder"+id;
  var body = document.getElementsByTagName("body")[0]; 
  body.insertBefore(mydiv,body.firstChild);
  // Create the panel.
  var w = ${applet.width} + 20;
  var h = ${applet.height} + 20;
  var panel2 = new YAHOO.widget.Panel("panel"+id, {visible:true, constraintoviewport:true,resizable:true} );
  panel2.setHeader("${project.name}");
  panel2.setBody("<div id='applet_holder"+id+"'></div>");
  panel2.setFooter("Created with <a href='http://www.processing.org/'>Processing</a> and <a href='http://www.andrewberman.org/'>P5Barebones</a>");
  panel2.hideEvent.subscribe(function() {
  	setTimeout(function() {stopApplet(id);panel2.destroy();},100);
  });
  panel2.render(mydiv.id);
}

function applet(container_id,id,propsRef)
{
// Load the properties into the applet tag.
var propS = '';
for (var prop in propsRef)
{
	propS += '<param name="'+prop +'" value="'+propsRef[prop]+'"/>';  
}

document.getElementById(container_id).innerHTML =
'<applet id="'+id+'" name="'+id+'" code="${internal.launcherClass}" ' +
 'codebase="'+codebase+'" archive="${internal.allJars}" width="${applet.width}" height="${applet.height}" mayscript="true">' +
'<param name="image" value="loading.gif"> <param name="boxmessage" value="Loading Processing software...">' +
'<param name="boxbgcolor" value="#FFFFFF"> <param name="progressbar" value="true"> <param name="subapplet.classname" VALUE="${project.class}"> ' + 
'<param name="subapplet.displayname" VALUE="${project.name}">' +
propS +
'</applet> ';
}

function appletObject(container_id,id,propsRef)
{
  var ao = new AppletObject(
	'${internal.launcherClass}',
	[${internal.quotedJars}],
	'${applet.width}',
	'${applet.height}',
	'0', // java version: 1.4.2
	'true',  // mayscript
	codebase,   // codebase
	[], // additional params
	AppletObjects.TAG_OBJECT, // Type of insertion -- either AppletObjects.TAG_APPLEt or AppletObject.TAG_OBJECT 
	id  
  );
  ao.addParams(['boxmessage','Loading ${project.name}...']);
  ao.addParams(['subapplet.classname','${project.class}']);
  ao.addParams(['subapplet.displayname','${project.name}']);
  
  // Load up the properties to go into the applet or object tag.
  for (var prop in propsRef)
  {
  	ao.addParams([prop,propsRef[prop]]);
  }
  ao.preload(container_id);
}

function stopApplet(el_id)
{
 	var applet = document.getElementById(el_id);
	applet.stop();
	applet.destroy();
}

function loadYahooCrap()
{
	if (loadingYahoo)
		return;
	loadingYahoo = true;
	  /*
	Copyright (c) 2008, Yahoo! Inc. All rights reserved.
	Code licensed under the BSD License:
	http://developer.yahoo.net/yui/license.txt
	version: 2.5.0
	*/
	if(typeof YAHOO=="undefined"||!YAHOO){var YAHOO={};}YAHOO.namespace=function(){var A=arguments,E=null,C,B,D;for(C=0;C<A.length;C=C+1){D=A[C].split(".");E=YAHOO;for(B=(D[0]=="YAHOO")?1:0;B<D.length;B=B+1){E[D[B]]=E[D[B]]||{};E=E[D[B]];}}return E;};YAHOO.log=function(D,A,C){var B=YAHOO.widget.Logger;if(B&&B.log){return B.log(D,A,C);}else{return false;}};YAHOO.register=function(A,E,D){var I=YAHOO.env.modules;if(!I[A]){I[A]={versions:[],builds:[]};}var B=I[A],H=D.version,G=D.build,F=YAHOO.env.listeners;B.name=A;B.version=H;B.build=G;B.versions.push(H);B.builds.push(G);B.mainClass=E;for(var C=0;C<F.length;C=C+1){F[C](B);}if(E){E.VERSION=H;E.BUILD=G;}else{YAHOO.log("mainClass is undefined for module "+A,"warn");}};YAHOO.env=YAHOO.env||{modules:[],listeners:[]};YAHOO.env.getVersion=function(A){return YAHOO.env.modules[A]||null;};YAHOO.env.ua=function(){var C={ie:0,opera:0,gecko:0,webkit:0,mobile:null};var B=navigator.userAgent,A;if((/KHTML/).test(B)){C.webkit=1;}A=B.match(/AppleWebKit\/([^\s]*)/);if(A&&A[1]){C.webkit=parseFloat(A[1]);if(/ Mobile\//.test(B)){C.mobile="Apple";}else{A=B.match(/NokiaN[^\/]*/);if(A){C.mobile=A[0];}}}if(!C.webkit){A=B.match(/Opera[\s\/]([^\s]*)/);if(A&&A[1]){C.opera=parseFloat(A[1]);A=B.match(/Opera Mini[^;]*/);if(A){C.mobile=A[0];}}else{A=B.match(/MSIE\s([^;]*)/);if(A&&A[1]){C.ie=parseFloat(A[1]);}else{A=B.match(/Gecko\/([^\s]*)/);if(A){C.gecko=1;A=B.match(/rv:([^\s\)]*)/);if(A&&A[1]){C.gecko=parseFloat(A[1]);}}}}}return C;}();(function(){YAHOO.namespace("util","widget","example");if("undefined"!==typeof YAHOO_config){var B=YAHOO_config.listener,A=YAHOO.env.listeners,D=true,C;if(B){for(C=0;C<A.length;C=C+1){if(A[C]==B){D=false;break;}}if(D){A.push(B);}}}})();YAHOO.lang=YAHOO.lang||{isArray:function(B){if(B){var A=YAHOO.lang;return A.isNumber(B.length)&&A.isFunction(B.splice);}return false;},isBoolean:function(A){return typeof A==="boolean";},isFunction:function(A){return typeof A==="function";},isNull:function(A){return A===null;},isNumber:function(A){return typeof A==="number"&&isFinite(A);},isObject:function(A){return(A&&(typeof A==="object"||YAHOO.lang.isFunction(A)))||false;},isString:function(A){return typeof A==="string";},isUndefined:function(A){return typeof A==="undefined";},hasOwnProperty:function(A,B){if(Object.prototype.hasOwnProperty){return A.hasOwnProperty(B);}return !YAHOO.lang.isUndefined(A[B])&&A.constructor.prototype[B]!==A[B];},_IEEnumFix:function(C,B){if(YAHOO.env.ua.ie){var E=["toString","valueOf"],A;for(A=0;A<E.length;A=A+1){var F=E[A],D=B[F];if(YAHOO.lang.isFunction(D)&&D!=Object.prototype[F]){C[F]=D;}}}},extend:function(D,E,C){if(!E||!D){throw new Error("YAHOO.lang.extend failed, please check that "+"all dependencies are included.");}var B=function(){};B.prototype=E.prototype;D.prototype=new B();D.prototype.constructor=D;D.superclass=E.prototype;if(E.prototype.constructor==Object.prototype.constructor){E.prototype.constructor=E;}if(C){for(var A in C){D.prototype[A]=C[A];}YAHOO.lang._IEEnumFix(D.prototype,C);}},augmentObject:function(E,D){if(!D||!E){throw new Error("Absorb failed, verify dependencies.");}var A=arguments,C,F,B=A[2];if(B&&B!==true){for(C=2;C<A.length;C=C+1){E[A[C]]=D[A[C]];}}else{for(F in D){if(B||!E[F]){E[F]=D[F];}}YAHOO.lang._IEEnumFix(E,D);}},augmentProto:function(D,C){if(!C||!D){throw new Error("Augment failed, verify dependencies.");}var A=[D.prototype,C.prototype];for(var B=2;B<arguments.length;B=B+1){A.push(arguments[B]);}YAHOO.lang.augmentObject.apply(this,A);},dump:function(A,G){var C=YAHOO.lang,D,F,I=[],J="{...}",B="f(){...}",H=", ",E=" => ";if(!C.isObject(A)){return A+"";}else{if(A instanceof Date||("nodeType" in A&&"tagName" in A)){return A;}else{if(C.isFunction(A)){return B;}}}G=(C.isNumber(G))?G:3;if(C.isArray(A)){I.push("[");for(D=0,F=A.length;D<F;D=D+1){if(C.isObject(A[D])){I.push((G>0)?C.dump(A[D],G-1):J);}else{I.push(A[D]);}I.push(H);}if(I.length>1){I.pop();}I.push("]");}else{I.push("{");for(D in A){if(C.hasOwnProperty(A,D)){I.push(D+E);if(C.isObject(A[D])){I.push((G>0)?C.dump(A[D],G-1):J);}else{I.push(A[D]);}I.push(H);}}if(I.length>1){I.pop();}I.push("}");}return I.join("");},substitute:function(Q,B,J){var G,F,E,M,N,P,D=YAHOO.lang,L=[],C,H="dump",K=" ",A="{",O="}";for(;;){G=Q.lastIndexOf(A);if(G<0){break;}F=Q.indexOf(O,G);if(G+1>=F){break;}C=Q.substring(G+1,F);M=C;P=null;E=M.indexOf(K);if(E>-1){P=M.substring(E+1);M=M.substring(0,E);}N=B[M];if(J){N=J(M,N,P);}if(D.isObject(N)){if(D.isArray(N)){N=D.dump(N,parseInt(P,10));}else{P=P||"";var I=P.indexOf(H);if(I>-1){P=P.substring(4);}if(N.toString===Object.prototype.toString||I>-1){N=D.dump(N,parseInt(P,10));}else{N=N.toString();}}}else{if(!D.isString(N)&&!D.isNumber(N)){N="~-"+L.length+"-~";L[L.length]=C;}}Q=Q.substring(0,G)+N+Q.substring(F+1);}for(G=L.length-1;G>=0;G=G-1){Q=Q.replace(new RegExp("~-"+G+"-~"),"{"+L[G]+"}","g");}return Q;},trim:function(A){try{return A.replace(/^\s+|\s+$/g,"");}catch(B){return A;}},merge:function(){var D={},B=arguments;for(var C=0,A=B.length;C<A;C=C+1){YAHOO.lang.augmentObject(D,B[C],true);}return D;},later:function(H,B,I,D,E){H=H||0;B=B||{};var C=I,G=D,F,A;if(YAHOO.lang.isString(I)){C=B[I];}if(!C){throw new TypeError("method undefined");}if(!YAHOO.lang.isArray(G)){G=[D];}F=function(){C.apply(B,G);};A=(E)?setInterval(F,H):setTimeout(F,H);return{interval:E,cancel:function(){if(this.interval){clearInterval(A);}else{clearTimeout(A);}}};},isValue:function(B){var A=YAHOO.lang;return(A.isObject(B)||A.isString(B)||A.isNumber(B)||A.isBoolean(B));}};YAHOO.util.Lang=YAHOO.lang;YAHOO.lang.augment=YAHOO.lang.augmentProto;YAHOO.augment=YAHOO.lang.augmentProto;YAHOO.extend=YAHOO.lang.extend;YAHOO.register("yahoo",YAHOO,{version:"2.5.0",build:"897"});YAHOO.util.Get=function(){var I={},H=0,B=0,O=false,A=YAHOO.env.ua,D=YAHOO.lang;var Q=function(U,R,V){var S=V||window,W=S.document,X=W.createElement(U);for(var T in R){if(R[T]&&YAHOO.lang.hasOwnProperty(R,T)){X.setAttribute(T,R[T]);}}return X;};var N=function(R,S){return Q("link",{"id":"yui__dyn_"+(B++),"type":"text/css","rel":"stylesheet","href":R},S);
	};var M=function(R,S){return Q("script",{"id":"yui__dyn_"+(B++),"type":"text/javascript","src":R},S);};var K=function(R){return{tId:R.tId,win:R.win,data:R.data,nodes:R.nodes,purge:function(){J(this.tId);}};};var P=function(T){var R=I[T];if(R.onFailure){var S=R.scope||R.win;R.onFailure.call(S,K(R));}};var F=function(T){var R=I[T];R.finished=true;if(R.aborted){P(T);return ;}if(R.onSuccess){var S=R.scope||R.win;R.onSuccess.call(S,K(R));}};var E=function(T,W){var S=I[T];if(S.aborted){P(T);return ;}if(W){S.url.shift();if(S.varName){S.varName.shift();}}else{S.url=(D.isString(S.url))?[S.url]:S.url;if(S.varName){S.varName=(D.isString(S.varName))?[S.varName]:S.varName;}}var Z=S.win,Y=Z.document,X=Y.getElementsByTagName("head")[0],U;if(S.url.length===0){if(S.type==="script"&&A.webkit&&A.webkit<420&&!S.finalpass&&!S.varName){var V=M(null,S.win);V.innerHTML='YAHOO.util.Get._finalize("'+T+'");';S.nodes.push(V);X.appendChild(V);}else{F(T);}return ;}var R=S.url[0];if(S.type==="script"){U=M(R,Z);}else{U=N(R,Z);}G(S.type,U,T,R,Z,S.url.length);S.nodes.push(U);X.appendChild(U);if((A.webkit||A.gecko)&&S.type==="css"){E(T,R);}};var C=function(){if(O){return ;}O=true;for(var R in I){var S=I[R];if(S.autopurge&&S.finished){J(S.tId);delete I[R];}}O=false;};var J=function(X){var U=I[X];if(U){var W=U.nodes,R=W.length,V=U.win.document,T=V.getElementsByTagName("head")[0];for(var S=0;S<R;S=S+1){T.removeChild(W[S]);}}U.nodes=[];};var L=function(S,R,T){var V="q"+(H++);T=T||{};if(H%YAHOO.util.Get.PURGE_THRESH===0){C();}I[V]=D.merge(T,{tId:V,type:S,url:R,finished:false,nodes:[]});var U=I[V];U.win=U.win||window;U.scope=U.scope||U.win;U.autopurge=("autopurge" in U)?U.autopurge:(S==="script")?true:false;D.later(0,U,E,V);return{tId:V};};var G=function(a,V,U,S,W,X,Z){var Y=Z||E;if(A.ie){V.onreadystatechange=function(){var b=this.readyState;if("loaded"===b||"complete"===b){Y(U,S);}};}else{if(A.webkit){if(a==="script"){if(A.webkit>=420){V.addEventListener("load",function(){Y(U,S);});}else{var R=I[U];if(R.varName){var T=YAHOO.util.Get.POLL_FREQ;R.maxattempts=YAHOO.util.Get.TIMEOUT/T;R.attempts=0;R._cache=R.varName[0].split(".");R.timer=D.later(T,R,function(f){var d=this._cache,c=d.length,b=this.win,e;for(e=0;e<c;e=e+1){b=b[d[e]];if(!b){this.attempts++;if(this.attempts++>this.maxattempts){R.timer.cancel();P(U);}else{}return ;}}R.timer.cancel();Y(U,S);},null,true);}else{D.later(YAHOO.util.Get.POLL_FREQ,null,Y,[U,S]);}}}}else{V.onload=function(){Y(U,S);};}}};return{POLL_FREQ:10,PURGE_THRESH:20,TIMEOUT:2000,_finalize:function(R){D.later(0,null,F,R);},abort:function(S){var T=(D.isString(S))?S:S.tId;var R=I[T];if(R){R.aborted=true;}},script:function(R,S){return L("script",R,S);},css:function(R,S){return L("css",R,S);}};}();YAHOO.register("get",YAHOO.util.Get,{version:"2.5.0",build:"897"});(function(){var Y=YAHOO,util=Y.util,lang=Y.lang,env=Y.env;var YUI={dupsAllowed:{"yahoo":true,"get":true},info:{"base":"http://yui.yahooapis.com/2.5.0/build/","skin":{"defaultSkin":"sam","base":"assets/skins/","path":"skin.css","rollup":3},"moduleInfo":{"animation":{"type":"js","path":"animation/animation-min.js","requires":["dom","event"]},"autocomplete":{"type":"js","path":"autocomplete/autocomplete-min.js","requires":["dom","event"],"optional":["connection","animation"],"skinnable":true},"base":{"type":"css","path":"base/base-min.css"},"button":{"type":"js","path":"button/button-min.js","requires":["element"],"optional":["menu"],"skinnable":true},"calendar":{"type":"js","path":"calendar/calendar-min.js","requires":["event","dom"],"skinnable":true},"charts":{"type":"js","path":"charts/charts-experimental-min.js","requires":["element","json","datasource"]},"colorpicker":{"type":"js","path":"colorpicker/colorpicker-min.js","requires":["slider","element"],"optional":["animation"],"skinnable":true},"connection":{"type":"js","path":"connection/connection-min.js","requires":["event"]},"container":{"type":"js","path":"container/container-min.js","requires":["dom","event"],"optional":["dragdrop","animation","connection"],"supersedes":["containercore"],"skinnable":true},"containercore":{"type":"js","path":"container/container_core-min.js","requires":["dom","event"],"pkg":"container"},"cookie":{"type":"js","path":"cookie/cookie-beta-min.js","requires":["yahoo"]},"datasource":{"type":"js","path":"datasource/datasource-beta-min.js","requires":["event"],"optional":["connection"]},"datatable":{"type":"js","path":"datatable/datatable-beta-min.js","requires":["element","datasource"],"optional":["calendar","dragdrop"],"skinnable":true},"dom":{"type":"js","path":"dom/dom-min.js","requires":["yahoo"]},"dragdrop":{"type":"js","path":"dragdrop/dragdrop-min.js","requires":["dom","event"]},"editor":{"type":"js","path":"editor/editor-beta-min.js","requires":["menu","element","button"],"optional":["animation","dragdrop"],"skinnable":true},"element":{"type":"js","path":"element/element-beta-min.js","requires":["dom","event"]},"event":{"type":"js","path":"event/event-min.js","requires":["yahoo"]},"fonts":{"type":"css","path":"fonts/fonts-min.css"},"get":{"type":"js","path":"get/get-min.js","requires":["yahoo"]},"grids":{"type":"css","path":"grids/grids-min.css","requires":["fonts"],"optional":["reset"]},"history":{"type":"js","path":"history/history-min.js","requires":["event"]},"imagecropper":{"type":"js","path":"imagecropper/imagecropper-beta-min.js","requires":["dom","event","dragdrop","element","resize"],"skinnable":true},"imageloader":{"type":"js","path":"imageloader/imageloader-min.js","requires":["event","dom"]},"json":{"type":"js","path":"json/json-min.js","requires":["yahoo"]},"layout":{"type":"js","path":"layout/layout-beta-min.js","requires":["dom","event","element"],"optional":["animation","dragdrop","resize","selector"],"skinnable":true},"logger":{"type":"js","path":"logger/logger-min.js","requires":["event","dom"],"optional":["dragdrop"],"skinnable":true},"menu":{"type":"js","path":"menu/menu-min.js","requires":["containercore"],"skinnable":true},"profiler":{"type":"js","path":"profiler/profiler-beta-min.js","requires":["yahoo"]},"profilerviewer":{"type":"js","path":"profilerviewer/profilerviewer-beta-min.js","requires":["yuiloader","element"],"skinnable":true},"reset":{"type":"css","path":"reset/reset-min.css"},"reset-fonts-grids":{"type":"css","path":"reset-fonts-grids/reset-fonts-grids.css","supersedes":["reset","fonts","grids","reset-fonts"],"rollup":3},"reset-fonts":{"type":"css","path":"reset-fonts/reset-fonts.css","supersedes":["reset","fonts"],"rollup":2},"resize":{"type":"js","path":"resize/resize-beta-min.js","requires":["dom","event","dragdrop","element"],"optional":["animation"],"skinnable":true},"selector":{"type":"js","path":"selector/selector-beta-min.js","requires":["yahoo","dom"]},"simpleeditor":{"type":"js","path":"editor/simpleeditor-beta-min.js","requires":["element"],"optional":["containercore","menu","button","animation","dragdrop"],"skinnable":true,"pkg":"editor"},"slider":{"type":"js","path":"slider/slider-min.js","requires":["dragdrop"],"optional":["animation"]},"tabview":{"type":"js","path":"tabview/tabview-min.js","requires":["element"],"optional":["connection"],"skinnable":true},"treeview":{"type":"js","path":"treeview/treeview-min.js","requires":["event"],"skinnable":true},"uploader":{"type":"js","path":"uploader/uploader-experimental.js","requires":["yahoo"]},"utilities":{"type":"js","path":"utilities/utilities.js","supersedes":["yahoo","event","dragdrop","animation","dom","connection","element","yahoo-dom-event"],"rollup":6},"yahoo":{"type":"js","path":"yahoo/yahoo-min.js"},"yahoo-dom-event":{"type":"js","path":"yahoo-dom-event/yahoo-dom-event.js","supersedes":["yahoo","event","dom"],"rollup":3},"yuiloader":{"type":"js","path":"yuiloader/yuiloader-beta-min.js"},"yuitest":{"type":"js","path":"yuitest/yuitest-min.js","requires":["logger"],"skinnable":true}}},ObjectUtil:{appendArray:function(o,a){if(a){for(var i=0;
	i<a.length;i=i+1){o[a[i]]=true;}}},keys:function(o,ordered){var a=[],i;for(i in o){if(lang.hasOwnProperty(o,i)){a.push(i);}}return a;}},ArrayUtil:{appendArray:function(a1,a2){Array.prototype.push.apply(a1,a2);},indexOf:function(a,val){for(var i=0;i<a.length;i=i+1){if(a[i]===val){return i;}}return -1;},toObject:function(a){var o={};for(var i=0;i<a.length;i=i+1){o[a[i]]=true;}return o;},uniq:function(a){return YUI.ObjectUtil.keys(YUI.ArrayUtil.toObject(a));}}};YAHOO.util.YUILoader=function(o){this._internalCallback=null;this._useYahooListener=false;this.onSuccess=null;this.onFailure=Y.log;this.onProgress=null;this.scope=this;this.data=null;this.varName=null;this.base=YUI.info.base;this.ignore=null;this.force=null;this.allowRollup=true;this.filter=null;this.required={};this.moduleInfo=lang.merge(YUI.info.moduleInfo);this.rollups=null;this.loadOptional=false;this.sorted=[];this.loaded={};this.dirty=true;this.inserted={};var self=this;env.listeners.push(function(m){if(self._useYahooListener){self.loadNext(m.name);}});this.skin=lang.merge(YUI.info.skin);this._config(o);};Y.util.YUILoader.prototype={FILTERS:{RAW:{"searchExp":"-min\\.js","replaceStr":".js"},DEBUG:{"searchExp":"-min\\.js","replaceStr":"-debug.js"}},SKIN_PREFIX:"skin-",_config:function(o){if(!o){return ;}for(var i in o){if(lang.hasOwnProperty(o,i)){switch(i){case"require":this.require(o[i]);break;case"filter":var f=o[i];if(typeof f==="string"){f=f.toUpperCase();if(f==="DEBUG"){this.require("logger");}this.filter=this.FILTERS[f];}else{this.filter=f;}break;default:this[i]=o[i];}}}},addModule:function(o){if(!o||!o.name||!o.type||(!o.path&&!o.fullpath)){return false;}this.moduleInfo[o.name]=o;this.dirty=true;return true;},require:function(what){var a=(typeof what==="string")?arguments:what;this.dirty=true;for(var i=0;i<a.length;i=i+1){this.required[a[i]]=true;var s=this.parseSkin(a[i]);if(s){this._addSkin(s.skin,s.module);}}YUI.ObjectUtil.appendArray(this.required,a);},_addSkin:function(skin,mod){var name=this.formatSkin(skin);if(!this.moduleInfo[name]){this.addModule({"name":name,"type":"css","path":this.skin.base+skin+"/"+this.skin.path,"rollup":this.skin.rollup});}if(mod){name=this.formatSkin(skin,mod);if(!this.moduleInfo[name]){var mdef=this.moduleInfo[mod];var pkg=mdef.pkg||mod;this.addModule({"name":name,"type":"css","path":pkg+"/"+this.skin.base+skin+"/"+mod+".css"});}}},getRequires:function(mod){if(!mod){return[];}if(!this.dirty&&mod.expanded){return mod.expanded;}mod.requires=mod.requires||[];var i,d=[],r=mod.requires,o=mod.optional,info=this.moduleInfo,m;for(i=0;i<r.length;i=i+1){d.push(r[i]);m=info[r[i]];YUI.ArrayUtil.appendArray(d,this.getRequires(m));if(m.skinnable){var req=this.required,l=req.length;for(var j=0;j<l;j=j+1){if(req[j].indexOf(r[j])>-1){d.push(req[j]);}}}}if(o&&this.loadOptional){for(i=0;i<o.length;i=i+1){d.push(o[i]);YUI.ArrayUtil.appendArray(d,this.getRequires(info[o[i]]));}}mod.expanded=YUI.ArrayUtil.uniq(d);return mod.expanded;},getProvides:function(name){var mod=this.moduleInfo[name];var o={};o[name]=true;var s=mod&&mod.supersedes;YUI.ObjectUtil.appendArray(o,s);return o;},calculate:function(o){if(this.dirty){this._config(o);this._setup();this._explode();this._skin();if(this.allowRollup){this._rollup();}this._reduce();this._sort();this.dirty=false;}},_setup:function(){this.loaded=lang.merge(this.inserted);if(!this._sandbox){this.loaded=lang.merge(this.loaded,env.modules);}if(this.ignore){YUI.ObjectUtil.appendArray(this.loaded,this.ignore);}if(this.force){for(var i=0;i<this.force.length;i=i+1){if(this.force[i] in this.loaded){delete this.loaded[this.force[i]];}}}},_explode:function(){var r=this.required,i,mod;for(i in r){mod=this.moduleInfo[i];if(mod){var req=this.getRequires(mod);if(req){YUI.ObjectUtil.appendArray(r,req);}}}},_skin:function(){var r=this.required,i,mod;for(i in r){mod=this.moduleInfo[i];if(mod&&mod.skinnable){var o=this.skin.overrides,j;if(o&&o[i]){for(j=0;j<o[i].length;j=j+1){this.require(this.formatSkin(o[i][j],i));}}else{this.require(this.formatSkin(this.skin.defaultSkin,i));}}}},formatSkin:function(skin,mod){var s=this.SKIN_PREFIX+skin;if(mod){s=s+"-"+mod;}return s;},parseSkin:function(mod){if(mod.indexOf(this.SKIN_PREFIX)===0){var a=mod.split("-");return{skin:a[1],module:a[2]};}return null;},_rollup:function(){var i,j,m,s,rollups={},r=this.required,roll;if(this.dirty||!this.rollups){for(i in this.moduleInfo){m=this.moduleInfo[i];if(m&&m.rollup){rollups[i]=m;}}this.rollups=rollups;}for(;;){var rolled=false;for(i in rollups){if(!r[i]&&!this.loaded[i]){m=this.moduleInfo[i];s=m.supersedes;roll=false;if(!m.rollup){continue;}var skin=this.parseSkin(i),c=0;if(skin){for(j in r){if(i!==j&&this.parseSkin(j)){c++;roll=(c>=m.rollup);if(roll){break;}}}}else{for(j=0;j<s.length;j=j+1){if(this.loaded[s[j]]&&(!YUI.dupsAllowed[s[j]])){roll=false;break;}else{if(r[s[j]]){c++;roll=(c>=m.rollup);if(roll){break;}}}}}if(roll){r[i]=true;rolled=true;this.getRequires(m);}}}if(!rolled){break;}}},_reduce:function(){var i,j,s,m,r=this.required;for(i in r){if(i in this.loaded){delete r[i];}else{var skinDef=this.parseSkin(i);if(skinDef){if(!skinDef.module){var skin_pre=this.SKIN_PREFIX+skinDef.skin;for(j in r){if(j!==i&&j.indexOf(skin_pre)>-1){delete r[j];}}}}else{m=this.moduleInfo[i];s=m&&m.supersedes;if(s){for(j=0;j<s.length;j=j+1){if(s[j] in r){delete r[s[j]];}}}}}}},_sort:function(){var s=[],info=this.moduleInfo,loaded=this.loaded;var requires=function(aa,bb){if(loaded[bb]){return false;}var ii,mm=info[aa],rr=mm&&mm.expanded;if(rr&&YUI.ArrayUtil.indexOf(rr,bb)>-1){return true;}var ss=info[bb]&&info[bb].supersedes;if(ss){for(ii=0;ii<ss.length;ii=ii+1){if(requires(aa,ss[ii])){return true;}}}return false;};for(var i in this.required){s.push(i);}var p=0;for(;;){var l=s.length,a,b,j,k,moved=false;for(j=p;j<l;j=j+1){a=s[j];for(k=j+1;k<l;k=k+1){if(requires(a,s[k])){b=s.splice(k,1);s.splice(j,0,b[0]);moved=true;break;}}if(moved){break;}else{p=p+1;}}if(!moved){break;}}this.sorted=s;},toString:function(){var o={type:"YUILoader",base:this.base,filter:this.filter,required:this.required,loaded:this.loaded,inserted:this.inserted};
	lang.dump(o,1);},insert:function(o,type){this.calculate(o);if(!type){var self=this;this._internalCallback=function(){self._internalCallback=null;self.insert(null,"js");};this.insert(null,"css");return ;}this._loading=true;this.loadType=type;this.loadNext();},sandbox:function(o,type){if(o){}else{}this._config(o);if(!this.onSuccess){throw new Error("You must supply an onSuccess handler for your sandbox");}this._sandbox=true;var self=this;if(!type||type!=="js"){this._internalCallback=function(){self._internalCallback=null;self.sandbox(null,"js");};this.insert(null,"css");return ;}if(!util.Connect){var ld=new YAHOO.util.YUILoader();ld.insert({base:this.base,filter:this.filter,require:"connection",onSuccess:function(){this.sandbox(null,"js");},scope:this},"js");return ;}this._scriptText=[];this._loadCount=0;this._stopCount=this.sorted.length;this._xhr=[];this.calculate();var s=this.sorted,l=s.length,i,m,url;for(i=0;i<l;i=i+1){m=this.moduleInfo[s[i]];if(!m){this.onFailure.call(this.scope,{msg:"undefined module "+m,data:this.data});for(var j=0;j<this._xhr.length;j=j+1){this._xhr[j].abort();}return ;}if(m.type!=="js"){this._loadCount++;continue;}url=m.fullpath||this._url(m.path);var xhrData={success:function(o){var idx=o.argument[0],name=o.argument[2];this._scriptText[idx]=o.responseText;if(this.onProgress){this.onProgress.call(this.scope,{name:name,scriptText:o.responseText,xhrResponse:o,data:this.data});}this._loadCount++;if(this._loadCount>=this._stopCount){var v=this.varName||"YAHOO";var t="(function() {\n";var b="\nreturn "+v+";\n})();";var ref=eval(t+this._scriptText.join("\n")+b);this._pushEvents(ref);if(ref){this.onSuccess.call(this.scope,{reference:ref,data:this.data});}else{this.onFailure.call(this.scope,{msg:this.varName+" reference failure",data:this.data});}}},failure:function(o){this.onFailure.call(this.scope,{msg:"XHR failure",xhrResponse:o,data:this.data});},scope:this,argument:[i,url,s[i]]};this._xhr.push(util.Connect.asyncRequest("GET",url,xhrData));}},loadNext:function(mname){if(!this._loading){return ;}if(mname){if(mname!==this._loading){return ;}this.inserted[mname]=true;if(this.onProgress){this.onProgress.call(this.scope,{name:mname,data:this.data});}}var s=this.sorted,len=s.length,i,m;for(i=0;i<len;i=i+1){if(s[i] in this.inserted){continue;}if(s[i]===this._loading){return ;}m=this.moduleInfo[s[i]];if(!m){this.onFailure.call(this.scope,{msg:"undefined module "+m,data:this.data});return ;}if(!this.loadType||this.loadType===m.type){this._loading=s[i];var fn=(m.type==="css")?util.Get.css:util.Get.script,url=m.fullpath||this._url(m.path),self=this,c=function(o){self.loadNext(o.data);};if(env.ua.webkit&&env.ua.webkit<420&&m.type==="js"&&!m.varName){c=null;this._useYahooListener=true;}fn(url,{data:s[i],onSuccess:c,varName:m.varName,scope:self});return ;}}this._loading=null;if(this._internalCallback){var f=this._internalCallback;this._internalCallback=null;f.call(this);}else{if(this.onSuccess){this._pushEvents();this.onSuccess.call(this.scope,{data:this.data});}}},_pushEvents:function(ref){var r=ref||YAHOO;if(r.util&&r.util.Event){r.util.Event._load();}},_url:function(path){var u=this.base||"",f=this.filter;u=u+path;if(f){u=u.replace(new RegExp(f.searchExp),f.replaceStr);}return u;}};})();
	      
	  var loader = new YAHOO.util.YUILoader({
	    require: ['dragdrop','container'],
	    onSuccess: function() {
	    	ready = true;
	    }
	  });
	  loader.insert();
}
