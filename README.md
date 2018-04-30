docx-html-editor
================

A proof of concept (JAX-RS 2.0) to show how a web-based HTML rich text editors (eg CkEditor) can be 
used to edit docx files (via XHTML round trip).

We've published this as a "cheap and cheerful" option only; for a commercial grade solution (new in 2018), please see 
https://nativedocuments.com/ (which takes a completely different approach).

That said, it should work OK for documents containing text, tables, and images.
Exotic Word features are not supported.

Interested docx4j community members are welcome to improve it and offer pull requests.

docx-html-editor is licensed under the AGPLv3.  This means you have to publish any changes you make. 

How it works
============

The idea is:

- use docx4j to convert the docx to XHTML
- use CKEditor to edit that XHTML in the web browser
- on submit, convert the XHTML back to docx content

The general problem with converting to/from XHTML is the “impendance mismatch”.  That is, losing stuff during round trip.  This will be a familiar problem to anyone who has ever edited a docx in Google Docs or LibreOffice.

This demo addresses that problem by identifying docx content which CKEditor would mangle, and then on submit/save, using the original docx content for those bits.

In this demo, the problematic content is replaced with visual placeholders, so you can see it is there.

Getting started
===============

Pre-reqs: Since we don't distribute a binary, you'll need maven to build from source.


Get CKeditor (v3.6.6.1); unzip it to src/main/webapp/ckeditor 
(so you have src/main/webapp/ckeditor/ckeditor.js etc)

Before build the war:

Download https://github.com/jcraane/textimagegenerator

(May be correct name in pom.xml from "SNAAPSHOT" to "SNAPSHOT")

Build jar:
     
     mvn install

Install textimagegen jar to local-maven-rep:
     
     mvn install:install-file -Dfile=target\TextImageGen-2.0-SNAPSHOT.jar -DpomFile=pom.xml

Go to docx-html-editor.

Build the war:
  
     mvn install
     
Load the resulting war file from dir target to your app server (eg Tomcat)

Visit the upload page in your web browser

	/docx-html-editor/

There's a bit more doco at https://github.com/plutext/docx-html-editor/blob/master/documentation/user/docx-html-editor_UserManual.docx?raw=true

Developing
==========

Set this up as a Maven project (eg in Eclipse) to get the dependencies.


  
