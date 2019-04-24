var connect = require('connect');
var serveStatic = require('serve-static');

var __dirname = "./static/"

var myArgs = process.argv.slice(2);
if (myArgs.length == 1) {
    var port = myArgs[0];
	connect().use(serveStatic(__dirname)).listen(port, function(){
		console.log('Server running on port ' + port);
	});
} else {
    console.log("Invalid command line argument. Need a port number.");
}
