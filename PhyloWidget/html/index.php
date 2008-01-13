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

#content {
  width: 600px;
  margin: auto;
}

#content div {
  margin: auto;
}

#comments {
}

input {
  font-size:12px;
}

fieldset {
  background:#CEE3F6;
  margin:auto;
  text-align:center;
  margin-top: 5px;
}

legend {
  background:white;
  border:1px solid black;
  padding:5px;
}

#treeText,#clipText {
  width:100%;
}
</style>

</head>

<body>

<div id="content">

<fieldset>
<legend>PhyloWidget</legend>
<div id="sketch">
<applet
 name="PhyloWidget"
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

</div>
</fieldset>

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

<div id="comments" style="clear:left;margin-top:10px;">
${internal.appletComments}
</div>

<p>
<a href="${internal.sourceZip}">Source code</a>
</p>

</div>

</body>
</html>