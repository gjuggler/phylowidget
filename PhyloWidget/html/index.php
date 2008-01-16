<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=iso-8859-1" />
<title>${project.name}: Built with Processing</title>

<script type="text/javascript" src="phylowidget.js"></script>

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

</style>

</head>

<body>


<div id="content" style="margin-top: 5px;width:750px;">

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
<applet
 id="${project.name}"
 name="${project.name}"
 code="${internal.launcherClass}" 
 archive="${internal.allJars}"
 width="${applet.width}" height="${applet.height}"
 mayscript="true">
<?php

foreach ($_GET as $key => $value) {
	$key = stripslashes($key);
	$key = str_replace("\"","",$key);
	$value = stripslashes($value);
	$value = str_replace("\"","",$value);
	echo("<param name=\"$key\" value=\"$value\">");
  }

foreach ($_POST as $key => $value) {
	$key = stripslashes($key);
	$key = str_replace("\"","",$key);
	$value = stripslashes($value);
	$value = str_replace("\"","",$value);
	echo("<param name=\"$key\" value=\"$value\">");
}

?>
<param name="image" value="loading.gif">
<param name="boxmessage" value="Loading Processing software...">
<param name="boxbgcolor" value="#FFFFFF">
<param name="progressbar" value="true">
<param name="subapplet.classname" VALUE="${project.class}"> 
<param name="subapplet.displayname" VALUE="${project.name}"> 

<!-- This is the message that shows up when people don't have
     Java installed in their browser. Any HTML can go here
     (i.e. if you wanted to include an image other links, 
     or an anti-Microsoft diatribe. -->
To view this content, you need to install Java from <A HREF="http://java.com">java.com</A>
</applet>

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
${internal.appletComments}
</div>

<p>
<a href="${internal.sourceZip}">Source code</a>
</p>

</div>


</div>

</body>
</html>