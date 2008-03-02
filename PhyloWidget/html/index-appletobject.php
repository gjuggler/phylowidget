<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=iso-8859-1" />
<title>${project.name}: Built with Processing</title>
<link rel="stylesheet" href="appletobject.css"></link>
<style type="text/css">
body {
  margin: 0px 0px 0px 0px;
  padding: 0px 0px 0px 0px;
  text-align: center;
  font-family: verdana, geneva, arial, helvetica, sans-serif; 
  font-size: 11px; 
  background-color: white; 
  text-decoration: none; 
  font-weight: normal; 
  line-height: normal;
}
 
A          { color: #3399CC; text-decoration: none; }
A:link     { color: #3399CC; text-decoration: none; }
A:visited  { color: #3399CC; text-decoration: none; }
A:active   { color: #3399CC; text-decoration: underline; }
A:hover    { color: #3399CC; text-decoration: underline; }

div {
  border: 0px;

}

#content {
  margin: auto;
}

#content fieldset {
  margin-top: 5px;
}

#comments {
}

input {
  font-size:12px;
}

fieldset {
  display:block;
  background:#CEE3F6;
  margin:auto;
  text-align:center;
}

legend {
  background:white;
  border:1px solid black;
  padding:5px;
}

#treeText,#clipText {
  width:100%;
}

table {
  text-align:left;
}

table .key {
  text-align:right;
  padding-right:10px;
}

table .val {
 text-align:left;
}

.rect, a .rect, .rect a, ul.menu li a {
	font-size:16px;
	font-weight:bold;
	color:#0e4c92;
	background-color:white;
	border: 2px solid #0e4c92;
	text-decoration: none;
	text-align: center;
}
.rect a:hover, a:hover .rect, ul.menu li a:hover {
	color:#005;
	background-color:#e1e1f6;
}

#div_applet {
	margin:auto;
}

</style>

</head>

<body>

<div id="content" style="margin-top: 5px;width:750px;">

<script type="text/javascript" src="phylowidget.js"></script>
<script type="text/javascript" src="appletobject.js"></script>
<script type="text/javascript">
//<![CDATA[
function loadApplet(element_id)
{
  var ao = new AppletObject(
	'${internal.launcherClass}',
	[${internal.quotedJars}],
	'${applet.width}',
	'${applet.height}',
	'0', // java version: 1.4.2
	'true',  // mayscript
	'',   // codebase
	[], // additional params
	AppletObjects.TAG_OBJECT
  );
  ao.addParams(['boxmessage','Loading ${project.name}...']);

  ao.addParams(
<?php

foreach ($_GET as $key => $value) {
	$key = stripslashes($key);
	$key = str_replace("\"","",$key);
	$value = stripslashes($value);
	$value = str_replace("\"","",$value);
	//echo("<param name=\"$key\" value=\"$value\">");
	echo("[\'$key\',\'$value\']");
  }

foreach ($_POST as $key => $value) {
	$key = stripslashes($key);
	$key = str_replace("\"","",$key);
	$value = stripslashes($value);
	$value = str_replace("\"","",$value);
	//echo("<param name=\"$key\" value=\"$value\">");
	echo("[\'$key\',\'$value\']");
}

?>
  );
  ao.preload(element_id);
}

//]]>
</script>

<div style="float:right;width:300px;">
<fieldset style="margin-top:0px;margin-left:5px;">
<legend>Node Info</legend>
<div id="nodeText" style="background:white;margin:5px;padding:5px;">
Mouse over a node to view its detailed information here.
</div>
</fieldset>
</div>

<div style="">
<fieldset style="height:450px;">
<legend>${project.name}</legend>
<div id="div_applet" style="height:400px;">
<script type="text/javascript" >
		//<![CDATA[
			document.write(
	'<a href="javascript:loadApplet(\'div_applet\')"><div class="rect" id="runbutton" style="display:table-cell;width:${applet.width}px;height:${applet.height}px;margin:auto;clear:both;vertical-align:middle;">Run ${project.name}</div></a>'
	);
	document.getElementById('runbutton').style.fontSize = (${applet.height} / 20) + "px";
		//]]>
</script>
</div>
</fieldset>
</div>

<div style="">
<form>
<fieldset>
<legend>Tree Text</legend>
<input name="treeText" id="treeText" onFocus="selectOnce(this);" value="Paste your Newick-formatted tree here, then press 'Create Tree'.">
</input>
<br/>
<input style="margin-top:5px;" type="button" name="sendButton" onClick="javascript:updateJavaTree();" value="Create Tree"/>
</fieldset>
<fieldset>
<legend>Clipboard Text</legend>
<input name="clipText" id="clipText" onChange="updateJavaClip();" value="Clipboard area. This text will synchronize with PhyloWidget's clipboard.">
</input>
</fieldset>
</form>

<div id="comments" style="">
${internal.appletcomments}
</div>

<p>
${internal.sourcehtml}
<p>
${internal.standalonehtml}

</div>


</div>
</body>
</html>