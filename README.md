DISCLAMER:

**THIS IS A WORK IN PROGRESS**

Done:

- Made the original plugin compatibile with Android 5.x (Lollipop)
- Changed the API name from "open" to "pick"

ToDo:

- Test the plugin on previous versions of Android (3.x up)

---

Cordova FileChooser Plugin

Requires Cordova >= 2.8.0

Install with Cordova CLI
	
	$ cordova plugin add http://github.com/MorisNotari/cordova-filechooser.git

Install with Plugman 

	$ plugman --platform android --project /path/to/project \ 
		--plugin http://github.com/MorisNotari/cordova-filechooser.git

API

	fileChooser.pick(successCallback, failureCallback);

The success callback get a JavaScript object with some information and a base64 encoded content of the selected file

	fileChooser.pick(function(fileData) {
		console.log('uri: ', fileData.uri);
		console.log('mime: ', fileData.mime);
		console.log('ext: ', fileData.ext);
		console.log('base64: ', fileData.base64);
	});

Screenshot

![Screenshot](filechooser.png "Screenshot")
